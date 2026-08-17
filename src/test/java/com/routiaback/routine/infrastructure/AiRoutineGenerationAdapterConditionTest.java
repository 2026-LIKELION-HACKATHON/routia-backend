package com.routiaback.routine.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.routine.application.generation.AiRoutineGenerationPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.ObjectMapper;

class AiRoutineGenerationAdapterConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    PromptTemplateLoader.class,
                    OpenAiRoutineGenerationAdapter.class,
                    StubAiRoutineGenerationAdapter.class,
                    UnconfiguredAiRoutineGenerationAdapter.class)
            .withBean(ObjectMapper.class, ObjectMapper::new);

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

    @Test
    void registersOpenAiAdapterWhenOpenAiProviderIsConfigured() {
        contextRunner
                .withPropertyValues(
                        "routia.ai.provider=openai",
                        "routia.ai.api-key=test-key",
                        "routia.ai.model=gpt-5.6-luna")
                .run(context -> {
                    assertThat(context).hasSingleBean(AiRoutineGenerationPort.class);
                    assertThat(context.getBean(AiRoutineGenerationPort.class))
                            .isInstanceOf(OpenAiRoutineGenerationAdapter.class);
                });
    }
}
