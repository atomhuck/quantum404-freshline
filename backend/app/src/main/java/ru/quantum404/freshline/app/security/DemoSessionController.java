package ru.quantum404.freshline.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.quantum404.freshline.app.api.ApiProblemException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/session")
public class DemoSessionController {
    private final String configuredAccessCode;
    private final boolean demoMode;
    private final SessionAttemptLimiter attemptLimiter;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy = new ChangeSessionIdAuthenticationStrategy();

    public DemoSessionController(
            @Value("${FRESHLINE_DEMO_ACCESS_CODE:}") String configuredAccessCode,
            @Value("${DEMO_MODE:true}") boolean demoMode,
            SessionAttemptLimiter attemptLimiter,
            CsrfTokenRepository csrfTokenRepository,
            SecurityContextRepository securityContextRepository
    ) {
        this.configuredAccessCode = configuredAccessCode;
        this.demoMode = demoMode;
        this.attemptLimiter = attemptLimiter;
        this.csrfTokenRepository = csrfTokenRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping
    public SessionResponse createSession(
            @Valid @RequestBody SessionRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String clientKey = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        if (!demoMode || configuredAccessCode.isBlank()) {
            throw new ApiProblemException(HttpStatus.SERVICE_UNAVAILABLE,
                    "DEMO_SESSION_DISABLED", "Demo sessions are not configured");
        }
        if (!attemptLimiter.tryAcquire(clientKey)) {
            throw new ApiProblemException(HttpStatus.TOO_MANY_REQUESTS,
                    "RATE_LIMITED", "Too many session attempts; try again later");
        }
        if (!matches(body.accessCode(), configuredAccessCode)) {
            throw new ApiProblemException(HttpStatus.UNAUTHORIZED,
                    "INVALID_ACCESS_CODE", "The demo access code is invalid");
        }

        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }
        HttpSession session = request.getSession(true);

        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                "demo", null, List.of(new SimpleGrantedAuthority("ROLE_DEMO_WRITE")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
        attemptLimiter.clear(clientKey);
        return new SessionResponse(csrfToken.getToken());
    }

    private static boolean matches(String candidate, String expected) {
        if (candidate == null || candidate.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(candidate.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }

    public record SessionRequest(@NotBlank String accessCode) {
    }

    public record SessionResponse(String csrfToken) {
    }
}
