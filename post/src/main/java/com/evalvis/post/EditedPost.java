package com.evalvis.post;

public class EditedPost {
    private final String id;
    private final String title;
    private final String content;

    public EditedPost(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public void edit(PostRepository repository, ContentStorage storage) {
        repository
                .findFirstByIdOrderByDatePostedDesc(id)
                .ifPresent(entry -> {
                    storage.upload(entry.getId(), entry.getVersion() + 1, content);
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
}
