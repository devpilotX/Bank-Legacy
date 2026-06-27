package com.corewise.modernization.web.dto;

import jakarta.validation.constraints.NotBlank;

/** The engineer's reviewed Java for a work unit. */
public record SaveJavaRequest(
    @NotBlank(message = "is required") String java
) {
}
