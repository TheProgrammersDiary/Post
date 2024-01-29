package com.evalvis.post.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Enumeration;

@Component
@WebFilter(filterName = "HttpLoggingFilter")
public final class HttpLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Value("${post.http-masking-turned-on}")
    private boolean isHttpMaskingOn;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (ServletException | IOException e) {
            log.warn(
                    "While processing this request, exception was caused: "
                            + requestLog(request, requestWrapper).toPrettyString()
            );
            throw e;
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        ObjectNode root = requestLog(request, requestWrapper);
        String responseBody = new String(
                responseWrapper.getContentAsByteArray(), response.getCharacterEncoding()
        );
        ObjectNode responseNode = root.withObject("/response");
        responseNode
                .put("status", response.getStatus())
                .put("body", isHttpMaskingOn ? "[hidden]" : responseBody)
                .put("time taken (ms)", timeTaken);

        appendResponseHeaders(responseNode, response);
        log.info(root.toPrettyString());
        responseWrapper.copyBodyToResponse();
    }

    private ObjectNode requestLog(
            HttpServletRequest request, ContentCachingRequestWrapper requestWrapper
    ) throws UnsupportedEncodingException {
        String requestBody = new String(
                requestWrapper.getContentAsByteArray(), request.getCharacterEncoding()
        );
        ObjectNode root = new ObjectMapper().createObjectNode().withObject("/HTTP");
        ObjectNode requestNode = root.withObject("/request");
        requestNode.put("URI", request.getRequestURI());
        requestNode.put("method", request.getMethod());
        requestNode.put("body", isHttpMaskingOn ? "[hidden]" : requestBody);
        appendRequestHeaders(requestNode, request);
        return root;
    }

    private void appendRequestHeaders(ObjectNode requestNode, HttpServletRequest request) {
        ArrayNode headers = requestNode.putArray("headers");
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            ObjectNode header = new ObjectMapper()
                    .createObjectNode()
                    .put(headerName, isHttpMaskingOn ? "[hidden]": request.getHeader(headerName));
            headers.add(header);
        }
    }

    private void appendResponseHeaders(ObjectNode responseNode, HttpServletResponse response) {
        ArrayNode headers = responseNode.putArray("headers");
        Collection<String> headerNames = response.getHeaderNames();
        for(String headerName : headerNames) {
            ObjectNode header = new ObjectMapper()
                    .createObjectNode()
                    .put(headerName, isHttpMaskingOn ? "[hidden]": response.getHeader(headerName));
            headers.add(header);
        }
    }
}
