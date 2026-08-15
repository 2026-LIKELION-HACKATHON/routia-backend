package com.routiaback.routine.infrastructure;

import com.routiaback.routine.application.generation.*;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="routia.ai.provider",havingValue="stub")
public class StubAiRoutineGenerationAdapter implements AiRoutineGenerationPort {
    private final PromptTemplateLoader prompt;
    public StubAiRoutineGenerationAdapter(PromptTemplateLoader prompt){this.prompt=prompt;}
    @Override public GeneratedRoutine generate(RoutineGenerationRequest request){return new GeneratedRoutine(
            "오늘의 컨디션과 날씨에 맞춰 무리 없이 관리해 보세요.",
            "작은 실천을 꾸준히 이어가는 하루를 만들어 보세요.",
            List.of(new GeneratedRoutine.GeneratedItem("MORNING","LIFESTYLE","물 한 잔으로 하루 시작하기","기상 후 천천히 물 한 잔을 마셔 수분을 보충하세요.","HYDRATION","수분 보충에 도움")));}
    @Override public String model(){return "stub-routine-model";} @Override public String promptVersion(){return prompt.version();}
}
