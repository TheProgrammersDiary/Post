package com.evalvis.post;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<PostRepository.PostEntry, String> {
    List<PostEntry> findAll();

    @Entity(name = "post")
    @JsonPropertyOrder(alphabetic=true)
    class PostEntry {
        @Id
        @Column(unique = true)
        private String id;
        @Column(nullable = false)
        private String author;
        @Column(nullable = false)
        private String title;

        PostEntry(String author, String title) {
            this.id = UUID.randomUUID().toString();
            this.author = author;
            this.title = title;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostEntry postEntry = (PostEntry) o;
            return Objects.equals(id, postEntry.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "PostEntry{" +
                    "id='" + id + '\'' +
                    ", author='" + author + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }
}
