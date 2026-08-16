package com.routiaback.routine.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.routine.application.generation.GeneratedRoutine;
import com.routiaback.routine.application.generation.RoutineGenerationRequest;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenAiRoutineGenerationAdapterTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private HttpServer server;
    private URI responsesUri;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/responses", this::handle);
        server.start();
        responsesUri = URI.create("http://localhost:" + server.getAddress().getPort() + "/v1/responses");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void requestsStrictStructuredOutputAndParsesGeneratedRoutine() throws Exception {
        OpenAiRoutineGenerationAdapter adapter = adapter();

        GeneratedRoutine result = adapter.generate(request());

        assertThat(authorization.get()).isEqualTo("Bearer test-key");
        JsonNode body = objectMapper.readTree(requestBody.get());
        assertThat(body.path("model").asText()).isEqualTo("gpt-5.6-luna");
        assertThat(body.path("reasoning").path("effort").asText()).isEqualTo("low");
        assertThat(body.path("text").path("format").path("type").asText()).isEqualTo("json_schema");
        assertThat(body.path("text").path("format").path("strict").asBoolean()).isTrue();
        assertThat(body.path("text").path("format").path("schema").path("additionalProperties").asBoolean()).isFalse();
        assertThat(body.path("input").asText()).contains("2026-08-16");
        assertThat(result.directionText()).isEqualTo("가볍게 시작해요");
        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.timeSlot()).isEqualTo("MORNING");
            assertThat(item.category()).isEqualTo("LIFESTYLE");
            assertThat(item.title()).isEqualTo("물 한 잔 마시기");
        });
    }

    @Test
    void rejectsCompletedResponseWithoutOutputText() {
        server.removeContext("/v1/responses");
        server.createContext("/v1/responses", exchange -> respond(exchange, 200,
                "{\"status\":\"completed\",\"output\":[]}"));

        assertThatThrownBy(() -> adapter().generate(request()))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_INVALID));
    }

    @Test
    void rejectsIncompleteResponse() {
        server.removeContext("/v1/responses");
        server.createContext("/v1/responses", exchange -> respond(exchange, 200,
                "{\"status\":\"incomplete\",\"incomplete_details\":{\"reason\":\"max_output_tokens\"},\"output\":[]}"));

        assertThatThrownBy(() -> adapter().generate(request()))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AI_RESPONSE_INVALID));
    }

    @Test
    void mapsProviderHttpErrorWithoutLeakingResponseBody() {
        server.removeContext("/v1/responses");
        server.createContext("/v1/responses", exchange -> respond(exchange, 429,
                "{\"error\":{\"message\":\"secret provider detail\"}}"));

        assertThatThrownBy(() -> adapter().generate(request()))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROUTINE_GENERATION_FAILED);
                    assertThat(exception).hasMessageNotContaining("secret provider detail");
                });
    }

    private OpenAiRoutineGenerationAdapter adapter() {
        return new OpenAiRoutineGenerationAdapter(
                objectMapper,
                new PromptTemplateLoaderForTest("prompt", "routine-v1"),
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(),
                responsesUri,
                "test-key",
                "gpt-5.6-luna",
                "low",
                2_000,
                Duration.ofSeconds(2));
    }

    private RoutineGenerationRequest request() {
        return new RoutineGenerationRequest(
                LocalDate.of(2026, 8, 16), null, null,
                new RoutineGenerationRequest.WeatherInput(27.0, 29.0, 1, 5.2),
                null);
    }

    private void handle(HttpExchange exchange) throws IOException {
        authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        respond(exchange, 200, """
                {
                  "status": "completed",
                  "output": [{
                    "type": "message",
                    "content": [{
                      "type": "output_text",
                      "text": "{\\\"directionText\\\":\\\"가볍게 시작해요\\\",\\\"homeComment\\\":\\\"오늘도 응원해요\\\",\\\"items\\\":[{\\\"timeSlot\\\":\\\"MORNING\\\",\\\"category\\\":\\\"LIFESTYLE\\\",\\\"title\\\":\\\"물 한 잔 마시기\\\",\\\"detail\\\":\\\"천천히 마셔요\\\",\\\"effectCode\\\":\\\"HYDRATION\\\",\\\"expectedEffect\\\":\\\"수분 보충\\\"}]}"
                    }]
                  }]
                }
                """);
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static final class PromptTemplateLoaderForTest extends PromptTemplateLoader {
        private final String template;
        private final String version;

        private PromptTemplateLoaderForTest(String template, String version) {
            super(template, version);
            this.template = template;
            this.version = version;
        }

        @Override
        public String template() {
            return template;
        }

        @Override
        public String version() {
            return version;
        }
    }
}
