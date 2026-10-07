package com.halamadrid.backend.post;

import java.time.Instant;

import com.halamadrid.backend.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "post_reports", uniqueConstraints = @UniqueConstraint(columnNames = { "post_id", "reporter_id" }))
public class PostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @Column(nullable = false, length = 200)
    private String reason;

    private Instant createdAt = Instant.now();

    protected PostReport() {
    }

    public PostReport(Post post, User reporter, String reason) {
        this.post = post;
        this.reporter = reporter;
        this.reason = reason;
    }
}
