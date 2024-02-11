package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtRefreshToken;
import com.evalvis.security.JwtShortLivedToken;
import com.evalvis.security.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    @Autowired
    private JwtKey key;
    @Autowired
    private PostController controller;
    @Value("${server.ssl.key-store-password}")
    private String sslPassword;

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
        JwtShortLivedToken jwtToken = jwtToken("tester", "tester@gmail.com");

        String id = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(newPost())
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
        String id = controller.create(newPost()).getBody().getPostId();
        JwtShortLivedToken jwtToken = jwtToken("author", "author@gmail.com");
        HttpServletResponse response = new FakeHttpServletResponse();

        int statusCode = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(new EditedPost(id, "edited", "edited"))
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
        String id = controller.create(newPost()).getBody().getPostId();
        JwtShortLivedToken jwtToken = jwtToken("nonAuthor", "nonAuthor@gmail.com");

        int statusCode = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(new EditedPost(id, "edited", "edited"))
                .header("AUTHORIZATION", "Bearer " + jwtToken.value())
                .put("/posts/edit")
                .statusCode();

        assertEquals(401, statusCode);
    }

    @Test
    void findsPostOfEarlierVersion() {
        setAuthentication("author@gmail.com");
        String id = controller.create(newPost()).getBody().getPostId();

        controller.edit(new EditedPost(id, "changed", "changed"));
        controller.edit(new EditedPost(id, "changedAgain", "changedAgain"));

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

    private JwtShortLivedToken jwtToken(String username, String email) {
        return JwtShortLivedToken.create(
                JwtRefreshToken.create(
                        username,
                        new UsernamePasswordAuthenticationToken(new User(email, null), null, null),
                        key.value()
                ),
                key.value()
        );
    }

    private Post newPost() {
        return Post.newlyCreated(
                "Human",
                "Testing matters",
                "You either test first, test along coding, or don't test at all."
        );
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }
}