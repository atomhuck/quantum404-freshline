package ru.quantum404.freshline.app.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.quantum404.freshline.app.api.ApiProblemWriter;

@Configuration
public class IdempotencyKeyWebConfiguration implements WebMvcConfigurer {
    private final ApiProblemWriter problemWriter;

    public IdempotencyKeyWebConfiguration(ApiProblemWriter problemWriter) {
        this.problemWriter = problemWriter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IdempotencyKeyInterceptor(problemWriter))
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/session");
    }
}
