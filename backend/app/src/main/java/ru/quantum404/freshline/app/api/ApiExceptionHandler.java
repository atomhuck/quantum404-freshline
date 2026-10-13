package ru.quantum404.freshline.app.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiProblemException.class)
    ResponseEntity<ApiProblem> handleApiProblem(ApiProblemException exception, HttpServletRequest request) {
        return response(exception.status(), exception.code(), exception.publicMessage(), exception.details(), request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ApiProblem> handleBinding(Exception exception, HttpServletRequest request) {
        BindException binding = (BindException) exception;
        Map<String, java.util.List<String>> validationErrors = binding.getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        TreeMap::new,
                        Collectors.mapping(error -> safeValidationMessage(error.getDefaultMessage()), Collectors.toList())
                ));
        Map<String, Object> details = new TreeMap<>();
        validationErrors.forEach(details::put);
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiProblem> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, Object> details = exception.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> safeValidationMessage(violation.getMessage()),
                        (first, ignored) -> first,
                        TreeMap::new
                ));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", details, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiProblem> handleMethodValidation(HandlerMethodValidationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", null, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiProblem> handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", "Request body is missing or invalid", null, request);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    ResponseEntity<ApiProblem> handleNotFound(Exception exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", "The requested resource was not found", null, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiProblem> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Unhandled API failure requestId={} exceptionType={}",
                RequestIdFilter.current(request), exception.getClass().getName());
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "The request could not be completed", null, request);
    }

    private ResponseEntity<ApiProblem> response(
            HttpStatus status,
            String code,
            String message,
            Map<String, Object> details,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status)
                .body(new ApiProblem(code, message, RequestIdFilter.current(request), details));
    }

    private static String safeValidationMessage(String message) {
        return message == null || message.isBlank() ? "invalid value" : message;
    }
}
