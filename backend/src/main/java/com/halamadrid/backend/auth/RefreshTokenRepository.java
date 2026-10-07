package com.halamadrid.backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("select t from RefreshToken t join fetch t.user where t.tokenHash = :hash")
    Optional<RefreshToken> findByTokenHash(String hash);

    @Modifying
    @Query("delete from RefreshToken t where t.tokenHash = :hash")
    int deleteByTokenHash(String hash);
}
