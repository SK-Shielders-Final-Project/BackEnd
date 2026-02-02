package org.rookies.zdme.security;

import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class KeyStore {
    private final Map<String, Key> symmetricKeyStore = new ConcurrentHashMap<>();

    public void putKey(String userId, Key key) {
        symmetricKeyStore.put(userId, key);
    }

    public Key getKey(String userId) {
        return symmetricKeyStore.get(userId);
    }

    public void removeKey(String userId) {
        symmetricKeyStore.remove(userId);
    }
}
