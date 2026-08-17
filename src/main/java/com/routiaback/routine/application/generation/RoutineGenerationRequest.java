package com.routiaback.routine.application.generation;

import com.routiaback.personalization.application.result.NeedsResult;
import com.routiaback.personalization.application.result.ProfileResult;
import java.time.LocalDate;
import java.util.Map;

public record RoutineGenerationRequest(LocalDate routineDate, ProfileResult profile, NeedsResult needs,
        Map<String, String> bodyConcernNames, Map<String, String> skinConcernNames,
        WeatherInput weather, PerformanceInput performance) {
    public record WeatherInput(double temperature, double feelsLike, int weatherCode,
            String weatherCondition, double uvIndex) { }
    public record PerformanceInput(Integer previousCompleted, Integer previousTotal, Double previousRate,
            java.util.List<String> incompleteCategories, java.util.List<String> incompleteTimeSlots,
            double recentAverageRate, double recentAverageCompleted, java.util.Map<String, Double> categoryRates,
            java.util.Map<String, Double> timeSlotRates, String weakTimeSlot, String strongTimeSlot) { }
}
