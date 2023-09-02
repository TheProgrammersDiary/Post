package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import protobufs.PostRequest;

import java.util.Arrays;

import static shadow.org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SnapshotExtension.class})
@ActiveProfiles("ittest")
public class PostTest {

    private Expect expect;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15.4"
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Test
    @SnapshotName("createsPost")
    public void createsPost() {
        PostRequest postRequest = PostRequest
                .newBuilder()
                .setAuthor("Human")
                .setTitle("Testing matters")
                .setContent("You either test first, test along coding, or don't test at all.")
                .build();

        PostRepository.PostEntry postFromResponse = restTemplate.postForObject(
                "http://localhost:" + port + "/posts/create",
                postRequest, PostRepository.PostEntry.class
        );

        expect.toMatchSnapshot(jsonWithMaskedProperties(postFromResponse, "id"));
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }
}