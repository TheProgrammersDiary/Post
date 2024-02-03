package com.evalvis.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;
import java.util.*;

@Repository
public interface PostRepository extends CrudRepository<PostRepository.PostEntry, String> {
    @Query("SELECT p FROM post p WHERE p.datePosted = (SELECT MAX(p2.datePosted) FROM post p2 WHERE p2.id = p.id)")
    List<PostEntry> allPostsLatestVersions();
    boolean existsByIdAndAuthorEmail(String id, String email);
    Optional<PostEntry> findFirstByIdOrderByDatePostedDesc(String id);

    @Entity(name = "post")
    @IdClass(PostEntryId.class)
    @JsonPropertyOrder(alphabetic=true)
    class PostEntry {
        @Id
        @Column
        private String id;
        @Id
        @Column
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int version;
        @CreationTimestamp
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false)
        public Date datePosted;
        @Column(nullable = false)
        private String authorName;
        @Column(nullable = false)
        @JsonIgnore
        private String authorEmail;
        @Column(nullable = false)
        private String title;

        public static PostEntry newlyCreated(String authorName, String authorEmail, String title) {
            return new PostEntry(UUID.randomUUID().toString(), 1, authorName, authorEmail, title);
        }

        public static PostEntry edited(String newTitle, PostEntry entry) {
            return new PostEntry(entry.id, entry.version + 1, entry.authorName, entry.authorEmail, newTitle);
        }

        private PostEntry(String id, int version, String authorName, String authorEmail, String title) {
            this.id = id;
            this.version = version;
            this.datePosted = new Date();
            this.authorName = authorName;
            this.authorEmail = authorEmail;
            this.title = title;
        }

        public PostEntry() {}

        public String getId() {
            return id;
        }

        public int getVersion() {
            return version;
        }

        public Date getDatePosted() {
            return datePosted;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getAuthorEmail() {
            return authorEmail;
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
    }

    public class PostEntryId implements Serializable {
        private String id;
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int version;

        public PostEntryId() {
        }

        public PostEntryId(String id, int version) {
            this.id = id;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostEntryId that = (PostEntryId) o;
            return version == that.version && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, version);
        }
    }
}
