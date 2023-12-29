package com.evalvis.post;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("posts")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
final class PostController {
    private final PostRepository postRepository;

    @Autowired
    PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping
    ResponseEntity<Collection<PostRepository.PostEntry>> getAll()
    {
        return ResponseEntity.ok(postRepository.findAll());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<PostRepository.PostEntry> getById(@PathVariable String id)
    {
        return postRepository
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/create")
    ResponseEntity<PostRepository.PostEntry> create(@RequestBody Post post)
    {
        return ResponseEntity.ok(post.save(postRepository));
    }
}
