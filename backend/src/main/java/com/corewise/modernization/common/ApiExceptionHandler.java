package com.corewise.modernization.common;

import com.corewise.modernization.ai.AiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Turns exceptions into clean JSON the frontend can rely on. The rule here is
 * simple: tell the caller what they can act on, in plain words, and keep our
 * internal details and stack traces on our side, in the logs.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "conflict", ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldProblem> fields = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new ApiError.FieldProblem(
                fe.getField(),
                fe.getDefaultMessage() == null ? "is not valid" : fe.getDefaultMessage()))
            .toList();
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "validation_failed",
            "Some fields need fixing.", request.getRequestURI(), fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.FieldProblem> fields = ex.getConstraintViolations().stream()
            .map(v -> new ApiError.FieldProblem(v.getPropertyPath().toString(), v.getMessage()))
            .toList();
        ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "validation_failed",
            "Some fields need fixing.", request.getRequestURI(), fields);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "upload_too_large",
            "That upload is larger than we allow. Try a smaller file or split it up.", request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "unauthorized", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "forbidden", "You do not have access to do that.", request);
    }

    @ExceptionHandler(AiException.class)
    public ResponseEntity<ApiError> handleAi(AiException ex, HttpServletRequest request) {
        // The AI is a service we do not control, so a failure there is a bad gateway,
        // not our internal error. The message is already plain enough to show.
        return build(HttpStatus.BAD_GATEWAY, "ai_error", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleEverythingElse(Exception ex, HttpServletRequest request) {
        // Something we did not plan for. Log the real detail for us, and tell the
        // caller only that it failed. No internals or stack traces go out.
        log.error("Unhandled error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
            "Something went wrong on our side. We have logged it.", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message,
                                           HttpServletRequest request) {
        ApiError body = ApiError.of(status.value(), error,
            message == null ? status.getReasonPhrase() : message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
