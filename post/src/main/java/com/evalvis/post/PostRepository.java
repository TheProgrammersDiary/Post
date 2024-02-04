package com.evalvis.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.*;

@Repository
public interface PostRepository extends CrudRepository<PostRepository.PostEntry, String> {
    @Query("SELECT p FROM post p WHERE p.datePosted = (SELECT MAX(p2.datePosted) FROM post p2 WHERE p2.postId = p.postId)")
    List<PostEntry> allPostsLatestVersions();
    boolean existsByPostIdAndAuthorEmail(String postId, String email);
    Optional<PostEntry> findFirstByPostIdOrderByDatePostedDesc(String postId);
    Optional<PostEntry> findByPostIdAndVersion(String postId, int version);
    List<PostEntry> findByPostId(String postId);

    @Entity(name = "post")
    @IdClass(PostEntryId.class)
    @JsonPropertyOrder(alphabetic=true)
    class PostEntry {
        @Id
        @Column
        private String postId;
        @Id
        @Column
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
            return new PostEntry(entry.postId, entry.version + 1, entry.authorName, entry.authorEmail, newTitle);
        }

        private PostEntry(String postId, int version, String authorName, String authorEmail, String title) {
            this.postId = postId;
            this.version = version;
            this.datePosted = new Date();
            this.authorName = authorName;
            this.authorEmail = authorEmail;
            this.title = title;
        }

        public PostEntry() {}

        public String getPostId() {
            return postId;
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
            return Objects.equals(postId, postEntry.postId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId);
        }
    }

    class PostEntryId implements Serializable {
        private String postId;
        private int version;

        public PostEntryId() {
        }

        public PostEntryId(String postId, int version) {
            this.postId = postId;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PostEntryId that = (PostEntryId) o;
            return version == that.version && postId.equals(that.postId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(postId, version);
        }
    }
}
