package com.evalvis.post;

import java.util.*;

public class FakePostRepository implements PostRepository {
    private final Map<String, PostEntry> entries = new HashMap<>();

    @Override
    public <S extends PostEntry> S save(S entry) {
        entries.put(entry.getId(), entry);
        return entry;
    }

    @Override
    public <S extends PostEntry> Iterable<S> saveAll(Iterable<S> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<PostEntry> findById(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return entries.get(id) != null;
    }

    @Override
    public List<PostEntry> findAll() {
        return entries.values().stream().toList();
    }

    @Override
    public Iterable<PostEntry> findAllById(Iterable<String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long count() {
        return entries.size();
    }

    @Override
    public void deleteById(String id) {
        entries.remove(id);
    }

    @Override
    public void delete(PostEntry entry) {
        entries.values().remove(entry);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll(Iterable<? extends PostEntry> ids) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void deleteAll() {
        entries.clear();
    }
}
