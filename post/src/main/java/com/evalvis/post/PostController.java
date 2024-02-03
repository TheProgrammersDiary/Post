package com.evalvis.post;

import com.evalvis.post.logging.RestNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("posts")
final class PostController {
    private final PostRepository postRepository;
    private final ContentStorage contentStorage;

    @Autowired
    PostController(PostRepository postRepository, ContentStorage contentStorage) {
        this.postRepository = postRepository;
        this.contentStorage = contentStorage;
    }

    @GetMapping
    ResponseEntity<Collection<PostRepository.PostEntry>> getAll()
    {
        return ResponseEntity.ok(postRepository.findAll());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<Post> getById(@PathVariable String id)
    {
        return Post
                .existing(id, postRepository, contentStorage)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RestNotFoundException("Post with id: " + id + " not found."));
    }

    @PostMapping(value = "/create")
    ResponseEntity<PostRepository.PostEntry> create(@RequestBody Post post)
    {
        return ResponseEntity.ok(post.save(postRepository, contentStorage));
    }
}
