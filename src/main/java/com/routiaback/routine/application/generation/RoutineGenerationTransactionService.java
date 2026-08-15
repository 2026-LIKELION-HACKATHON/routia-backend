package com.routiaback.routine.application.generation;

import com.routiaback.routine.application.port.*;
import com.routiaback.routine.domain.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutineGenerationTransactionService {
    private final DailyRoutineRepositoryPort routines; private final RoutineItemRepositoryPort items;
    private final WeatherSnapshotRepositoryPort weatherSnapshots;
    public RoutineGenerationTransactionService(DailyRoutineRepositoryPort routines,RoutineItemRepositoryPort items,WeatherSnapshotRepositoryPort weatherSnapshots){this.routines=routines;this.items=items;this.weatherSnapshots=weatherSnapshots;}

    @Transactional
    public Reservation reserve(Long userId,LocalDate date,
            com.routiaback.personalization.domain.RoutineDifficulty difficulty,
            com.routiaback.personalization.domain.RoutineTimePreference timePreference,
            Instant notificationAt,Instant now){
        return routines.findByUserIdAndRoutineDate(userId,date).map(r->new Reservation(r,false))
                .orElseGet(()->new Reservation(routines.save(DailyRoutine.generating(userId,date,difficulty,timePreference,notificationAt,now)),true));
    }

    @Transactional
    public DailyRoutine complete(DailyRoutine generating,WeatherSnapshot weather,GeneratedRoutine generated,
            com.routiaback.personalization.domain.RoutineDifficulty difficulty,
            com.routiaback.personalization.domain.RoutineTimePreference timePreference,
            String personalizationJson,String performanceJson,String model,String promptVersion,Instant now){
        WeatherSnapshot savedWeather=weatherSnapshots.save(weather);
        DailyRoutine ready=routines.save(generating.ready(savedWeather.id(),generated.directionText(),generated.homeComment(),difficulty,timePreference,personalizationJson,performanceJson,model,promptVersion,now));
        List<RoutineItem> generatedItems=new ArrayList<>(); int order=1;
        for(GeneratedRoutine.GeneratedItem item:generated.items()) generatedItems.add(RoutineItem.generated(ready.id(),RoutineTimeSlot.valueOf(item.timeSlot()),RoutineCategory.valueOf(item.category()),item.title(),item.detail(),item.effectCode(),item.expectedEffect(),order++,now));
        items.saveAll(generatedItems); return ready;
    }

    @Transactional
    public void fail(Long routineId,String errorCode,Instant now){routines.findById(routineId).filter(r->r.status()==RoutineStatus.GENERATING).ifPresent(r->routines.save(r.failed(errorCode,now)));}
    public record Reservation(DailyRoutine routine,boolean created){}
}
