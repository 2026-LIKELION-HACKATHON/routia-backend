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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@EnabledIf("liveTestEnabled")
class OpenAiRoutineGenerationAdapterLiveTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final RoutineGenerationValidator validator = new RoutineGenerationValidator();

    @Test
    void generatesUxAlignedRoutinesForFourRepresentativeCases() throws Exception {
        OpenAiRoutineGenerationAdapter adapter = adapter();

        for (LiveCase liveCase : cases()) {
            GeneratedRoutine result = adapter.generate(liveCase.request());

            validator.validate(result, liveCase.difficulty(), liveCase.request().targetDistribution());
            assertThat(result.items()).hasSize(liveCase.targetCount());
            assertThat(result.items()).allSatisfy(item -> {
                assertThat(item.title()).isNotBlank();
                assertThat(item.detail()).isNotBlank();
                assertThat(item.effectCode()).isNotBlank();
                assertThat(item.expectedEffect()).isNotBlank();
            });
            assertPreferenceDistribution(liveCase, result);
            System.out.println("LIVE_OPENAI_" + liveCase.name() + "=" + report(result));
        }
    }

    private OpenAiRoutineGenerationAdapter adapter() throws Exception {
        String model = setting("AI_MODEL", "gpt-5.6-luna");
        return new OpenAiRoutineGenerationAdapter(
                objectMapper,
                new PromptTemplateLoader(new ClassPathResource("prompts/routine-v2.txt"), "routine-v2-onboarding-v2"),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(),
                URI.create("https://api.openai.com/v1/responses"),
                setting("OPENAI_API_KEY", ""),
                model,
                "low",
                3_000,
                Duration.ofSeconds(60));
    }

    static boolean liveTestEnabled() {
        return Boolean.parseBoolean(System.getenv("RUN_OPENAI_LIVE_TEST"));
    }

    private String setting(String name, String defaultValue) throws Exception {
        String environmentValue = System.getenv(name);
        if (environmentValue != null && !environmentValue.isBlank()) return environmentValue;
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) return defaultValue;
        return Files.readAllLines(envFile).stream()
                .filter(line -> line.startsWith(name + "="))
                .map(line -> line.substring(name.length() + 1))
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElse(defaultValue);
    }

    private void assertPreferenceDistribution(LiveCase liveCase, GeneratedRoutine result) {
        Map<String, Long> counts = result.items().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        GeneratedRoutine.GeneratedItem::timeSlot,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.counting()));
        if (liveCase.timePreference() == RoutineTimePreference.MORNING) {
            assertThat(counts.getOrDefault("MORNING", 0L))
                    .isGreaterThanOrEqualTo(Math.round(liveCase.targetCount() * 2.0 / 3.0) - 1);
        }
        if (liveCase.timePreference() == RoutineTimePreference.EVENING) {
            long eveningAndBedtime = counts.getOrDefault("EVENING", 0L)
                    + counts.getOrDefault("BEDTIME", 0L);
            assertThat(eveningAndBedtime)
                    .isGreaterThanOrEqualTo(Math.round(liveCase.targetCount() * 2.0 / 3.0) - 1);
            assertThat(eveningAndBedtime).isLessThan(liveCase.targetCount());
        }
    }

    private String report(GeneratedRoutine result) throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("distribution", result.items().stream().collect(java.util.stream.Collectors.groupingBy(
                GeneratedRoutine.GeneratedItem::timeSlot,
                LinkedHashMap::new,
                java.util.stream.Collectors.counting())));
        report.put("routine", result);
        return objectMapper.writeValueAsString(report);
    }

    private List<LiveCase> cases() {
        return List.of(
                new LiveCase("CASE_A_MORNING_HIGH_UV", RoutineDifficulty.SIMPLE,
                        RoutineTimePreference.MORNING, 8,
                        request(RoutineTimePreference.MORNING, highUvWeather(), averagePerformance())),
                new LiveCase("CASE_B_EVENING", RoutineDifficulty.SIMPLE,
                        RoutineTimePreference.EVENING, 8,
                        request(RoutineTimePreference.EVENING, mildWeather(), averagePerformance())),
                new LiveCase("CASE_C_LOW_PERFORMANCE", RoutineDifficulty.SIMPLE,
                        RoutineTimePreference.MORNING, 8,
                        request(RoutineTimePreference.MORNING, mildWeather(), lowPerformance())),
                new LiveCase("CASE_D_NEW_USER", RoutineDifficulty.SIMPLE,
                        RoutineTimePreference.ANY, 8,
                        request(RoutineTimePreference.ANY, mildWeather(), emptyPerformance())));
    }

    private RoutineGenerationRequest request(
            RoutineTimePreference timePreference,
            RoutineGenerationRequest.WeatherInput weather,
            RoutineGenerationRequest.PerformanceInput performance) {
        NeedsResult needs = new NeedsResult(
                BodyGoal.BUILD_HABIT,
                List.of(BodyGoal.BUILD_HABIT, BodyGoal.REGULAR_LIFE),
                List.of("FATIGUE", "SWELLING"),
                SkinType.COMBINATION,
                List.of("ELASTICITY", "PORE"),
                List.of("FACE_FASCIA_TOOL"),
                timePreference,
                RoutineDifficulty.SIMPLE);
        return new RoutineGenerationRequest(
                LocalDate.of(2026, 8, 17),
                new RoutineGenerationRequest.ProfileInput(
                        new BigDecimal("165.3"), new BigDecimal("55.2"), Gender.FEMALE, AgeGroup.TWENTIES),
                needs,
                Map.of("BUILD_HABIT", "생활 습관 형성", "REGULAR_LIFE", "규칙적인 생활"),
                Map.of("FATIGUE", "피로감", "SWELLING", "붓기"),
                Map.of("ELASTICITY", "탄력", "PORE", "모공"),
                Map.of("FACE_FASCIA_TOOL", "페이스 괄사"),
                com.routiaback.routine.application.generation.RoutineDistributionPolicy.calculate(
                        RoutineDifficulty.SIMPLE, timePreference),
                weather,
                performance);
    }

    private RoutineGenerationRequest.WeatherInput highUvWeather() {
        return new RoutineGenerationRequest.WeatherInput(31.0, 34.0, 0, "맑음", 8.5);
    }

    private RoutineGenerationRequest.WeatherInput mildWeather() {
        return new RoutineGenerationRequest.WeatherInput(24.0, 24.5, 2, "구름 조금", 3.0);
    }

    private RoutineGenerationRequest.PerformanceInput averagePerformance() {
        return new RoutineGenerationRequest.PerformanceInput(
                5, 8, 0.625, List.of("BODY"), List.of("EVENING"), 0.68, 5.4,
                Map.of("SKIN", 0.75, "BODY", 0.55, "LIFESTYLE", 0.7),
                Map.of("MORNING", 0.8, "AFTERNOON", 0.65, "EVENING", 0.5, "BEDTIME", 0.7),
                "EVENING", "MORNING");
    }

    private RoutineGenerationRequest.PerformanceInput lowPerformance() {
        return new RoutineGenerationRequest.PerformanceInput(
                2, 8, 0.25, List.of("BODY", "LIFESTYLE"), List.of("EVENING"), 0.31, 2.5,
                Map.of("SKIN", 0.5, "BODY", 0.2, "LIFESTYLE", 0.25),
                Map.of("MORNING", 0.65, "AFTERNOON", 0.3, "EVENING", 0.1, "BEDTIME", 0.2),
                "EVENING", "MORNING");
    }

    private RoutineGenerationRequest.PerformanceInput emptyPerformance() {
        return new RoutineGenerationRequest.PerformanceInput(
                null, null, null, List.of(), List.of(), 0.0, 0.0,
                Map.of(), Map.of(), null, null);
    }

    private record LiveCase(
            String name,
            RoutineDifficulty difficulty,
            RoutineTimePreference timePreference,
            int targetCount,
            RoutineGenerationRequest request) {
    }
}
