package org.example.blog.dao;

import org.example.blog.model.Post;
import org.example.blog.model.User;

import java.util.List;

/**
 * DAO interface cho Post.
 * Nếu trước đây bạn có thêm hàm nào khác trong PostDAO (save, update, v.v.)
 * thì thêm cả vào đây.
 */
public interface PostDAO {

    /**
     * Lấy tất cả bài viết PUBLISHED, mới nhất nằm trên cùng (cho Reader).
     */
    List<Post> findAllPublished();

    /**
     * Tìm bài viết PUBLISHED theo title + content (cho Reader).
     */
    List<Post> searchPublished(String keyword);

    // ====== Các hàm thống kê cho Dashboard ======

    /**
     * Tổng số bài viết của 1 tác giả.
     */
    long countByAuthor(User author);

    /**
     * Số bài viết của 1 tác giả theo trạng thái (PUBLISHED/DRAFT).
     */
    long countByAuthorAndStatus(User author, String status);

    /**
     * Tổng views của tất cả bài viết thuộc 1 tác giả.
     */
    long sumViewsByAuthor(User author);

    /**
     * Đếm số bài theo topic cho 1 tác giả.
     * Trả về list Object[]{ String topicName, Long count }.
     */
    List<Object[]> countByTopicForAuthor(User author);

    /**
     * Lấy tất cả bài viết của 1 tác giả (dùng để tính Top post engagement).
     */
    List<Post> findByAuthor(User author);
}
