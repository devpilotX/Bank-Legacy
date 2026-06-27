package com.corewise.modernization.config;

import com.corewise.modernization.ai.AiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Turns on the typed config properties that live in the config and ai packages. */
@Configuration
@EnableConfigurationProperties({BootstrapProperties.class, StorageProperties.class, AiProperties.class})
public class AppPropertiesConfig {
}
