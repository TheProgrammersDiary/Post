package com.evalvis.post;

public interface ContentStorage {
    void upload(String objectId, int version, String content);
    String download(String objectId, int version);
}
