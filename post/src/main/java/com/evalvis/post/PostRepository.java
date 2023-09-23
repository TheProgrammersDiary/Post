package com.evalvis.post;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<PostRepository.PostEntry, String> {

    List<PostEntry> findAll();

    @Entity(name = "post")
    @JsonPropertyOrder(alphabetic=true)
    class PostEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        @Column(unique = true)
        private String id;
        @Column(nullable = false)
        private String author;
        @Column(nullable = false)
        private String title;
        @Column(nullable = false)
        private String content;

        PostEntry(String author, String title, String content) {
            this.author = author;
            this.title = title;
            this.content = content;
        }

        public PostEntry() {}

        public String getId() {
            return id;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
