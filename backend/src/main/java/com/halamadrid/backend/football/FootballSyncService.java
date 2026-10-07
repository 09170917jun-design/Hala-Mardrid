package com.halamadrid.backend.football;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.halamadrid.backend.football.FootballDataClient.ApiMatch;
import com.halamadrid.backend.football.FootballDataClient.SquadMember;
import com.halamadrid.backend.match.FootballMatch;
import com.halamadrid.backend.match.MatchRepository;
import com.halamadrid.backend.match.MatchStatus;
import com.halamadrid.backend.player.Player;
import com.halamadrid.backend.player.PlayerRepository;
import com.halamadrid.backend.player.Position;

/** football-data.org의 선수단·경기 데이터를 우리 DB로 가져온다. */
@Service
public class FootballSyncService {

    private static final Logger log = LoggerFactory.getLogger(FootballSyncService.class);

    private final FootballDataClient client;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TransactionTemplate tx;
    private final Duration staleAfter;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Instant lastSuccess;

    public FootballSyncService(FootballDataClient client, PlayerRepository playerRepository,
            MatchRepository matchRepository, TransactionTemplate tx,
            @Value("${app.football.stale-hours}") long staleHours) {
        this.client = client;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.tx = tx;
        this.staleAfter = Duration.ofHours(staleHours);
    }

    /** 선수단과 경기를 모두 동기화한다. API 키가 없거나 이미 실행 중이면 아무것도 하지 않는다. */
    public void syncAll() {
        if (!client.isConfigured()) {
            log.info("FOOTBALL_DATA_API_KEY is not set; skipping football sync.");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            syncPlayers();
            syncMatches();
            lastSuccess = Instant.now();
            log.info("Football data synced.");
        } catch (Exception e) {
            // 외부 API 장애가 서비스 전체에 영향을 주지 않도록 기존 데이터를 그대로 둔다.
            log.warn("Football sync failed: {}", e.getMessage());
        } finally {
            running.set(false);
        }
    }

    /** 데이터가 오래됐으면 백그라운드로 갱신을 시작한다. (조회 요청을 막지 않는다) */
    public void refreshIfStale() {
        if (!client.isConfigured() || running.get()) {
            return;
        }
        Instant last = lastSuccess;
        if (last == null) {
            last = latest(playerRepository.lastSyncedAt().orElse(null), matchRepository.lastSyncedAt().orElse(null));
        }
        if (last == null || last.isBefore(Instant.now().minus(staleAfter))) {
            CompletableFuture.runAsync(this::syncAll);
        }
    }

    void syncPlayers() {
        List<SquadMember> squad = client.fetchTeam().squad();
        if (squad == null || squad.isEmpty()) {
            return; // 비정상 응답으로 선수단이 통째로 비활성화되는 것을 막는다.
        }
        tx.executeWithoutResult(status -> {
            Set<Long> seen = new HashSet<>();
            for (SquadMember m : squad) {
                if (m.id() == null || m.name() == null) {
                    continue;
                }
                seen.add(m.id());
                Player player = playerRepository.findByExternalId(m.id()).orElseGet(() -> new Player(m.id()));
                player.sync(m.name(), Position.fromApi(m.position()), m.shirtNumber(), m.nationality(),
                        parseDate(m.dateOfBirth()));
                playerRepository.save(player);
            }
            playerRepository.findByActiveTrue().stream()
                    .filter(p -> !seen.contains(p.getExternalId()))
                    .forEach(Player::deactivate);
        });
    }

    void syncMatches() {
        List<ApiMatch> matches = client.fetchMatches().matches();
        if (matches == null) {
            return;
        }
        tx.executeWithoutResult(status -> {
            for (ApiMatch m : matches) {
                if (m.id() == null || m.utcDate() == null || m.homeTeam() == null || m.awayTeam() == null
                        || m.competition() == null) {
                    continue;
                }
                FootballMatch match = matchRepository.findByExternalId(m.id()).orElseGet(() -> new FootballMatch(m.id()));
                Integer home = m.score() != null && m.score().fullTime() != null ? m.score().fullTime().home() : null;
                Integer away = m.score() != null && m.score().fullTime() != null ? m.score().fullTime().away() : null;
                match.sync(m.competition().code(), m.competition().name(), m.matchday(), teamName(m.homeTeam()),
                        teamName(m.awayTeam()), Instant.parse(m.utcDate()), MatchStatus.fromApi(m.status()), home, away);
                matchRepository.save(match);
            }
        });
    }

    private static String teamName(FootballDataClient.TeamRef team) {
        if (team.shortName() != null && !team.shortName().isBlank()) {
            return team.shortName();
        }
        return team.name() != null ? team.name() : "TBD";
    }

    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private static Instant latest(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isAfter(b) ? a : b;
    }
}
