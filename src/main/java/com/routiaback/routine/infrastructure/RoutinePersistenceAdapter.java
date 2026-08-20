package com.routiaback.routine.infrastructure;

import com.routiaback.routine.application.port.DailyRoutineRepositoryPort;
import com.routiaback.routine.application.port.RoutineItemRepositoryPort;
import com.routiaback.routine.application.port.WeatherSnapshotRepositoryPort;
import com.routiaback.routine.domain.DailyRoutine;
import com.routiaback.routine.domain.RoutineItem;
import com.routiaback.routine.domain.WeatherSnapshot;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import com.routiaback.routine.domain.RoutineStatus;

@Repository
class RoutinePersistenceAdapter implements DailyRoutineRepositoryPort, RoutineItemRepositoryPort, WeatherSnapshotRepositoryPort {

    private final DailyRoutineJpaRepository dailyRoutineJpaRepository;
    private final RoutineItemJpaRepository routineItemJpaRepository;
    private final WeatherSnapshotJpaRepository weatherSnapshotJpaRepository;

    RoutinePersistenceAdapter(DailyRoutineJpaRepository dailyRoutineJpaRepository,
                              RoutineItemJpaRepository routineItemJpaRepository,
                              WeatherSnapshotJpaRepository weatherSnapshotJpaRepository) {
        this.dailyRoutineJpaRepository = dailyRoutineJpaRepository;
        this.routineItemJpaRepository = routineItemJpaRepository;
        this.weatherSnapshotJpaRepository = weatherSnapshotJpaRepository;
    }

    @Override
    public Optional<DailyRoutine> findByUserIdAndRoutineDate(Long userId, LocalDate routineDate) {
        return dailyRoutineJpaRepository.findByUserIdAndRoutineDate(userId, routineDate)
                .map(this::toDomain);
    }

    @Override
    public List<RoutineItem> findAllByRoutineIdOrderBySortOrder(Long routineId) {
        return routineItemJpaRepository.findAllByRoutineIdOrderBySortOrderAsc(routineId)
                .stream()
                .map(e -> new RoutineItem(e.getId(), e.getRoutineId(), e.getTimeSlot(), e.getCategory(),
                        e.getTitle(), e.getDetail(), e.getEffectCode(), e.getExpectedEffect(), e.getSortOrder(), e.isCompleted(), e.getCompletedAt(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
    }

    @Override
    public Optional<DailyRoutine> findById(Long id) {
        return dailyRoutineJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<RoutineItem> findItemById(Long itemId) {
        return routineItemJpaRepository.findById(itemId)
                .map(e -> new RoutineItem(e.getId(), e.getRoutineId(), e.getTimeSlot(), e.getCategory(),
                        e.getTitle(), e.getDetail(), e.getEffectCode(), e.getExpectedEffect(), e.getSortOrder(), e.isCompleted(), e.getCompletedAt(),
                        e.getCreatedAt(), e.getUpdatedAt()));
    }

    @Override
    public RoutineItem save(RoutineItem item) {
        RoutineItemJpaEntity entity = new RoutineItemJpaEntity(item.id(), item.routineId(), item.timeSlot(),
                item.category(), item.title(), item.detail(), item.effectCode(), item.expectedEffect(), item.sortOrder(), item.completed(),
                item.completedAt(), item.createdAt(), item.updatedAt());
        RoutineItemJpaEntity saved = routineItemJpaRepository.save(entity);
        return new RoutineItem(saved.getId(), saved.getRoutineId(), saved.getTimeSlot(), saved.getCategory(),
                saved.getTitle(), saved.getDetail(), saved.getEffectCode(), saved.getExpectedEffect(), saved.getSortOrder(), saved.isCompleted(),
                saved.getCompletedAt(), saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @Override
    public List<DailyRoutine> findAllByUserIdAndRoutineDateBetween(Long userId, LocalDate start, LocalDate end) {
        return dailyRoutineJpaRepository.findAllByUserIdAndRoutineDateBetween(userId, start, end)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DailyRoutine> findReadyDueForNotification(Instant now) {
        return dailyRoutineJpaRepository
                .findAllByStatusAndNotificationScheduledAtIsNotNullAndNotificationScheduledAtLessThanEqual(
                        RoutineStatus.READY, now)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<RoutineItem> findAllByRoutineIds(List<Long> routineIds) {
        if (routineIds.isEmpty()) return List.of();
        return routineItemJpaRepository.findAllByRoutineIdIn(routineIds)
                .stream()
                .map(e -> new RoutineItem(e.getId(), e.getRoutineId(), e.getTimeSlot(), e.getCategory(),
                        e.getTitle(), e.getDetail(), e.getEffectCode(), e.getExpectedEffect(), e.getSortOrder(), e.isCompleted(), e.getCompletedAt(),
                        e.getCreatedAt(), e.getUpdatedAt()))
                .toList();
    }

    @Override public DailyRoutine save(DailyRoutine r){return toDomain(dailyRoutineJpaRepository.save(new DailyRoutineJpaEntity(r.id(),r.userId(),r.weatherSnapshotId(),r.routineDate(),r.status(),r.directionText(),r.homeComment(),r.difficultySnapshot(),r.timePreferenceSnapshot(),r.personalizationSnapshot(),r.performanceSnapshot(),r.generationErrorCode(),r.aiModel(),r.promptVersion(),r.generatedAt(),r.notificationScheduledAt(),r.createdAt(),r.updatedAt())));}
    @Override public List<RoutineItem> saveAll(List<RoutineItem> items){return items.stream().map(this::save).toList();}
    @Override public Optional<WeatherSnapshot> findWeatherById(Long id){return weatherSnapshotJpaRepository.findById(id).map(this::toDomain);}
    @Override public WeatherSnapshot save(WeatherSnapshot w){return toDomain(weatherSnapshotJpaRepository.save(new WeatherSnapshotJpaEntity(w.id(),w.userId(),w.targetDate(),w.regionSido(),w.regionSigungu(),w.latitude(),w.longitude(),w.temperature(),w.uvIndex(),w.weatherCondition(),w.provider(),w.observedAt(),w.createdAt())));}
    private DailyRoutine toDomain(DailyRoutineJpaEntity e){return new DailyRoutine(e.getId(),e.getUserId(),e.getWeatherSnapshotId(),e.getRoutineDate(),e.getStatus(),e.getDirectionText(),e.getHomeComment(),e.getDifficultySnapshot(),e.getTimePreferenceSnapshot(),e.getPersonalizationSnapshot(),e.getPerformanceSnapshot(),e.getGenerationErrorCode(),e.getAiModel(),e.getPromptVersion(),e.getGeneratedAt(),e.getNotificationScheduledAt(),e.getCreatedAt(),e.getUpdatedAt());}
    private WeatherSnapshot toDomain(WeatherSnapshotJpaEntity e){return new WeatherSnapshot(e.getId(),e.getUserId(),e.getTargetDate(),e.getRegionSido(),e.getRegionSigungu(),e.getLatitude(),e.getLongitude(),e.getTemperature(),e.getUvIndex(),e.getWeatherCondition(),e.getProvider(),e.getObservedAt(),e.getCreatedAt());}
}
