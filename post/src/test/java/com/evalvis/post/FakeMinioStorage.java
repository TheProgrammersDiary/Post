package com.evalvis.post;

import java.util.HashMap;
import java.util.Map;

public class FakeMinioStorage implements ContentStorage {
    private final Map<String, String> storage = new HashMap<>();

    @Override
    public void upload(String objectId, String content) {
        storage.put(objectId, content);
    }

    @Override
    public String download(String objectId) {
        return storage.get(objectId);
    }
}
