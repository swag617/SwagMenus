package com.swag.swagmenus.web;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Password-based authentication for the web editor.
 *
 * <p>Flow:
 * <ol>
 *   <li>Browser POSTs password to {@code /api/login}</li>
 *   <li>Server validates against config, issues a random session token</li>
 *   <li>Browser stores the token and sends it in {@code X-Auth-Token} on every request</li>
 *   <li>Tokens expire after a configurable inactivity window (sliding expiry)</li>
 * </ol>
 */
public class AuthManager {

    private final long expiryMs;

    // token -> last access timestamp
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public AuthManager(long expiryMinutes) {
        this.expiryMs = expiryMinutes * 60_000L;
    }

    /**
     * Creates a new session token (called after password is validated).
     */
    public String createSession() {
        String token = UUID.randomUUID().toString();
        sessions.put(token, System.currentTimeMillis());
        return token;
    }

    /**
     * Validates a session token. Returns true if valid, refreshing the expiry window.
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

        sessions.put(token, now); // refresh sliding window
        return true;
    }

    /** Removes all expired session tokens. */
    private void pruneExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            if (now - it.next().getValue() > expiryMs) it.remove();
        }
    }
}
