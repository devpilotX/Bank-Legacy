package com.corewise.modernization.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Turns on the typed config properties that live in the config package. */
@Configuration
@EnableConfigurationProperties({BootstrapProperties.class, StorageProperties.class})
public class AppPropertiesConfig {
}
