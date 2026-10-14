package ru.quantum404.freshline.app.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.quantum404.freshline.app.api.ApiExceptionHandler;
import ru.quantum404.freshline.app.api.ApiProblemWriter;
import ru.quantum404.freshline.app.api.RequestIdFilter;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DemoSessionController.class)
@Import({SecurityConfiguration.class, SessionAttemptLimiter.class, ApiProblemWriter.class,
        ApiExceptionHandler.class, RequestIdFilter.class, IdempotencyKeyWebConfiguration.class,
        DemoSessionSecurityTest.ProbeConfiguration.class})
@TestPropertySource(properties = {
        "FRESHLINE_DEMO_ACCESS_CODE=test-access-code",
        "DEMO_MODE=true",
        "spring.mvc.throw-exception-if-no-handler-found=true",
        "server.servlet.session.cookie.name=FRESHLINE_SESSION"
})
class DemoSessionSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void invalidAccessCodeReturnsSafeProblemAndRequestId() throws Exception {
        mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"wrong-private-code\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("INVALID_ACCESS_CODE")))
                .andExpect(jsonPath("$.message", not(containsString("wrong-private-code"))))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(header().exists(RequestIdFilter.HEADER));
    }

    @Test
    void sessionCookieAndCsrfTokenAuthorizeOnlyMatchingWrites() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"test-access-code\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode sessionResponse = objectMapper.readTree(login.getResponse().getContentAsByteArray());
        String csrfToken = sessionResponse.path("csrfToken").asText();
        var session = login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        org.junit.jupiter.api.Assertions.assertFalse(csrfToken.isBlank());

        mockMvc.perform(post("/api/v1/test/write").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("CSRF_REQUIRED")));

        mockMvc.perform(post("/api/v1/test/write")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                        .header("X-CSRF-Token", csrfToken)
                        .header("Idempotency-Key", "retry-key-0001"))
                .andExpect(status().isNoContent());
    }

    @Test
    void businessWritesRequireContractSizedIdempotencyKey() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"test-access-code\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sessionResponse = objectMapper.readTree(login.getResponse().getContentAsByteArray());
        String csrfToken = sessionResponse.path("csrfToken").asText();
        var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/test/write")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("IDEMPOTENCY_KEY_REQUIRED")))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(header().exists(RequestIdFilter.HEADER));

        mockMvc.perform(post("/api/v1/test/write")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken)
                        .header("Idempotency-Key", "short"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_IDEMPOTENCY_KEY")));

        mockMvc.perform(post("/api/v1/test/write")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken)
                        .header("Idempotency-Key", "k".repeat(129)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("INVALID_IDEMPOTENCY_KEY")));

        mockMvc.perform(post("/api/v1/test/write")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken)
                        .header("Idempotency-Key", "12345678"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/test/write")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken)
                        .header("Idempotency-Key", "k".repeat(128)))
                .andExpect(status().isNoContent());
    }

    @Test
    void blankAccessCodeUsesContractValidationProblem() throws Exception {
        mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void sessionExchangeReturnsRateLimitProblemAfterFiveAttempts() throws Exception {
        RequestPostProcessor clientAddress = request -> {
            request.setRemoteAddr("192.0.2.77");
            return request;
        };

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/v1/session")
                            .with(clientAddress)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"accessCode\":\"wrong-code\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/session")
                        .with(clientAddress)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"wrong-code\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code", is("RATE_LIMITED")))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void unexpectedFailureDoesNotExposeInternalMessage() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"test-access-code\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sessionResponse = objectMapper.readTree(login.getResponse().getContentAsByteArray());
        String csrfToken = sessionResponse.path("csrfToken").asText();
        var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/test/failure")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken)
                        .header("Idempotency-Key", "retry-key-0002"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code", is("INTERNAL_ERROR")))
                .andExpect(jsonPath("$.message", not(containsString("internal-private-detail"))))
                .andExpect(content().string(not(containsString("internal-private-detail"))));
    }

    @Test
    void unknownWriteRouteUsesProblemContractAfterSessionAndCsrfPass() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"test-access-code\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode sessionResponse = objectMapper.readTree(login.getResponse().getContentAsByteArray());
        String csrfToken = sessionResponse.path("csrfToken").asText();
        var session = (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/api/v1/test/missing")
                        .session(session)
                        .header("X-CSRF-Token", csrfToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void unsupportedMethodUsesSafeProblemContract() throws Exception {
        mockMvc.perform(get("/api/v1/session"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("METHOD_NOT_ALLOWED")))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("POST")))
                .andExpect(header().exists(RequestIdFilter.HEADER));
    }

    @Test
    void unsupportedContentTypeUsesSafeProblemContract() throws Exception {
        mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("access-code"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("UNSUPPORTED_MEDIA_TYPE")))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(header().exists(RequestIdFilter.HEADER));
    }

    @Test
    void malformedJsonUsesSafeProblemContract() throws Exception {
        mockMvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("INVALID_REQUEST_BODY")))
                .andExpect(jsonPath("$.requestId").isNotEmpty())
                .andExpect(header().exists(RequestIdFilter.HEADER));
    }

    @TestConfiguration
    static class ProbeConfiguration {
        @Bean
        ProtectedWriteProbe protectedWriteProbe() {
            return new ProtectedWriteProbe();
        }
    }

    @RestController
    @RequestMapping("/api/v1/test")
    static class ProtectedWriteProbe {
        @PostMapping("/write")
        @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
        void write() {
        }

        @PostMapping("/failure")
        void fail() {
            throw new IllegalStateException("internal-private-detail");
        }
    }
}
