package com.corewise.modernization.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Talks to Anthropic's Claude. Active when app.ai.provider is "claude" (the default).
 * It only knows how to make the call and read the reply; retries live in AiClient.
 */
public class ClaudeProvider implements AiProvider {

    private final AiProperties.Vendor config;
    private final RestClient http;

    public ClaudeProvider(AiProperties properties) {
        this.config = properties.claude();
        this.http = RestClient.builder()
            .baseUrl(config.baseUrl())
            .requestFactory(timeoutFactory(properties))
            .build();
    }

    @Override
    public String name() {
        return "claude";
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new AiException("The AI is not set up yet. Add the Claude API key in config.", false);
        }
        Map<String, Object> body = Map.of(
            "model", config.model(),
            "max_tokens", 1024,
            "system", systemPrompt,
            "messages", List.of(Map.of("role", "user", "content", userPrompt)));
        try {
            JsonNode response = http.post()
                .uri("/v1/messages")
                .header("x-api-key", config.apiKey())
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
            return extractText(response);
        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            throw new AiException("The AI service returned an error (HTTP " + status + ").",
                status == 429 || status >= 500);
        } catch (ResourceAccessException e) {
            throw new AiException("The AI service timed out or could not be reached.", e, true);
        }
    }

    private String extractText(JsonNode response) {
        // Claude replies with { "content": [ { "type": "text", "text": "..." } ] }.
        if (response != null) {
            JsonNode content = response.path("content");
            if (content.isArray()) {
                StringBuilder text = new StringBuilder();
                for (JsonNode part : content) {
                    if ("text".equals(part.path("type").asText())) {
                        text.append(part.path("text").asText());
                    }
                }
                String result = text.toString().trim();
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        throw new AiException("The AI replied in a form we did not understand.", false);
    }

    private SimpleClientHttpRequestFactory timeoutFactory(AiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int millis = (int) Math.min(properties.timeout().toMillis(), Integer.MAX_VALUE);
        factory.setConnectTimeout(millis);
        factory.setReadTimeout(millis);
        return factory;
    }
}
