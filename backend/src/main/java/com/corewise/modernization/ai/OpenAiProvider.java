package com.corewise.modernization.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Talks to OpenAI's GPT. Active when app.ai.provider is "openai". Same shape as the
 * Claude provider so the two are interchangeable; only the request and reply format
 * differ.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiProvider implements AiProvider {

    private final AiProperties.Vendor config;
    private final RestClient http;

    public OpenAiProvider(AiProperties properties) {
        this.config = properties.openai();
        this.http = RestClient.builder()
            .baseUrl(config.baseUrl())
            .requestFactory(timeoutFactory(properties))
            .build();
    }

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (config.apiKey() == null || config.apiKey().isBlank()) {
            throw new AiException("The AI is not set up yet. Add the OpenAI API key in config.", false);
        }
        Map<String, Object> body = Map.of(
            "model", config.model(),
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        try {
            JsonNode response = http.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + config.apiKey())
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
        // GPT replies with { "choices": [ { "message": { "content": "..." } } ] }.
        if (response != null) {
            JsonNode choices = response.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                String result = choices.get(0).path("message").path("content").asText("").trim();
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
