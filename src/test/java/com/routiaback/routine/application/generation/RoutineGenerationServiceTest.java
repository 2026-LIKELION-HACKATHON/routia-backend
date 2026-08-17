package com.routiaback.routine.application.generation;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import tools.jackson.databind.ObjectMapper;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.personalization.application.port.*;
import com.routiaback.personalization.domain.*;
import com.routiaback.routine.application.port.*;
import com.routiaback.routine.domain.*;
import com.routiaback.weather.application.port.*;
import com.routiaback.weather.domain.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

class RoutineGenerationServiceTest {
    private static final Instant NOW=Instant.parse("2026-08-15T00:00:00Z"); private static final LocalDate DATE=LocalDate.of(2026,8,15);
    private final UserProfileRepositoryPort profiles=mock(UserProfileRepositoryPort.class); private final UserNeedsRepositoryPort needs=mock(UserNeedsRepositoryPort.class);
    private final UserLocationPort locations=mock(UserLocationPort.class); private final WeatherClientPort weather=mock(WeatherClientPort.class);
    private final DailyRoutineRepositoryPort routines=mock(DailyRoutineRepositoryPort.class); private final RoutineItemRepositoryPort items=mock(RoutineItemRepositoryPort.class);
    private final AiRoutineGenerationPort ai=mock(AiRoutineGenerationPort.class); private final RoutineGenerationTransactionService tx=mock(RoutineGenerationTransactionService.class);
    private RoutineGenerationService service; private DailyRoutine generating;

    @BeforeEach void setUp(){service=new RoutineGenerationService(profiles,needs,locations,weather,routines,items,ai,new RoutineGenerationValidator(),tx,new ObjectMapper(),Clock.fixed(NOW,ZoneOffset.UTC));
        UserProfile p=new UserProfile(1L,new BigDecimal("165.3"),new BigDecimal("55.2"),Gender.FEMALE,AgeGroup.TWENTIES,null,"서울","강남",new BigDecimal("37.5"),new BigDecimal("127.0"),LocationSource.GPS,NOW,NOW,NOW);
        UserPreference pref=new UserPreference(1L,BodyGoal.BUILD_HABIT,SkinType.SENSITIVE,RoutineTimePreference.MORNING,RoutineDifficulty.SIMPLE,NOW,NOW);
        given(profiles.findByUserId(1L)).willReturn(Optional.of(p));given(needs.findPreferenceByUserId(1L)).willReturn(Optional.of(pref));given(needs.findBodyConcernCodes(1L)).willReturn(List.of("FATIGUE"));given(needs.findSkinConcernCodes(1L)).willReturn(List.of("WRINKLE"));
        given(needs.findBodyConcernNames(List.of("FATIGUE"))).willReturn(Map.of("FATIGUE", "피로감"));
        given(needs.findSkinConcernNames(List.of("WRINKLE"))).willReturn(Map.of("WRINKLE", "주름"));
        given(locations.findByUserId(1L)).willReturn(Optional.of(new UserLocation(new BigDecimal("37.5"),new BigDecimal("127.0"),"서울","강남")));given(weather.fetchCurrentWeather(anyDouble(),anyDouble())).willReturn(new WeatherInfo(28,29,1,6));given(routines.findByUserIdAndRoutineDate(1L,DATE)).willReturn(Optional.empty());given(routines.findAllByUserIdAndRoutineDateBetween(anyLong(),any(),any())).willReturn(List.of());given(items.findAllByRoutineIds(anyList())).willReturn(List.of());
        generating=new DailyRoutine(7L,1L,null,DATE,RoutineStatus.GENERATING,null,null,RoutineDifficulty.SIMPLE,RoutineTimePreference.MORNING,null,null,null,null,null,null,null,NOW,NOW);
        given(tx.reserve(anyLong(),any(),any(),any(),any(),any())).willReturn(new RoutineGenerationTransactionService.Reservation(generating,true));
    }

    @Test void composesInputsSnapshotsAndStoresReadyRoutine(){GeneratedRoutine generated=new GeneratedRoutine("방향","코멘트",List.of(new GeneratedRoutine.GeneratedItem("MORNING","SKIN","세안","미온수 세안","CLEAN","피부 청결")));given(ai.generate(any())).willReturn(generated);given(ai.model()).willReturn("model-x");given(ai.promptVersion()).willReturn("routine-v1");DailyRoutine ready=generating.ready(2L,"방향","코멘트",RoutineDifficulty.SIMPLE,RoutineTimePreference.MORNING,"{}","{}","model-x","routine-v1",NOW);given(tx.complete(any(),any(),any(),any(),any(),anyString(),anyString(),anyString(),anyString(),any())).willReturn(ready);
        var result=service.generate(1L,DATE,RoutineGenerationType.INITIAL_ONBOARDING,null);
        assertThat(result.status()).isEqualTo(RoutineStatus.READY);ArgumentCaptor<RoutineGenerationRequest> request=ArgumentCaptor.forClass(RoutineGenerationRequest.class);verify(ai).generate(request.capture());assertThat(request.getValue().needs().bodyConcerns()).containsExactly("FATIGUE");assertThat(request.getValue().bodyConcernNames()).containsEntry("FATIGUE", "피로감");assertThat(request.getValue().skinConcernNames()).containsEntry("WRINKLE", "주름");assertThat(request.getValue().weather().uvIndex()).isEqualTo(6);assertThat(request.getValue().weather().weatherCondition()).isEqualTo("구름 조금");
        ArgumentCaptor<String> personalization=ArgumentCaptor.forClass(String.class);ArgumentCaptor<String> performance=ArgumentCaptor.forClass(String.class);verify(tx).complete(eq(generating),any(),eq(generated),eq(RoutineDifficulty.SIMPLE),eq(RoutineTimePreference.MORNING),personalization.capture(),performance.capture(),eq("model-x"),eq("routine-v1"),any());assertThat(personalization.getValue()).contains("BUILD_HABIT","FATIGUE","피로감","SENSITIVE","WRINKLE","주름");assertThat(performance.getValue()).contains("recentAverageRate");
    }

    @Test void returnsStoredAiResultWithoutRegenerationWhenReadyRoutineAlreadyExists(){DailyRoutine ready=generating.ready(2L,"기존 방향","기존 코멘트",RoutineDifficulty.SIMPLE,RoutineTimePreference.MORNING,"{}","{}","m","v",NOW);given(routines.findByUserIdAndRoutineDate(1L,DATE)).willReturn(Optional.of(ready));given(items.findAllByRoutineIdOrderBySortOrder(7L)).willReturn(List.of(new RoutineItem(11L,7L,"MORNING","SKIN","기존 세안","상세","CLEAN","청결",1,false,null,NOW,NOW)));var result=service.generate(1L,DATE,RoutineGenerationType.SCHEDULED_DAILY,NOW);assertThat(result.generated()).isFalse();assertThat(result.routine().directionText()).isEqualTo("기존 방향");assertThat(result.routine().items()).singleElement().extracting(GeneratedRoutine.GeneratedItem::detail).isEqualTo("상세");verifyNoInteractions(ai,weather);}

    @Test void rejectsMissingCoordinatesBeforeRoutineReservation(){given(locations.findByUserId(1L)).willReturn(Optional.of(new UserLocation(null,null,"서울","강남")));assertThatThrownBy(()->service.generate(1L,DATE,RoutineGenerationType.INITIAL_ONBOARDING,null)).isInstanceOf(ApiException.class).extracting("errorCode").isEqualTo(ErrorCode.USER_LOCATION_NOT_FOUND);verifyNoInteractions(tx,weather,ai);}

    @Test void marksGeneratingRoutineFailedWhenAiFails(){given(ai.generate(any())).willThrow(new IllegalStateException("timeout"));assertThatThrownBy(()->service.generate(1L,DATE,RoutineGenerationType.INITIAL_ONBOARDING,null)).isInstanceOf(com.routiaback.global.error.ApiException.class);verify(tx).fail(7L,"EXTERNAL_AI_ERROR",NOW);}
}
