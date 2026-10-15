package ru.quantum404.freshline.app.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionAttemptLimiterTest {
    @Test
    void limitsEachClientAfterFiveSessionAttempts() {
        SessionAttemptLimiter limiter = new SessionAttemptLimiter();

        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }
        assertFalse(limiter.tryAcquire("client-a"));
        assertTrue(limiter.tryAcquire("client-b"));
    }

    @Test
    void successfulSessionCanResetItsClientWindow() {
        SessionAttemptLimiter limiter = new SessionAttemptLimiter();
        for (int attempt = 0; attempt < 5; attempt++) {
            assertTrue(limiter.tryAcquire("client-a"));
        }

        limiter.clear("client-a");

        assertTrue(limiter.tryAcquire("client-a"));
    }
}
