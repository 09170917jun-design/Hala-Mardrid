package com.halamadrid.backend.player;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByExternalId(Long externalId);

    List<Player> findByActiveTrue();

    @Query("select max(p.syncedAt) from Player p")
    Optional<Instant> lastSyncedAt();
}
