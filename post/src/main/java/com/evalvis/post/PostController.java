package com.evalvis.post;

import com.evalvis.post.logging.RestNotFoundException;
import com.evalvis.post.logging.UnauthorizedException;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtShortLivedToken;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("posts")
final class PostController {
    private final PostRepository repo;
    private final ContentStorage contentStorage;
    private final JwtKey key;
    private final Safelist safelist;

    @Autowired
    PostController(PostRepository repo, ContentStorage contentStorage, JwtKey key, Safelist safelist) {
        this.repo = repo;
        this.contentStorage = contentStorage;
        this.key = key;
        this.safelist = safelist;
    }

    @GetMapping
    ResponseEntity<Collection<PostRepository.PostEntry>> getAll()
    {
        return ResponseEntity.ok(repo.allPostsLatestVersions());
    }

    @GetMapping(value = "/{id}/{version}")
    ResponseEntity<Post> findByIdAndVersion(
            @PathVariable String id, @PathVariable int version,
            HttpServletRequest request, HttpServletResponse response
    ) {
        return Post
                .existing(id, version, repo, contentStorage)
                .map(post -> {
                    addOwnerHeader(id, request, response);
                    return ResponseEntity.ok(post);
                })
                .orElseThrow(() -> new RestNotFoundException("Post with id: " + id + " not found."));
    }

    private void addOwnerHeader(String postId, HttpServletRequest request, HttpServletResponse response) {
        boolean isOwner;
        try {
            isOwner = JwtShortLivedToken
                    .existing(request, key.value())
                    .map(token -> repo.existsByPostIdAndAuthorEmail(postId, token.email()))
                    .orElse(false);
        } catch(MalformedJwtException | ExpiredJwtException e) {
            isOwner = false;
        }
        response.addHeader("IS-OWNER", String.valueOf(isOwner));
    }

    @GetMapping(value = "/{id}/version")
    ResponseEntity<List<PostRepository.PostEntry>> getDateVersionMapping(@PathVariable String id) {
        return ResponseEntity.ok(repo.findByPostId(id));
    }

    @PostMapping(value = "/create")
    ResponseEntity<PostRepository.PostEntry> create(HttpServletRequest request, @RequestBody Post.PostRequest postRequest)
    {
        return ResponseEntity.ok(
                postRequest
                        .toPost(JwtShortLivedToken.existing(request, key.value()).get().username(), safelist)
                        .save(repo, contentStorage)
        );
    }

    @PutMapping(value = "/edit")
    ResponseEntity<String> edit(@RequestBody EditedPost.EditedPostRequest post)
    {
        if(!repo.existsByPostIdAndAuthorEmail(post.getId(), SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new UnauthorizedException(
                    "Either the post with id: " + post.getId() + " does not exist or you are not authorized to edit it."
            );
        }
        post.toEditedPost(safelist).edit(repo, contentStorage);
        return ResponseEntity.ok("Updated.");
    }
}
