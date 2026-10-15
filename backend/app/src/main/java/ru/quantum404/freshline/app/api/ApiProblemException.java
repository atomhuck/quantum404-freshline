package ru.quantum404.freshline.app.api;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiProblemException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final String publicMessage;
    private final Map<String, Object> details;

    public ApiProblemException(HttpStatus status, String code, String publicMessage) {
        this(status, code, publicMessage, null);
    }

    public ApiProblemException(HttpStatus status, String code, String publicMessage, Map<String, Object> details) {
        super(publicMessage);
        this.status = status;
        this.code = code;
        this.publicMessage = publicMessage;
        this.details = details;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String publicMessage() {
        return publicMessage;
    }

    public Map<String, Object> details() {
        return details;
    }
}
