package com.mentortrack.exception;

import com.mentortrack.ocr.OcrUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(OcrUnavailableException.class)
    public ResponseEntity<Object> handleOcrUnavailable(OcrUnavailableException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * NOTE on why there is no {@code @ExceptionHandler(Error.class)} here: Spring's
     * DispatcherServlet.doDispatch() catches raw {@link Throwable}s (including {@link Error}s such
     * as Tess4j/Tesseract's native "Invalid memory access" failure) and wraps them into a plain
     * {@code Exception} with the message "Handler dispatch failed: ..." *before* the
     * {@code @ExceptionHandler} resolver chain ever runs — which is exactly the confusing message
     * this app used to surface to the client. Because of that wrapping, an {@code Error.class}
     * handler here would never actually be invoked; it would be dead code. The real fix has to stop
     * the Error before it leaves application code in the first place — see
     * {@code OcrService.extractText}, which now catches {@link Throwable} around the native
     * Tesseract call specifically and converts it into a clean {@link OcrUnavailableException}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    private ResponseEntity<Object> build(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
