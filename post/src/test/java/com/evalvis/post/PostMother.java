package com.evalvis.post;

import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtRefreshToken;
import com.evalvis.security.JwtShortLivedToken;
import com.evalvis.security.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostMother {
    private final PostController controller;
    private final JwtKey key;

    public PostMother(PostController controller, JwtKey key) {
        this.controller = controller;
        this.key = key;
    }

    public PostRepository.PostEntry create(HttpServletRequest request, String content) {
        return controller.create(request, new Post.PostRequest("title", content)).getBody();
    }

    public List<PostRepository.PostEntry> createMultiple(HttpServletRequest request) {
        return List.of(
                controller.create(request, new Post.PostRequest("title1", "content1")).getBody(),
                controller.create(request, new Post.PostRequest("title2", "content2")).getBody()
        );
    }

    public HttpServletRequest request(String username, String email) {
        return new FakeHttpServletRequest(
                Map.of(
                        "Authorization",
                        "Bearer " + jwtToken(username, email).value()
                )
        );
    }
    public JwtShortLivedToken jwtToken(String username, String email) {
        return JwtShortLivedToken.create(
                JwtRefreshToken.create(
                        username,
                        new UsernamePasswordAuthenticationToken(new User(email, null), null, null),
                        key.value(),
                        true
                ),
                key.value()
        );
    }


    public Post edit(EditedPost.EditedPostRequest post, int nextVersion) {
        int status = controller.edit(post).getStatusCode().value();
        assertEquals(200, status);
        return controller
                .findByIdAndVersion(post.getId(), nextVersion, new FakeHttpServletRequest(), new FakeHttpServletResponse())
                .getBody();
    }
}
