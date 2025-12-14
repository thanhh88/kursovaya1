package org.example.blog.model;

/**
 * Статусы записей блога (хранятся в БД как строки на русском).
 */
public final class PostStatus {

    /** Черновик (draft) */
    public static final String DRAFT = "Черновик";

    /** Опубликовано (published) */
    public static final String PUBLISHED = "Опубликовано";

    private PostStatus() {
    }
}
