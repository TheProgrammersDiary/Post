package com.evalvis.post;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostMother {
    private final PostController controller;

    public PostMother(PostController controller) {
        this.controller = controller;
    }

    public PostRepository.PostEntry create(String content) {
        return controller.create(Post.newlyCreated("author", "title", content)).getBody();
    }

    public List<PostRepository.PostEntry> createMultiple() {
        return List.of(
                controller.create(Post.newlyCreated("author1", "title1", "content1")).getBody(),
                controller.create(Post.newlyCreated("author2", "title2", "content2")).getBody()
        );
    }

    public Post edit(EditedPost post) {
        int status = controller.edit(post).getStatusCode().value();
        assertEquals(200, status);
        return controller.findLatestById(post.getId(), new FakeHttpServletRequest(), new FakeHttpServletResponse()).getBody();
    }
}
