package com.corewise.modernization.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * The single shape we send back when something goes wrong. It stays plain on
 * purpose: a status, a short label, a message a person can read, and, for bad
 * input, which fields need fixing. We never put stack traces or internal detail
 * in here.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
    OffsetDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldProblem> fieldErrors
) {
    /** One field that did not pass validation, and why, in plain words. */
    public record FieldProblem(String field, String message) {}

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<FieldProblem> fieldErrors) {
        return new ApiError(OffsetDateTime.now(), status, error, message, path, fieldErrors);
    }
}
