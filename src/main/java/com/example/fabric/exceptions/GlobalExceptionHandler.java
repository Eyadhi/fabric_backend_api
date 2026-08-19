package com.example.fabric.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.fabric.model.ApiResponse;
import com.example.fabric.util.ResponseUtil;

/**
 * Centralized exception handler for all controllers.
 *
 * Before: every controller method had its own try/catch block returning
 *         inconsistent error shapes.
 *
 * After:  controllers contain zero try/catch — they just call the service
 *         and return the result. Any exception bubbles up here and gets a
 *         consistent ApiResponse shape with the right HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 404 — resource does not exist
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseUtil.createErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 409 — uniqueness / duplicate constraint violated
    // -------------------------------------------------------------------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicate(DuplicateResourceException ex) {
        return ResponseUtil.createErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 400 — caller sent invalid / incomplete data
    // -------------------------------------------------------------------------

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException ex) {
        return ResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    /**
     * Handles @Valid / @Validated bean validation failures.
     * Collects every field error into a map so the caller knows exactly
     * which fields are wrong.
     *   e.g. { "name": "must not be blank", "meters": "must be > 0" }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("fields", fieldErrors);

        ApiResponse<Map<String, Object>> response =
                new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), body);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing required @RequestParam.
     * e.g. calling /getWorker without the required query param.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return ResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * Handles wrong type for a @RequestParam / @PathVariable.
     * e.g. passing "abc" for a Long id.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Parameter '" + ex.getName() + "' has an invalid value: '" + ex.getValue() + "'";
        return ResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    }

    // -------------------------------------------------------------------------
    // 422 — business rule violation
    // -------------------------------------------------------------------------

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessRule(BusinessException ex) {
        return ResponseUtil.createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 403 — security / access denied
    // -------------------------------------------------------------------------

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseUtil.createErrorResponse(HttpStatus.FORBIDDEN.value(),
                "Access denied. You do not have permission to perform this action.");
    }

    // -------------------------------------------------------------------------
    // 500 — file processing error
    // -------------------------------------------------------------------------

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ApiResponse<?>> handleFileProcessing(FileProcessingException ex) {
        return ResponseUtil.createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 500 — catch-all for anything unexpected
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        // Log the real stack trace here in production — don't expose it to callers
        return ResponseUtil.createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.");
    }
}
