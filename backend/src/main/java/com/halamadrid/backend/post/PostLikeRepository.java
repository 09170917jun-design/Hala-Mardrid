package com.halamadrid.backend.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Query("select count(l) > 0 from PostLike l where l.post.id = :postId and l.user.id = :userId")
    boolean exists(Long postId, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PostLike l where l.post.id = :postId and l.user.id = :userId")
    int delete(Long postId, Long userId);
}
