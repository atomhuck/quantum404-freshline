package ru.quantum404.freshline.app.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiProblem(
        String code,
        String message,
        String requestId,
        Map<String, Object> details
) {
}
