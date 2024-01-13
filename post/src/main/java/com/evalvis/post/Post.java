package com.evalvis.post;

public final class Post {
    private final String author;
    private final String title;
    private final String content;

    public Post(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public PostRepository.PostEntry save(PostRepository postRepository) {
        return postRepository.save(new PostRepository.PostEntry(author, title, content));
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
