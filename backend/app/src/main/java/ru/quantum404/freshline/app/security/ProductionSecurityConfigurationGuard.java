package ru.quantum404.freshline.app.security;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

/** Fails application startup before data-source initialization when production secrets are unsafe. */
@Component
public final class ProductionSecurityConfigurationGuard implements BeanFactoryPostProcessor, EnvironmentAware {
    private static final Profiles PRODUCTION_PROFILES = Profiles.of("prod", "production");
    private static final Set<String> DEFAULT_SECRETS = Set.of(
            "demo", "password", "secret", "changeme", "change-me", "test", "test-access-code",
            "validation-only", "example", "freshline"
    );

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        validate(environment);
    }

    static void validate(Environment environment) {
        if (!environment.acceptsProfiles(PRODUCTION_PROFILES)) {
            return;
        }

        boolean secureCookie = environment.getProperty("FRESHLINE_COOKIE_SECURE", Boolean.class,
                environment.getProperty("server.servlet.session.cookie.secure", Boolean.class, false));
        if (!secureCookie) {
            throw new IllegalStateException("Production requires FRESHLINE_COOKIE_SECURE=true");
        }

        String databasePassword = firstNonBlank(
                environment.getProperty("spring.datasource.password"),
                environment.getProperty("SPRING_DATASOURCE_PASSWORD")
        );
        requireNonDefaultSecret("SPRING_DATASOURCE_PASSWORD", databasePassword);

        boolean demoMode = environment.getProperty("DEMO_MODE", Boolean.class, true);
        if (demoMode) {
            requireNonDefaultSecret("FRESHLINE_DEMO_ACCESS_CODE",
                    environment.getProperty("FRESHLINE_DEMO_ACCESS_CODE"));
        }
    }

    private static void requireNonDefaultSecret(String property, String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || DEFAULT_SECRETS.contains(normalized)) {
            throw new IllegalStateException("Production requires " + property + " to be set to a non-default secret");
        }
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}
