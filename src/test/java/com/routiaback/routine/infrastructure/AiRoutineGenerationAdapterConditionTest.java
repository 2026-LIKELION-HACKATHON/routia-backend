package com.routiaback.routine.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.routine.application.generation.AiRoutineGenerationPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AiRoutineGenerationAdapterConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    PromptTemplateLoader.class,
                    StubAiRoutineGenerationAdapter.class,
                    UnconfiguredAiRoutineGenerationAdapter.class);

    @Test
    void registersUnconfiguredAdapterWhenProviderIsMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AiRoutineGenerationPort.class);
            assertThat(context.getBean(AiRoutineGenerationPort.class))
                    .isInstanceOf(UnconfiguredAiRoutineGenerationAdapter.class);
        });
    }

    @Test
    void registersStubAdapterWhenStubProviderIsConfigured() {
        contextRunner
                .withPropertyValues("routia.ai.provider=stub")
                .run(context -> {
                    assertThat(context).hasSingleBean(AiRoutineGenerationPort.class);
                    assertThat(context.getBean(AiRoutineGenerationPort.class))
                            .isInstanceOf(StubAiRoutineGenerationAdapter.class);
                });
    }
}
