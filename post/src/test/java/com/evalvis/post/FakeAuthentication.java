package com.evalvis.post;

import com.evalvis.security.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public final class FakeAuthentication implements Authentication {
    private final User user;

    public FakeAuthentication(String email, String password) {
        this.user = new User(email, password);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getCredentials() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getDetails() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getName() {
        return user.getUsername();
    }
}
