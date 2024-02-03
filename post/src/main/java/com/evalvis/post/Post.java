package com.evalvis.post;

import org.springframework.security.core.context.SecurityContextHolder;

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
                .findFirstByIdOrderByDatePostedDesc(id)
                .map(
                        entry -> new Post(
                                entry.getAuthorName(),
                                entry.getTitle(),
                                contentStorage.download(entry.getId(), entry.getVersion())
                        )
                );
    }

    private Post(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostRepository.PostEntry save(PostRepository repo, ContentStorage storage) {
        PostRepository.PostEntry entry = repo.save(
                PostRepository.PostEntry.newlyCreated(
                        author, SecurityContextHolder.getContext().getAuthentication().getName(), title
                )
        );
        storage.upload(entry.getId(), entry.getVersion(), content);
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
