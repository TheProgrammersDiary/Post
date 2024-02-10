package com.evalvis.post;

import java.util.*;
import java.util.stream.Collectors;

public class FakePostRepository implements PostRepository {
    private final Map<String, List<PostEntry>> entries = new HashMap<>();

    @Override
    public <S extends PostEntry> S save(S entry) {
        entries.merge(entry.getPostId(), new ArrayList<>(List.of(entry)), (oldValue, newValue) -> {
            oldValue.addAll(newValue);
            return oldValue;
        });
        return entry;
    }

    @Override
    public <S extends PostEntry> Iterable<S> saveAll(Iterable<S> entries) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public Optional<PostEntry> findById(String id) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean existsById(String id) {
        return entries.get(id) != null;
    }

    @Override
    public Iterable<PostEntry> findAll() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<PostEntry> allPostsLatestVersions() {
        return entries.values().stream()
                .map(postEntries -> postEntries.stream().max(Comparator.comparing(PostEntry::getDatePosted)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByPostIdAndAuthorEmail(String id, String email) {
        return entries.getOrDefault(id, new ArrayList<>()).stream().anyMatch(post -> post.getAuthorEmail().equals(email));
    }

    @Override
    public Optional<PostEntry> findFirstByPostIdOrderByVersionDesc(String id) {
        return entries
                .getOrDefault(id, new ArrayList<>())
                .stream()
                .max(Comparator.comparing(PostEntry::getVersion));
    }

    @Override
    public Optional<PostEntry> findByPostIdAndVersion(String id, int version) {
        return entries.getOrDefault(id, new ArrayList<>()).stream().filter(p -> p.getVersion() == version).findFirst();
    }

    @Override
    public List<PostEntry> findByPostId(String postId) {
        return entries.get(postId);
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
