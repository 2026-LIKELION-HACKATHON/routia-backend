package com.routiaback.routine.application.generation;

import com.routiaback.personalization.application.result.NeedsResult;
import com.routiaback.personalization.application.result.ProfileResult;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.AgeGroup;

public record RoutineGenerationRequest(LocalDate routineDate, ProfileInput profile, NeedsResult needs,
        Map<String, String> bodyGoalNames, Map<String, String> bodyConcernNames,
        Map<String, String> skinConcernNames, Map<String, String> ownedToolNames,
        DistributionInput targetDistribution, WeatherInput weather, PerformanceInput performance) {
    public RoutineGenerationRequest(LocalDate routineDate, ProfileResult profile, NeedsResult needs,
            Map<String, String> bodyConcernNames, Map<String, String> skinConcernNames,
            WeatherInput weather, PerformanceInput performance) {
        this(routineDate, profile == null ? null : ProfileInput.from(profile), needs, Map.of(), bodyConcernNames, skinConcernNames, Map.of(),
                needs == null || needs.routineDifficulty() == null || needs.routineTimePreference() == null
                        ? null : RoutineDistributionPolicy.calculate(
                                needs.routineDifficulty(), needs.routineTimePreference()),
                weather, performance);
    }
    public record ProfileInput(BigDecimal height, BigDecimal weight, Gender gender, AgeGroup ageGroup) {
        static ProfileInput from(ProfileResult profile) {
            return new ProfileInput(profile.height(), profile.weight(), profile.gender(), profile.ageGroup());
        }
    }
    public record DistributionInput(int targetItemCount, Map<String, Integer> timeSlotTargets) {
        public DistributionInput { timeSlotTargets = Map.copyOf(timeSlotTargets); }
    }
    public record WeatherInput(double temperature, double feelsLike, int weatherCode,
            String weatherCondition, double uvIndex) { }
    public record PerformanceInput(Integer previousCompleted, Integer previousTotal, Double previousRate,
            List<String> incompleteCategories, List<String> incompleteTimeSlots,
            double recentAverageRate, double recentAverageCompleted, java.util.Map<String, Double> categoryRates,
            java.util.Map<String, Double> timeSlotRates, String weakTimeSlot, String strongTimeSlot) { }
}
