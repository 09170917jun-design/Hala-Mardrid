package com.halamadrid.backend.auth;

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

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // 원본 토큰은 저장하지 않고 SHA-256 해시만 저장한다.
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(User user, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public User getUser() {
        return user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
