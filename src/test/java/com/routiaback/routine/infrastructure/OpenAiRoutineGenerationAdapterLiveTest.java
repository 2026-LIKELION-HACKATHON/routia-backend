package com.routiaback.routine.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.personalization.application.result.NeedsResult;
import com.routiaback.personalization.application.result.ProfileResult;
import com.routiaback.personalization.domain.AgeGroup;
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import com.routiaback.routine.application.generation.GeneratedRoutine;
import com.routiaback.routine.application.generation.RoutineGenerationRequest;
import com.routiaback.routine.application.generation.RoutineGenerationValidator;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiRoutineGenerationAdapterLiveTest {

    @Test
    void generatesValidRoutineThroughRealResponsesApi() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
        String model = System.getenv().getOrDefault("AI_MODEL", "gpt-5.6-luna");
        OpenAiRoutineGenerationAdapter adapter = new OpenAiRoutineGenerationAdapter(
                objectMapper,
                new PromptTemplateLoader(new ClassPathResource("prompts/routine-v1.txt"), "routine-v1"),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                URI.create("https://api.openai.com/v1/responses"),
                System.getenv("OPENAI_API_KEY"),
                model,
                "low",
                3_000,
                Duration.ofSeconds(60));

        GeneratedRoutine result = adapter.generate(request());

        new RoutineGenerationValidator().validate(result, RoutineDifficulty.COMPLEX);
        assertThat(result.directionText()).isNotBlank();
        assertThat(result.homeComment()).isNotBlank();
        assertThat(result.items()).isNotEmpty().hasSizeLessThanOrEqualTo(12);
        assertThat(result.items()).allSatisfy(item -> {
            assertThat(item.title()).isNotBlank();
            assertThat(item.detail()).isNotBlank();
            assertThat(item.effectCode()).isNotBlank();
            assertThat(item.expectedEffect()).isNotBlank();
        });
        System.out.println("LIVE_OPENAI_RESULT=" + objectMapper.writeValueAsString(result));
    }

    private RoutineGenerationRequest request() {
        ProfileResult profile = new ProfileResult(
                new BigDecimal("165.3"),
                new BigDecimal("55.2"),
                Gender.FEMALE,
                AgeGroup.TWENTIES,
                null,
                "서울특별시",
                "중구",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                null,
                null);
        NeedsResult needs = new NeedsResult(
                BodyGoal.BUILD_HABIT,
                List.of("SHOULDER_NECK", "LOWER_BODY_SWELLING"),
                SkinType.COMBINATION,
                List.of("DRYNESS", "PORES"),
                RoutineTimePreference.ANY,
                RoutineDifficulty.COMPLEX);
        RoutineGenerationRequest.PerformanceInput performance =
                new RoutineGenerationRequest.PerformanceInput(
                        3,
                        5,
                        0.6,
                        List.of("BODY"),
                        List.of("EVENING"),
                        0.68,
                        3.4,
                        Map.of("SKIN", 0.8, "BODY", 0.5),
                        Map.of("MORNING", 0.8, "EVENING", 0.4),
                        "EVENING",
                        "MORNING");
        return new RoutineGenerationRequest(
                LocalDate.of(2026, 8, 16),
                profile,
                needs,
                new RoutineGenerationRequest.WeatherInput(29.0, 31.0, 1, 7.2),
                performance);
    }
}
