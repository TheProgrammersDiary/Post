package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.post.logging.RestNotFoundException;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SnapshotExtension.class})
public class PostTests {
    private Expect expect;
    private final PostRepository repository;
    private final BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    private final JwtKey jwtKey;
    private final PostController controller;
    private final PostMother mother;

    public PostTests() {
        this.repository = new FakePostRepository();
        this.blacklistedJwtTokenRepository = new BlacklistedJwtTokenFakeRepository();
        this.jwtKey = new JwtKey() {
            @Override
            public SecretKey value() {
                return Keys.hmacShaKeyFor(
                        Decoders.BASE64.decode(
                                "b936cee86c9f87aa5d3c6f2e84cb5a4239a5fe50480a6ec66b70ab5b1f4ac6730c6c51542" +
                                        "1b327ec1d69402e53dfb49ad7381eb067b338fd7b0cb22247225d47"
                        )
                );
            }
        };
        this.controller = new PostController(
                repository, new FakeMinioStorage(), new BlacklistedJwtTokenFakeRepository(), jwtKey
        );
        this.mother = new PostMother(this.controller);
    }

    @BeforeAll
    public static void setup() {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new FakeAuthentication("user@gmail.com", "password"));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void createsPost() {
        PostRepository.PostEntry post = mother.create("content");

        expect.toMatchSnapshot(jsonWithMaskedProperties(post, "id", "datePosted"));
    }

    @Test
    public void editsPost() {
        PostRepository.PostEntry post = mother.create("initial");

        Post editedPost = mother.edit(new EditedPost(post.getId(), "changed", "changed"));

        expect.toMatchSnapshot(jsonWithMaskedProperties(editedPost, "id"));
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }

    @Test
    public void findsPost() {
        PostRepository.PostEntry initialPost = mother.create("content");

        Post foundPost = controller.getById(
                initialPost.getId(), new FakeHttpServletRequest(), new FakeHttpServletResponse()
        ).getBody();

        assertEquals(initialPost.getAuthorName(), foundPost.getAuthor());
        assertEquals(initialPost.getTitle(), foundPost.getTitle());
        assertEquals("content", foundPost.getContent());
    }

    @Test
    public void findsAllPosts() {
        List<PostRepository.PostEntry> initialPosts = mother.createMultiple();

        List<PostRepository.PostEntry> foundPosts = new ArrayList<>(controller.getAll().getBody());

        Assertions.assertThat(foundPosts)
                .extractingResultOf("toString")
                .containsExactlyInAnyOrderElementsOf(
                        initialPosts.stream().map(PostRepository.PostEntry::toString).toList()
                );
    }

    @Test
    public void throwsExceptionIfSearchedPostDoesNotExist() {
        Exception e = assertThrows(
                RestNotFoundException.class,
                () -> controller.getById(
                        "not-existing-id", new FakeHttpServletRequest(), new FakeHttpServletResponse()
                )
        );
        assertEquals("Post with id: not-existing-id not found.", e.getMessage());
    }
}