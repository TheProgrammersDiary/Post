package com.evalvis.post;

public interface ContentStorage {
    void upload(String objectId, String content);
    String download(String objectId);
}
