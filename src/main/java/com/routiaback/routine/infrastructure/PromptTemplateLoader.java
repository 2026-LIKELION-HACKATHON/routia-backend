package com.routiaback.routine.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class PromptTemplateLoader {
    private final String template; private final String version;
    public PromptTemplateLoader(@Value("${routia.ai.prompt-resource:classpath:prompts/routine-v2.txt}") Resource resource,
            @Value("${routia.ai.prompt-version:routine-v2-onboarding-v2}") String version) throws IOException {
        this.template=resource.getContentAsString(StandardCharsets.UTF_8); this.version=version;
    }
    public String template(){return template;} public String version(){return version;}
}
