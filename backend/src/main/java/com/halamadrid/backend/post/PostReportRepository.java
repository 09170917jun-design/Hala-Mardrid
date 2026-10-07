package com.halamadrid.backend.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    @Query("select count(r) > 0 from PostReport r where r.post.id = :postId and r.reporter.id = :userId")
    boolean exists(Long postId, Long userId);
}
