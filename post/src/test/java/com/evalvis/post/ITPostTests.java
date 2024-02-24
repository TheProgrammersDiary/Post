package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtShortLivedToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({SnapshotExtension.class})
@ActiveProfiles("it")
public class ITPostTests {
    private Expect expect;
    @Value("${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    private JwtKey key;
    private PostController controller;
    @Value("${server.ssl.key-store-password}")
    private String sslPassword;
    private PostMother mother;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");
    private static final GenericContainer<?> minio = new GenericContainer<>(
            "minio/minio:RELEASE.2023-06-29T05-12-28Z.fips"
    )
            .withExposedPorts(9000)
            .withCommand("server /data")
            .withEnv("MINIO_ROOT_USER", "admin")
            .withEnv("MINIO_ROOT_PASSWORD", "administrator");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getFirstMappedPort());
        registry.add("minio.bucket", () -> "posts");
        registry.add("minio.username", () -> "admin");
        registry.add("minio.password", () -> "administrator");
    }

    @Autowired
    public ITPostTests(JwtKey key, PostController controller) {
        this.key = key;
        this.controller = controller;
        this.mother = new PostMother(controller, key);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        minio.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        minio.stop();
    }

    @Test
    void createsPost() {
        JwtShortLivedToken jwtToken = mother.jwtToken("tester", "tester@gmail.com");

        String id = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(newPostRequest())
                .header("AUTHORIZATION", "Bearer " + jwtToken.value())
                .post("/posts/create")
                .as(PostRepository.PostEntry.class)
                .getPostId();

        Post post = controller
                .findByIdAndVersion(id, 1, new FakeHttpServletRequest(), new FakeHttpServletResponse())
                .getBody();
        expect.toMatchSnapshot(jsonWithMaskedProperties(post, "postId"));
    }

    @Test
    void authorEditsPost() {
        setAuthentication("author@gmail.com");
        String id = controller.create(
                mother.request("author", "author@gmail.com"), newPostRequest()
        ).getBody().getPostId();
        JwtShortLivedToken jwtToken = mother.jwtToken("author", "author@gmail.com");
        HttpServletResponse response = new FakeHttpServletResponse();

        int statusCode = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(new EditedPost.EditedPostRequest(id, "edited", "edited"))
                .header("AUTHORIZATION", "Bearer " + jwtToken.value())
                .put("/posts/edit")
                .statusCode();

        assertEquals(200, statusCode);
        Post editedPost = controller.findByIdAndVersion(
                id, 2, new FakeHttpServletRequest(Map.of("Authorization", "Bearer " + jwtToken.value())), response
        ).getBody();
        assertEquals("true", response.getHeader("IS-OWNER"));
        expect.toMatchSnapshot(jsonWithMaskedProperties(editedPost, "postId"));
    }

    @Test
    void nonAuthorFailsToEditPost() {
        setAuthentication("author@gmail.com");
        String id = controller.create(
                mother.request("author", "author@gmail.com"), newPostRequest()
        ).getBody().getPostId();
        JwtShortLivedToken nonAuthorJwtToken = mother.jwtToken("nonAuthor", "nonAuthor@gmail.com");

        int statusCode = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(new EditedPost.EditedPostRequest(id, "edited", "edited"))
                .header("AUTHORIZATION", "Bearer " + nonAuthorJwtToken.value())
                .put("/posts/edit")
                .statusCode();

        assertEquals(401, statusCode);
    }

    @Test
    void findsPostOfEarlierVersion() {
        setAuthentication("author@gmail.com");
        String id = controller.create(
                mother.request("author", "author@gmail.com"), newPostRequest()
        ).getBody().getPostId();

        controller.edit(new EditedPost.EditedPostRequest(id, "changed", "changed"));
        controller.edit(new EditedPost.EditedPostRequest(id, "changedAgain", "changedAgain"));

        Response response = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .get("/posts/" + id + "/" + 2);

        assertEquals(200, response.statusCode());
        expect.toMatchSnapshot(jsonWithMaskedProperties(response.as(Post.class), "postId"));
    }

    private void setAuthentication(String email) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new FakeAuthentication(email, "password"));
        SecurityContextHolder.setContext(securityContext);
    }

    private Post.PostRequest newPostRequest() {
        return new Post.PostRequest("Testing matters", "You either test first, test along coding, or don't test at all.");
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }
}