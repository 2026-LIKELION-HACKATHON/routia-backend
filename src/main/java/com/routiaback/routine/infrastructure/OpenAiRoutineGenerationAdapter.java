package com.routiaback.routine.infrastructure;

import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.routine.application.generation.AiRoutineGenerationPort;
import com.routiaback.routine.application.generation.GeneratedRoutine;
import com.routiaback.routine.application.generation.RoutineGenerationRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
@ConditionalOnProperty(name = "routia.ai.provider", havingValue = "openai")
public class OpenAiRoutineGenerationAdapter implements AiRoutineGenerationPort {

    private static final String RESPONSES_PATH = "/v1/responses";

    private final ObjectMapper objectMapper;
    private final PromptTemplateLoader prompt;
    private final HttpClient httpClient;
    private final URI responsesUri;
    private final String apiKey;
    private final String model;
    private final String reasoningEffort;
    private final int maxOutputTokens;
    private final Duration requestTimeout;

    @Autowired
    public OpenAiRoutineGenerationAdapter(
            ObjectMapper objectMapper,
            PromptTemplateLoader prompt,
            @Value("${routia.ai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${routia.ai.api-key:}") String apiKey,
            @Value("${routia.ai.model:}") String model,
            @Value("${routia.ai.reasoning-effort:low}") String reasoningEffort,
            @Value("${routia.ai.max-output-tokens:3000}") int maxOutputTokens,
            @Value("${routia.ai.timeout-seconds:30}") long timeoutSeconds) {
        this(
                objectMapper,
                prompt,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                        .build(),
                responsesUri(baseUrl),
                apiKey,
                model,
                reasoningEffort,
                maxOutputTokens,
                Duration.ofSeconds(timeoutSeconds));
    }

    OpenAiRoutineGenerationAdapter(
            ObjectMapper objectMapper,
            PromptTemplateLoader prompt,
            HttpClient httpClient,
            URI responsesUri,
            String apiKey,
            String model,
            String reasoningEffort,
            int maxOutputTokens,
            Duration requestTimeout) {
        this.objectMapper = objectMapper;
        this.prompt = prompt;
        this.httpClient = httpClient;
        this.responsesUri = responsesUri;
        this.apiKey = apiKey;
        this.model = model;
        this.reasoningEffort = reasoningEffort;
        this.maxOutputTokens = maxOutputTokens;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public GeneratedRoutine generate(RoutineGenerationRequest request) {
        validateConfiguration();

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(responsesUri)
                    .timeout(requestTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(request)))
                    .build();
            HttpResponse<String> response = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(ErrorCode.ROUTINE_GENERATION_FAILED);
            }
            return parseResponse(response.body());
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.ROUTINE_GENERATION_FAILED, exception);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ApiException(ErrorCode.ROUTINE_GENERATION_FAILED, exception);
        }
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public String promptVersion() {
        return prompt.version();
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank() || model == null || model.isBlank()) {
            throw new ApiException(ErrorCode.AI_PROVIDER_NOT_CONFIGURED);
        }
        if (maxOutputTokens <= 0 || requestTimeout.isZero() || requestTimeout.isNegative()) {
            throw new ApiException(ErrorCode.AI_PROVIDER_NOT_CONFIGURED);
        }
    }

    private String requestBody(RoutineGenerationRequest request) throws JacksonException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("instructions", prompt.template());
        root.put("input", objectMapper.writeValueAsString(request));
        root.putObject("reasoning").put("effort", reasoningEffort);
        root.put("max_output_tokens", maxOutputTokens);

        ObjectNode format = root.putObject("text").putObject("format");
        format.put("type", "json_schema");
        format.put("name", "routia_daily_routine");
        format.put("strict", true);
        format.set("schema", responseSchema());
        return objectMapper.writeValueAsString(root);
    }

    private ObjectNode responseSchema() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "object");
        root.put("additionalProperties", false);
        root.set("required", stringArray("directionText", "homeComment", "items"));

        ObjectNode properties = root.putObject("properties");
        properties.set("directionText", stringSchema());
        properties.set("homeComment", stringSchema());

        ObjectNode items = properties.putObject("items");
        items.put("type", "array");
        items.put("minItems", 1);
        items.put("maxItems", 12);

        ObjectNode item = items.putObject("items");
        item.put("type", "object");
        item.put("additionalProperties", false);
        item.set("required", stringArray(
                "timeSlot", "category", "title", "detail", "effectCode", "expectedEffect"));

        ObjectNode itemProperties = item.putObject("properties");
        itemProperties.set("timeSlot", enumSchema("MORNING", "AFTERNOON", "EVENING", "BEDTIME"));
        itemProperties.set("category", enumSchema("SKIN", "BODY", "LIFESTYLE"));
        itemProperties.set("title", stringSchema());
        itemProperties.set("detail", stringSchema());
        itemProperties.set("effectCode", stringSchema());
        itemProperties.set("expectedEffect", stringSchema());
        return root;
    }

    private ObjectNode stringSchema() {
        return objectMapper.createObjectNode().put("type", "string");
    }

    private ObjectNode enumSchema(String... values) {
        ObjectNode schema = stringSchema();
        schema.set("enum", stringArray(values));
        return schema;
    }

    private ArrayNode stringArray(String... values) {
        ArrayNode array = objectMapper.createArrayNode();
        List.of(values).forEach(array::add);
        return array;
    }

    private GeneratedRoutine parseResponse(String responseBody) {
        try {
            JsonNode response = objectMapper.readTree(responseBody);
            if (!"completed".equals(response.path("status").asString())) {
                throw new ApiException(ErrorCode.AI_RESPONSE_INVALID);
            }

            for (JsonNode output : response.path("output")) {
                for (JsonNode content : output.path("content")) {
                    if ("output_text".equals(content.path("type").asString())) {
                        String text = content.path("text").asString();
                        if (!text.isBlank()) {
                            return objectMapper.readValue(text, GeneratedRoutine.class);
                        }
                    }
                }
            }
            throw new ApiException(ErrorCode.AI_RESPONSE_INVALID);
        } catch (ApiException exception) {
            throw exception;
        } catch (JacksonException exception) {
            throw new ApiException(ErrorCode.AI_RESPONSE_INVALID, exception);
        }
    }

    private static URI responsesUri(String baseUrl) {
        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return URI.create(normalized + RESPONSES_PATH);
    }
}
