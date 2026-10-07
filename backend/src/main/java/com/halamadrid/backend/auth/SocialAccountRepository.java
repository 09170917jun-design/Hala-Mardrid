package com.halamadrid.backend.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    @Query("select s from SocialAccount s join fetch s.user where s.provider = :provider and s.providerId = :providerId")
    Optional<SocialAccount> findByProviderAndProviderId(SocialAccount.Provider provider, String providerId);
}
