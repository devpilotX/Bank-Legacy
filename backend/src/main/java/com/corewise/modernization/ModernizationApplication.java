package com.corewise.modernization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the modernization backend.
 *
 * This is the internal API our team uses to read old COBOL, explain it with AI,
 * map how the parts connect, and help rewrite pieces into Java. Only our team
 * uses it.
 */
@SpringBootApplication
public class ModernizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModernizationApplication.class, args);
    }

}
