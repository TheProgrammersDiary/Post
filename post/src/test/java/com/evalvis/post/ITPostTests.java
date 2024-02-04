package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtToken;
import com.evalvis.security.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.Cookie;
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
    private BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    @Autowired
    private PostController controller;
    @Value("${server.ssl.key-store-password}")
    private String sslPassword;
    @Value("${minio.password}")
    private String minioPassword;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");
    private static final GenericContainer<?> redis = new GenericContainer<>(
            "redis:latest"
    ).withExposedPorts(6379);

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

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getFirstMappedPort());
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        redis.start();
        minio.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        redis.stop();
        minio.stop();
    }

    @Test
    void createsPost() {
        JwtToken jwtToken = jwtToken();

        String id = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .header("X-CSRF-TOKEN", jwtToken.csrfToken())
                .body(newPost())
                .cookie(new Cookie.Builder("jwt", jwtToken.value()).build())
                .post("/posts/create")
                .as(PostRepository.PostEntry.class)
                .getPostId();

        Post post = controller.findLatestById(id, new FakeHttpServletRequest(), new FakeHttpServletResponse()).getBody();

        expect.toMatchSnapshot(jsonWithMaskedProperties(post, "postId"));
    }

    @Test
    void authorEditsPost() {
        setAuthentication("author@gmail.com");
        String id = controller.create(newPost()).getBody().getPostId();
        JwtToken jwtToken = jwtToken("author@gmail.com");
        HttpServletResponse response = new FakeHttpServletResponse();

        int statusCode = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .header("X-CSRF-TOKEN", jwtToken.csrfToken())
                .body(new EditedPost(id, "edited", "edited"))
                .cookie(new Cookie.Builder("jwt", jwtToken.value()).build())
                .put("/posts/edit")
                .statusCode();

        assertEquals(200, statusCode);
        Post editedPost = controller.findLatestById(
                id, new FakeHttpServletRequest(Map.of("Authorization", "Bearer " + jwtToken.value())), response
        ).getBody();
        assertEquals("true", response.getHeader("IS-OWNER"));
        expect.toMatchSnapshot(jsonWithMaskedProperties(editedPost, "postId"));
    }

    @Test
    void nonAuthorFailsToEditPost() {
        setAuthentication("author@gmail.com");
        String id = controller.create(newPost()).getBody().getPostId();
        JwtToken jwtToken = jwtToken("nonAuthor@gmail.com");

        int statusCode = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .header("X-CSRF-TOKEN", jwtToken.csrfToken())
                .body(new EditedPost(id, "edited", "edited"))
                .cookie(new Cookie.Builder("jwt", jwtToken.value()).build())
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

    @Test
    public void authorizedRequestWithoutCsrfFails() {
        JwtToken jwtToken = jwtToken();

        Response response = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .body(newPost())
                .cookie(new Cookie.Builder("jwt", jwtToken.value()).build())
                .post("/posts/create");

        assertEquals(401, response.statusCode());
    }

    private JwtToken jwtToken() {
        return jwtToken("tester@gmail.com");
    }

    private JwtToken jwtToken(String email) {
        return JwtToken.create(
                new UsernamePasswordAuthenticationToken(
                        new User(email, null), null, null
                ),
                key.value(),
                blacklistedJwtTokenRepository
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