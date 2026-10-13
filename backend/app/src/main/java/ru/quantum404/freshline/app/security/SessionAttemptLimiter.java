package ru.quantum404.freshline.app.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SessionAttemptLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_TRACKED_CLIENTS = 4096;
    private static final long WINDOW_NANOS = Duration.ofMinutes(1).toNanos();

    private final ConcurrentMap<String, AttemptWindow> attemptsByClient = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientKey) {
        long now = System.nanoTime();
        if (attemptsByClient.size() >= MAX_TRACKED_CLIENTS) {
            attemptsByClient.entrySet().removeIf(entry -> now - entry.getValue().startedAtNanos > WINDOW_NANOS);
            if (attemptsByClient.size() >= MAX_TRACKED_CLIENTS && !attemptsByClient.containsKey(clientKey)) {
                return false;
            }
        }

        AttemptWindow window = attemptsByClient.computeIfAbsent(clientKey, ignored -> new AttemptWindow(now));
        synchronized (window) {
            if (now - window.startedAtNanos >= WINDOW_NANOS) {
                window.startedAtNanos = now;
                window.attempts = 0;
            }
            if (window.attempts >= MAX_ATTEMPTS) {
                return false;
            }
            window.attempts++;
            return true;
        }
    }

    public void clear(String clientKey) {
        attemptsByClient.remove(clientKey);
    }

    private static final class AttemptWindow {
        private long startedAtNanos;
        private int attempts;

        private AttemptWindow(long startedAtNanos) {
            this.startedAtNanos = startedAtNanos;
        }
    }
}
