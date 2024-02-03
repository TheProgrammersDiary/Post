package com.evalvis.post;

import java.util.List;

public class PostMother {
    private final PostController controller;

    public PostMother(PostController controller) {
        this.controller = controller;
    }

    public PostRepository.PostEntry create() {
        return controller.create(Post.newlyCreated("author", "title", "content")).getBody();
    }

    public List<PostRepository.PostEntry> createMultiple() {
        return List.of(
                controller.create(Post.newlyCreated("author1", "title1", "content1")).getBody(),
                controller.create(Post.newlyCreated("author2", "title2", "content2")).getBody()
        );
    }
}
