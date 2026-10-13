package ru.quantum404.freshline.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.quantum404.freshline.app.api.ApiProblemWriter;

import java.io.IOException;

final class IdempotencyKeyInterceptor implements HandlerInterceptor {
    static final String HEADER = "Idempotency-Key";
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private final ApiProblemWriter problemWriter;

    IdempotencyKeyInterceptor(ApiProblemWriter problemWriter) {
        this.problemWriter = problemWriter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod) || !isBusinessWrite(request.getMethod())) {
            return true;
        }

        String key = request.getHeader(HEADER);
        if (key == null) {
            problemWriter.write(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key is required");
            return false;
        }

        int length = key.codePointCount(0, key.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            problemWriter.write(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_IDEMPOTENCY_KEY", "Idempotency-Key must contain between 8 and 128 characters");
            return false;
        }

        return true;
    }

    private static boolean isBusinessWrite(String method) {
        return HttpMethod.POST.matches(method)
                || HttpMethod.PUT.matches(method)
                || HttpMethod.PATCH.matches(method)
                || HttpMethod.DELETE.matches(method);
    }
}
