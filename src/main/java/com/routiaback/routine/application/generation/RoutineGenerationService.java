package com.routiaback.routine.application.generation;

import com.routiaback.global.error.*;
import com.routiaback.personalization.application.port.*;
import com.routiaback.personalization.application.result.*;
import com.routiaback.personalization.domain.*;
import com.routiaback.routine.application.port.*;
import com.routiaback.routine.domain.*;
import com.routiaback.weather.application.port.*;
import com.routiaback.weather.application.WeatherCodeMapper;
import com.routiaback.weather.domain.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class RoutineGenerationService {
    private final UserProfileRepositoryPort profiles; private final UserNeedsRepositoryPort needs;
    private final UserLocationPort locations; private final WeatherClientPort weatherClient;
    private final DailyRoutineRepositoryPort routines; private final RoutineItemRepositoryPort items;
    private final AiRoutineGenerationPort ai; private final RoutineGenerationValidator validator;
    private final RoutineGenerationTransactionService transactions; private final ObjectMapper objectMapper; private final Clock clock;
    public RoutineGenerationService(UserProfileRepositoryPort profiles,UserNeedsRepositoryPort needs,UserLocationPort locations,WeatherClientPort weatherClient,DailyRoutineRepositoryPort routines,RoutineItemRepositoryPort items,AiRoutineGenerationPort ai,RoutineGenerationValidator validator,RoutineGenerationTransactionService transactions,ObjectMapper objectMapper,Clock clock){this.profiles=profiles;this.needs=needs;this.locations=locations;this.weatherClient=weatherClient;this.routines=routines;this.items=items;this.ai=ai;this.validator=validator;this.transactions=transactions;this.objectMapper=objectMapper;this.clock=clock;}

    public GenerationOutcome generate(Long userId,LocalDate date,RoutineGenerationType type,Instant notificationAt){
        Optional<DailyRoutine> existing=routines.findByUserIdAndRoutineDate(userId,date);
        if(existing.isPresent()&&(existing.get().status()==RoutineStatus.READY||existing.get().status()==RoutineStatus.GENERATING))return outcome(existing.get(),false);
        Instant now=clock.instant();
        ProfileResult profile=ProfileResult.from(profiles.findByUserId(userId).orElseThrow(()->new ApiException(ErrorCode.ROUTINE_GENERATION_INPUT_INVALID)));
        UserPreference preference=needs.findPreferenceByUserId(userId).orElseThrow(()->new ApiException(ErrorCode.ROUTINE_GENERATION_INPUT_INVALID));
        NeedsResult need=NeedsResult.from(preference,needs.findBodyConcernCodes(userId),needs.findSkinConcernCodes(userId));
        if(preference.routineDifficulty()==null)throw new ApiException(ErrorCode.ROUTINE_GENERATION_INPUT_INVALID);
        UserLocation location=locations.findByUserId(userId)
                .filter(value -> value.latitude() != null && value.longitude() != null)
                .orElseThrow(()->new ApiException(ErrorCode.USER_LOCATION_NOT_FOUND));
        RoutineGenerationTransactionService.Reservation reservation;
        try{reservation=transactions.reserve(userId,date,preference.routineDifficulty(),preference.routineTimePreference(),type==RoutineGenerationType.INITIAL_ONBOARDING?null:notificationAt,now);}catch(DataIntegrityViolationException ex){DailyRoutine found=routines.findByUserIdAndRoutineDate(userId,date).orElseThrow(()->ex);return outcome(found,false);}
        DailyRoutine routine=reservation.routine(); if(!reservation.created())return outcome(routine,false);
        try{
            WeatherInfo weather=weatherClient.fetchCurrentWeather(location.latitude().doubleValue(),location.longitude().doubleValue());
            RoutineGenerationRequest.PerformanceInput performance=performance(userId,date);
            RoutineGenerationRequest request=new RoutineGenerationRequest(date,profile,need,new RoutineGenerationRequest.WeatherInput(weather.temperature(),weather.feelsLike(),weather.weatherCode(),weather.uvIndex()),performance);
            GeneratedRoutine generated=ai.generate(request); validator.validate(generated,preference.routineDifficulty());
            WeatherSnapshot snapshot=new WeatherSnapshot(null,userId,date,location.regionSido(),location.regionSigungu(),location.latitude(),location.longitude(),BigDecimal.valueOf(weather.temperature()),BigDecimal.valueOf(weather.uvIndex()),WeatherCodeMapper.toDescription(weather.weatherCode()),"OPEN_METEO",now,now);
            DailyRoutine ready=transactions.complete(routine,snapshot,generated,preference.routineDifficulty(),preference.routineTimePreference(),json(personalization(request)),json(performance),ai.model(),ai.promptVersion(),clock.instant());
            return new GenerationOutcome(ready.id(),ready.status(),true,generated);
        }catch(RuntimeException ex){transactions.fail(routine.id(),errorCode(ex),clock.instant());throw ex instanceof ApiException?ex:new ApiException(ErrorCode.ROUTINE_GENERATION_FAILED,ex);}
    }

    private Map<String,Object> personalization(RoutineGenerationRequest r){Map<String,Object> m=new LinkedHashMap<>();m.put("height",r.profile().height());m.put("weight",r.profile().weight());m.put("gender",r.profile().gender());m.put("ageGroup",r.profile().ageGroup());m.put("bodyGoal",r.needs().bodyGoal());m.put("bodyConcerns",r.needs().bodyConcerns());m.put("skinType",r.needs().skinType());m.put("skinConcerns",r.needs().skinConcerns());m.put("routineTimePreference",r.needs().routineTimePreference());m.put("routineDifficulty",r.needs().routineDifficulty());return m;}
    private String json(Object value){try{return objectMapper.writeValueAsString(value);}catch(JacksonException ex){throw new IllegalStateException("snapshot serialization failed",ex);}}
    private String errorCode(RuntimeException ex){return ex instanceof ApiException a?a.getErrorCode().name():"EXTERNAL_AI_ERROR";}

    private RoutineGenerationRequest.PerformanceInput performance(Long userId,LocalDate date){
        List<DailyRoutine> history=routines.findAllByUserIdAndRoutineDateBetween(userId,date.minusDays(7),date.minusDays(1));
        List<Long> ids=history.stream().map(DailyRoutine::id).toList(); List<RoutineItem> all=items.findAllByRoutineIds(ids);
        Map<Long,List<RoutineItem>> byRoutine=all.stream().collect(Collectors.groupingBy(RoutineItem::routineId));
        DailyRoutine yesterday=history.stream().filter(r->r.routineDate().equals(date.minusDays(1))).findFirst().orElse(null);
        List<RoutineItem> previous=yesterday==null?List.of():byRoutine.getOrDefault(yesterday.id(),List.of());
        Integer completed=yesterday==null?null:(int)previous.stream().filter(RoutineItem::completed).count(); Integer total=yesterday==null?null:previous.size(); Double rate=total==null||total==0?null:completed/(double)total;
        double avgRate=history.stream().map(byRoutine::get).filter(Objects::nonNull).filter(l->!l.isEmpty()).mapToDouble(l->l.stream().filter(RoutineItem::completed).count()/(double)l.size()).average().orElse(0);
        double avgCompleted=history.stream().map(byRoutine::get).filter(Objects::nonNull).mapToLong(l->l.stream().filter(RoutineItem::completed).count()).average().orElse(0);
        Map<String,Double> category=rates(all,RoutineItem::category); Map<String,Double> slots=rates(all,RoutineItem::timeSlot);
        return new RoutineGenerationRequest.PerformanceInput(completed,total,rate,previous.stream().filter(i->!i.completed()).map(RoutineItem::category).distinct().toList(),previous.stream().filter(i->!i.completed()).map(RoutineItem::timeSlot).distinct().toList(),avgRate,avgCompleted,category,slots,extreme(slots,false),extreme(slots,true));
    }
    private Map<String,Double> rates(List<RoutineItem> all,java.util.function.Function<RoutineItem,String> key){return all.stream().collect(Collectors.groupingBy(key,Collectors.collectingAndThen(Collectors.toList(),l->l.isEmpty()?0:l.stream().filter(RoutineItem::completed).count()/(double)l.size())));}
    private String extreme(Map<String,Double> rates,boolean max){return rates.entrySet().stream().min((a,b)->max?Double.compare(b.getValue(),a.getValue()):Double.compare(a.getValue(),b.getValue())).map(Map.Entry::getKey).orElse(null);}
    private GenerationOutcome outcome(DailyRoutine routine,boolean generated){
        GeneratedRoutine result=null;
        if(routine.status()==RoutineStatus.READY){
            List<GeneratedRoutine.GeneratedItem> storedItems=items.findAllByRoutineIdOrderBySortOrder(routine.id()).stream()
                    .map(item->new GeneratedRoutine.GeneratedItem(item.timeSlot(),item.category(),item.title(),item.detail(),item.effectCode(),item.expectedEffect()))
                    .toList();
            result=new GeneratedRoutine(routine.directionText(),routine.homeComment(),storedItems);
        }
        return new GenerationOutcome(routine.id(),routine.status(),generated,result);
    }
    public record GenerationOutcome(Long routineId,RoutineStatus status,boolean generated,GeneratedRoutine routine){
        public GenerationOutcome(Long routineId,RoutineStatus status,boolean generated){this(routineId,status,generated,null);}
    }
}
