package com.evalvis.post;

import java.util.HashMap;
import java.util.Map;

public class FakeMinioStorage implements ContentStorage {
    private final Map<String, String> storage = new HashMap<>();

    @Override
    public void upload(String objectId, int version, String content) {
        storage.put(objectId + "_v" + version, content);
    }

    @Override
    public String download(String objectId, int version) {
        return storage.get(objectId + "_v" + version);
    }
}
