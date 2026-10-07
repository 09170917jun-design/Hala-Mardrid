package com.halamadrid.backend.match;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchRepository extends JpaRepository<FootballMatch, Long> {

    Optional<FootballMatch> findByExternalId(Long externalId);

    List<FootballMatch> findByStatusInOrderByKickoffAtAsc(Collection<MatchStatus> statuses, Limit limit);

    List<FootballMatch> findByStatusInOrderByKickoffAtDesc(Collection<MatchStatus> statuses, Limit limit);

    @Query("select max(m.syncedAt) from FootballMatch m")
    Optional<Instant> lastSyncedAt();
}
