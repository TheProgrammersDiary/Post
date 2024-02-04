package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.post.logging.RestNotFoundException;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void createsPost() {
        PostRepository.PostEntry post = mother.create("content");

        expect.toMatchSnapshot(jsonWithMaskedProperties(post, "postId", "datePosted"));
    }

    @Test
    void editsPost() {
        PostRepository.PostEntry post = mother.create("initial");

        Post editedPost = mother.edit(new EditedPost(post.getPostId(), "changed", "changed"));

        expect.toMatchSnapshot(jsonWithMaskedProperties(editedPost, "postId"));
    }

    @Test
    void findsEarlierPost() {
        PostRepository.PostEntry post = mother.create("initial");

        mother.edit(new EditedPost(post.getPostId(), "changed", "changed"));

        expect.toMatchSnapshot(
                controller.findByIdAndVersion(
                        post.getPostId(), 1, new FakeHttpServletRequest(), new FakeHttpServletResponse()
                )
        );
    }

    @Test
    void getsDateVersionMapping() {
        String id = mother.create("initial").getPostId();
        mother.edit(new EditedPost(id, "changed", "changed"));
        mother.edit(new EditedPost(id, "changedAgain", "changedAgain"));

        expect.toMatchSnapshot(
                jsonWithMaskedProperties(controller.getDateVersionMapping(id), "postId", "datePosted")
        );
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        try {
            ObjectNode node = new ObjectMapper().valueToTree(object);
            Arrays.stream(properties).forEach(property -> maskField(node, property));
            return node;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void maskField(JsonNode node, String property) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            if (objectNode.has(property)) {
                objectNode.put(property, "#hidden#");
            }
            objectNode.fields().forEachRemaining(entry -> maskField(entry.getValue(), property));
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                maskField(element, property);
            }
        }
    }

    @Test
    void findsPost() {
        PostRepository.PostEntry initialPost = mother.create("content");

        Post foundPost = controller.findLatestById(
                initialPost.getPostId(), new FakeHttpServletRequest(), new FakeHttpServletResponse()
        ).getBody();

        assertEquals(initialPost.getAuthorName(), foundPost.getAuthor());
        assertEquals(initialPost.getTitle(), foundPost.getTitle());
        assertEquals("content", foundPost.getContent());
    }

    @Test
    void findsAllPosts() {
        List<PostRepository.PostEntry> initialPosts = mother.createMultiple();

        List<PostRepository.PostEntry> foundPosts = new ArrayList<>(controller.getAll().getBody());

        Assertions.assertThat(foundPosts)
                .extractingResultOf("toString")
                .containsExactlyInAnyOrderElementsOf(
                        initialPosts.stream().map(PostRepository.PostEntry::toString).toList()
                );
    }

    @Test
    void throwsExceptionIfSearchedPostDoesNotExist() {
        Exception e = assertThrows(
                RestNotFoundException.class,
                () -> controller.findLatestById(
                        "not-existing-id", new FakeHttpServletRequest(), new FakeHttpServletResponse()
                )
        );
        assertEquals("Post with id: not-existing-id not found.", e.getMessage());
    }
}