package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SnapshotExtension.class})
public class PostTests {
    private Expect expect;
    private final PostRepository repository;
    private final PostController controller;
    private final PostMother mother;

    public PostTests() {
        this.repository = new FakePostRepository();
        this.controller = new PostController(repository, new FakeMinioStorage());
        this.mother = new PostMother(this.controller);
    }

    @Test
    public void createsPost() {
        PostRepository.PostEntry post = mother.create();

        expect.toMatchSnapshot(jsonWithMaskedProperties(post, "id"));
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }

    @Test
    public void findsPost() {
        PostRepository.PostEntry initialPost = mother.create();

        PostRepository.PostEntry foundPost = repository.findById(initialPost.getId()).get();

        assertEquals(initialPost, foundPost);
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
}