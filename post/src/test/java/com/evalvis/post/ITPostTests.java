package com.evalvis.post;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.annotations.SnapshotName;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtToken;
import com.evalvis.security.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Arrays;

import static io.restassured.RestAssured.given;

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
    @Value("${server.ssl.key-store-password}")
    private String sslPassword;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.4");
    private static final GenericContainer<?> redis = new GenericContainer<>(
            "redis:latest"
    ).withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        redis.stop();
    }

    @Test
    @SnapshotName("createsPost")
    public void createsPost() {
        JwtToken jwtToken = JwtToken.create(
                new UsernamePasswordAuthenticationToken(
                        new User("tester", null), null, null
                ),
                key.value(),
                blacklistedJwtTokenRepository
        );
        Cookie jwt = new Cookie.Builder("jwt", jwtToken.value()).build();
        Post post = new Post(
                "Human",
                "Testing matters",
                "You either test first, test along coding, or don't test at all."
        );
        PostRepository.PostEntry postFromResponse = given()
                .trustStore("blog.p12", sslPassword)
                .baseUri("https://localhost:" + port)
                .contentType("application/json")
                .header("X-CSRF-TOKEN", jwtToken.csrfToken())
                .body(post)
                .cookie(jwt)
                .post("/posts/create")
                .as(PostRepository.PostEntry.class);

        expect.toMatchSnapshot(jsonWithMaskedProperties(postFromResponse, "id"));
    }

    private <T> ObjectNode jsonWithMaskedProperties(T object, String... properties) {
        ObjectNode node = new ObjectMapper().valueToTree(object);
        Arrays.stream(properties).forEach(property -> node.put(property, "#hidden#"));
        return node;
    }
}