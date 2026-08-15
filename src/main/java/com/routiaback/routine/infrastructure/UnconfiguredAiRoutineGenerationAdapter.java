package com.routiaback.routine.infrastructure;

import com.routiaback.global.error.*;
import com.routiaback.routine.application.generation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component @ConditionalOnMissingBean(AiRoutineGenerationPort.class)
public class UnconfiguredAiRoutineGenerationAdapter implements AiRoutineGenerationPort {
    private final PromptTemplateLoader prompt;
    public UnconfiguredAiRoutineGenerationAdapter(PromptTemplateLoader prompt){this.prompt=prompt;}
    @Override public GeneratedRoutine generate(RoutineGenerationRequest request){throw new ApiException(ErrorCode.AI_PROVIDER_NOT_CONFIGURED);}
    @Override public String model(){return "UNCONFIGURED";} @Override public String promptVersion(){return prompt.version();}
}
