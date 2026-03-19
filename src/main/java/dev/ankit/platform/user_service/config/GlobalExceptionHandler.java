package dev.ankit.platform.user_service.config;


import dev.ankit.platform.user_service.exception.BusinessException;
import dev.ankit.platform.user_service.exception.ErrorResponse;
import dev.ankit.platform.user_service.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {

        log.warn("Resource not found path={}, message={}", req.getRequestURI(), ex.getMessage());

        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest req) {

        log.warn("Business exception path={}, message={}", req.getRequestURI(), ex.getMessage());

        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest req) {

        log.warn("Validation failed path={}, error={}", req.getRequestURI(), ex.getMessage());

        return build(HttpStatus.BAD_REQUEST, "Validation failed", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {

        log.error("Unhandled exception path={}, error={}", req.getRequestURI(), ex.getMessage(), ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
    }

    // 🔥 Common builder (IMPORTANT)
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String msg, HttpServletRequest req) {

        String traceId = org.slf4j.MDC.get("traceId");

        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .message(msg)
                        .path(req.getRequestURI())
                        .status(status.value())
                        .timestamp(OffsetDateTime.now())
                        .traceId(traceId)
                        .build()
        );
    }
}