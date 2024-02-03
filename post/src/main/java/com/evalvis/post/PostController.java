package com.evalvis.post;

import com.evalvis.post.logging.RestNotFoundException;
import com.evalvis.post.logging.UnauthorizedException;
import com.evalvis.security.BlacklistedJwtTokenRepository;
import com.evalvis.security.JwtKey;
import com.evalvis.security.JwtToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("posts")
final class PostController {
    private final PostRepository repo;
    private final ContentStorage contentStorage;
    private final BlacklistedJwtTokenRepository blacklistedJwtTokenRepository;
    private final JwtKey key;

    @Autowired
    PostController(
            PostRepository repo,
            ContentStorage contentStorage,
            BlacklistedJwtTokenRepository blacklistedJwtTokenRepository,
            JwtKey key
    ) {
        this.repo = repo;
        this.contentStorage = contentStorage;
        this.blacklistedJwtTokenRepository = blacklistedJwtTokenRepository;
        this.key = key;
    }

    @GetMapping
    ResponseEntity<Collection<PostRepository.PostEntry>> getAll()
    {
        return ResponseEntity.ok(repo.allPostsLatestVersions());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<Post> getById(@PathVariable String id, HttpServletRequest request, HttpServletResponse response)
    {
        return Post
                .existing(id, repo, contentStorage)
                .map(post -> {
                    boolean isOwner = JwtToken
                            .existing(request, key.value(), blacklistedJwtTokenRepository)
                            .map(token -> repo.existsByIdAndAuthorEmail(id, token.email()))
                            .orElse(false);
                    response.addHeader("IS-OWNER", String.valueOf(isOwner));
                    return ResponseEntity.ok(post);
                })
                .orElseThrow(() -> new RestNotFoundException("Post with id: " + id + " not found."));
    }

    @PostMapping(value = "/create")
    ResponseEntity<PostRepository.PostEntry> create(@RequestBody Post post)
    {
        return ResponseEntity.ok(post.save(repo, contentStorage));
    }

    @PutMapping(value = "/edit")
    ResponseEntity<String> edit(@RequestBody EditedPost post)
    {
        if(!repo.existsByIdAndAuthorEmail(post.getId(), SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new UnauthorizedException(
                    "Either the post with id: " + post.getId() + " does not exist or you are not authorized to edit it."
            );
        }
        post.edit(repo, contentStorage);
        return ResponseEntity.ok("Updated.");
    }
}
