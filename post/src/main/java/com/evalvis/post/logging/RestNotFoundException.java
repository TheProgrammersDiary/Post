package com.evalvis.post.logging;

public class RestNotFoundException extends RuntimeException {
    public RestNotFoundException(String message) {
        super(message);
    }
}
