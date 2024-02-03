package com.evalvis.post;

import java.util.Optional;

public final class Post {
    private final String author;
    private final String title;
    private final String content;

    public static Post newlyCreated(String author, String title, String content) {
        return new Post(author, title, content);
    }

    public static Optional<Post> existing(String id, PostRepository postRepository, ContentStorage contentStorage) {
        return postRepository
                .findById(id)
                .map(entry -> new Post(entry.getAuthor(), entry.getTitle(), contentStorage.download(entry.getId())));
    }

    private Post(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostRepository.PostEntry save(PostRepository postRepository, ContentStorage contentStorage) {
        PostRepository.PostEntry entry = postRepository.save(new PostRepository.PostEntry(author, title));
        contentStorage.upload(entry.getId(), content);
        return entry;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
