package com.evalvis.post;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class EditedPost {
    private final String id;
    private final String title;
    private final String content;

    private EditedPost(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public void edit(PostRepository repository, ContentStorage storage) {
        repository
                .findFirstByPostIdOrderByVersionDesc(id)
                .ifPresent(entry -> {
                    storage.upload(entry.getPostId(), entry.getVersion() + 1, content);
                    repository.save(PostRepository.PostEntry.edited(title, entry));
                });
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public static final class EditedPostRequest {
        private final String id;
        private final String title;
        private final String content;

        public EditedPostRequest(String id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }

        public EditedPost toEditedPost(Safelist safelist) {
            return new EditedPost(id, title, Jsoup.clean(content, safelist));
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }
}
