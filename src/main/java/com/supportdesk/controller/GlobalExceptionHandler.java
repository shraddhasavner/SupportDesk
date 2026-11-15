package com.supportdesk.controller;

import com.supportdesk.service.DuplicateEmailException;
import com.supportdesk.service.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global Exception Handler: Intercepts exceptions thrown across controllers
 * and transforms them into standard, readable JSON error responses.
 * 
 * [English]:
 * @RestControllerAdvice: Applies to all @RestController classes.
 * @ExceptionHandler: Defines which exception class is handled by each method.
 * 
 * [Hinglish]:
 * Jab controller ya service me koi error aata hai, yeh class use catch karti hai
 * aur client (browser/Postman) ko saaf aur readable JSON error response bhejti hai.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Helper to build uniform JSON error response structures.
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handles 404 Not Found when an entity ID doesn't exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles 409 Conflict when an email is already registered.
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles 400 Bad Request for bean validation failures (@Valid annotations like @NotBlank, @Email).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.put("message", "Validation failed for one or more fields");
        error.put("validationErrors", fieldErrors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles 401 Unauthorized for bad login credentials or inactive account.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Handles 401 Unauthorized for general authentication failures.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Full authentication is required to access this resource.";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, message);
    }

    /**
     * Handles 403 Forbidden when an authenticated user lacks required permissions or fails ownership check.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : "You do not have permission to access this resource.";
        return buildErrorResponse(HttpStatus.FORBIDDEN, message);
    }

    /**
     * Handles generic illegal arguments and illegal states (e.g. self-deactivation or last admin lockout).
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles 400 Bad Request when JSON payload in request body cannot be parsed or read.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid request body.");
    }

    /**
     * Handles 400 Bad Request when URL path variables or request parameters fail type conversion (e.g. string to Long).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "valid format";
        String message = "Invalid value for parameter '" + ex.getName() + "'. Expected type: " + expectedType + ".";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles 400 Bad Request when required query parameters are missing.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String message = "Required request parameter '" + ex.getParameterName() + "' is missing.";
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles 405 Method Not Allowed when an endpoint is requested with an unsupported HTTP verb.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.";
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    /**
     * Handles 415 Unsupported Media Type when request Content-Type is not supported.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        String contentType = ex.getContentType() != null ? ex.getContentType().toString() : "specified";
        String message = "Content type '" + contentType + "' is not supported.";
        return buildErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }

    /**
     * Handles 404 Not Found when a static resource or unmapped endpoint handler is not found.
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(Exception ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "The requested resource was not found.");
    }

    /**
     * Handles 409 Conflict for database integrity constraint violations (foreign key or unique constraints).
     * Prevents internal SQL syntax and table schema names from leaking to the client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.error("Database constraint violation occurred: ", ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "Database constraint violation: The operation could not be completed due to conflicting data.");
    }

    /**
     * Handles Spring web ResponseStatusException, preserving custom status codes and reasons.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return buildErrorResponse(status, message);
    }

    /**
     * Safe fallback handler for all unexpected server-side exceptions (500 Internal Server Error).
     * Logs the full exception details on the server while returning a sanitized client response
     * that never exposes stack traces, SQL, file paths, credentials, or internal class names.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        // Defensive check: if security or business exceptions reach here, delegate to specific handlers
        if (ex instanceof AccessDeniedException accessDeniedEx) {
            return handleAccessDenied(accessDeniedEx);
        }
        if (ex instanceof AuthenticationException authEx) {
            return handleAuthentication(authEx);
        }
        if (ex instanceof ResourceNotFoundException notFoundEx) {
            return handleResourceNotFound(notFoundEx);
        }
        if (ex instanceof DuplicateEmailException duplicateEmailEx) {
            return handleDuplicateEmail(duplicateEmailEx);
        }
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            return handleBadRequest((RuntimeException) ex);
        }

        logger.error("Unhandled server exception encountered: ", ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal server error occurred. Please contact support or try again later.");
    }
}
