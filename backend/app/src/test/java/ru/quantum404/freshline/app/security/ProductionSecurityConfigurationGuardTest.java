package ru.quantum404.freshline.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionSecurityConfigurationGuardTest {
    @Test
    void localProfileDoesNotRequireProductionSecrets() {
        assertDoesNotThrow(() -> ProductionSecurityConfigurationGuard.validate(new MockEnvironment()));
    }

    @Test
    void productionRejectsInsecureSessionCookie() {
        MockEnvironment environment = productionEnvironment()
                .withProperty("FRESHLINE_COOKIE_SECURE", "false");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ProductionSecurityConfigurationGuard.validate(environment));

        assertTrue(error.getMessage().contains("FRESHLINE_COOKIE_SECURE=true"));
    }

    @Test
    void productionRejectsMissingDatabasePassword() {
        MockEnvironment environment = productionEnvironment()
                .withProperty("spring.datasource.password", " ");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ProductionSecurityConfigurationGuard.validate(environment));

        assertTrue(error.getMessage().contains("SPRING_DATASOURCE_PASSWORD"));
    }

    @Test
    void productionRejectsDefaultDemoAccessCodeWithoutEchoingIt() {
        MockEnvironment environment = productionEnvironment()
                .withProperty("FRESHLINE_DEMO_ACCESS_CODE", "validation-only");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ProductionSecurityConfigurationGuard.validate(environment));

        assertTrue(error.getMessage().contains("FRESHLINE_DEMO_ACCESS_CODE"));
        assertFalse(error.getMessage().contains("validation-only"));
    }

    @Test
    void productionAcceptsExplicitSecretsAndSecureCookie() {
        MockEnvironment environment = productionEnvironment()
                .withProperty("FRESHLINE_COOKIE_SECURE", "true")
                .withProperty("spring.datasource.password", "configured-db-secret")
                .withProperty("FRESHLINE_DEMO_ACCESS_CODE", "configured-demo-code");

        assertDoesNotThrow(() -> ProductionSecurityConfigurationGuard.validate(environment));
    }

    @Test
    void productionMisconfigurationFailsContextRefresh() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("prod");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "FRESHLINE_COOKIE_SECURE", "false",
                "spring.datasource.password", "configured-db-secret",
                "DEMO_MODE", "true",
                "FRESHLINE_DEMO_ACCESS_CODE", "configured-demo-code"
        )));
        context.register(ProductionSecurityConfigurationGuard.class);

        try {
            IllegalStateException error = assertThrows(IllegalStateException.class, context::refresh);
            assertTrue(error.getMessage().contains("FRESHLINE_COOKIE_SECURE=true"));
        } finally {
            context.close();
        }
    }

    private static MockEnvironment productionEnvironment() {
        return new MockEnvironment()
                .withProperty("spring.profiles.active", "prod")
                .withProperty("FRESHLINE_COOKIE_SECURE", "true")
                .withProperty("spring.datasource.password", "configured-db-secret")
                .withProperty("DEMO_MODE", "true")
                .withProperty("FRESHLINE_DEMO_ACCESS_CODE", "configured-demo-code");
    }
}
