package com.corewise.modernization.config;

import com.corewise.modernization.ai.AiProperties;
import com.corewise.modernization.verify.VerifyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Turns on the typed config properties that live in the config, ai, and verify packages. */
@Configuration
@EnableConfigurationProperties({
  BootstrapProperties.class,
  StorageProperties.class,
  AiProperties.class,
  VerifyProperties.class
})
public class AppPropertiesConfig {
}
