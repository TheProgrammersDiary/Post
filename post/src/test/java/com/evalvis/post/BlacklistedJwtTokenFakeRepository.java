package com.evalvis.post;

import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtToken;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BlacklistedJwtTokenFakeRepository implements BlacklistedJwtTokenRepository {
    private final Map<String, Date> blacklistedJwtTokenWithExpirationDate = new HashMap<>();

    @Override
    public void blacklistToken(JwtToken token) {
        blacklistedJwtTokenWithExpirationDate.put(token.value(), token.expirationDate());
    }

    @Override
    public boolean isTokenBlacklisted(String jwt) {
        return blacklistedJwtTokenWithExpirationDate.containsKey(jwt);
    }

    @Override
    public void removeExpiredTokens() {
        throw new UnsupportedOperationException("Not implemented.");
    }
}