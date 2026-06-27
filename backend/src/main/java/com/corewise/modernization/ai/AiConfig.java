package com.corewise.modernization.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the one AI provider the config asks for.
 *
 * We pick the provider here, from the bound settings, instead of with a bean
 * condition. That keeps it predictable: app.ai.provider is read the same way as
 * every other setting, including from the local config file, so what you set is
 * what runs.
 */
@Configuration
public class AiConfig {

    @Bean
    AiProvider aiProvider(AiProperties properties) {
        if ("openai".equalsIgnoreCase(properties.provider())) {
            return new OpenAiProvider(properties);
        }
        return new ClaudeProvider(properties);
    }
}
