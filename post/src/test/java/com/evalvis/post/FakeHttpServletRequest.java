package com.evalvis.post;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.security.Principal;
import java.util.*;

public class FakeHttpServletRequest implements HttpServletRequest {
    private final Map<String, String> headers;
    private final Cookie[] cookies;

    public FakeHttpServletRequest() {
        this(new Cookie[]{});
    }

    public FakeHttpServletRequest(Map<String, String> map) {
        this(map, new Cookie[]{});
    }

    public FakeHttpServletRequest(Cookie[] cookies) {
        this(Collections.emptyMap(), cookies);
    }

    public FakeHttpServletRequest(Map<String, String> headers, Cookie[] cookies) {
        this.headers = headers;
        this.cookies = cookies;
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Cookie[] getCookies() {
        return cookies;
    }

    @Override
    public long getDateHeader(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getHeader(String header) {
        return headers.get(header);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getIntHeader(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getMethod() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getQueryString() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRequestURI() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public HttpSession getSession(boolean create) {
        return new FakeHttpSession();
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void login(String email, String password) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection<Part> getParts() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Part getPart(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setCharacterEncoding(String encoding) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long getContentLengthLong() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletInputStream getInputStream() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getParameter(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String[] getParameterValues(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public BufferedReader getReader() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRemoteAddr() {
        return "localhost";
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setAttribute(String name, Object o) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getRequestId() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getProtocolRequestId() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ServletConnection getServletConnection() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
