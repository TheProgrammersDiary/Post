package com.evalvis.post;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;
import java.util.UUID;

public class FakeHttpSession implements HttpSession {
    private final String id = UUID.randomUUID().toString();

    @Override
    public long getCreationTime() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void setAttribute(String name, Object value) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
