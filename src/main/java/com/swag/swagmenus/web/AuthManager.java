package com.swag.swagmenus.web;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Password-based session authentication for the web editor.
 *
 * Flow: browser POSTs password to /api/login; server validates and issues a random token;
 * browser sends that token in X-Auth-Token on every subsequent request.
 * Tokens use a sliding expiry window — each valid request resets the timer.
 */
public class AuthManager {

    private final long expiryMs;

    // token -> last access timestamp
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthManager(long expiryMinutes) {
        this.expiryMs = expiryMinutes * 60_000L;
    }

    public String createSession() {
        String token = UUID.randomUUID().toString();
        sessions.put(token, System.currentTimeMillis());
        return token;
    }

    /**
     * Returns true if the token is valid, refreshing its expiry window on success.
     */
    public boolean validate(String token) {
        if (token == null || token.isBlank()) return false;

        pruneExpired();

        Long lastAccess = sessions.get(token);
        if (lastAccess == null) return false;

        long now = System.currentTimeMillis();
        if (now - lastAccess > expiryMs) {
            sessions.remove(token);
            return false;
        }

        sessions.put(token, now);
        return true;
    }

    private void pruneExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue() > expiryMs) it.remove();
        }
    }
}
