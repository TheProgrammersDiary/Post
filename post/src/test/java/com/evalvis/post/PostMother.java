package com.evalvis.post;

import java.util.ArrayList;
import java.util.List;

public class PostMother {
    private final PostController controller;

    public PostMother(PostController controller) {
        this.controller = controller;
    }

    public PostRepository.PostEntry create() {
        return controller.create(new Post("author", "title", "content")).getBody();
    }

    public List<PostRepository.PostEntry> createMultiple() {
        List<PostRepository.PostEntry> entries = new ArrayList<>();
        entries.add(controller.create(new Post("author1", "title1", "content1")).getBody());
        entries.add(controller.create(new Post("author2", "title2", "content2")).getBody());
        return entries;
    }
}
