package com.halamadrid.backend.match;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "matches", indexes = @Index(name = "idx_matches_kickoff", columnList = "kickoffAt"))
public class FootballMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long externalId;

    @Column(nullable = false, length = 10)
    private String competitionCode;

    @Column(nullable = false, length = 80)
    private String competitionName;

    private Integer matchday;

    @Column(nullable = false, length = 80)
    private String homeTeam;

    @Column(nullable = false, length = 80)
    private String awayTeam;

    @Column(nullable = false)
    private Instant kickoffAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private MatchStatus status;

    private Integer homeScore;

    private Integer awayScore;

    @Column(nullable = false)
    private Instant syncedAt;

    protected FootballMatch() {
    }

    public FootballMatch(Long externalId) {
        this.externalId = externalId;
    }

    public void sync(String competitionCode, String competitionName, Integer matchday, String homeTeam,
            String awayTeam, Instant kickoffAt, MatchStatus status, Integer homeScore, Integer awayScore) {
        this.competitionCode = competitionCode;
        this.competitionName = competitionName;
        this.matchday = matchday;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.kickoffAt = kickoffAt;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.syncedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public String getCompetitionCode() {
        return competitionCode;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public Integer getMatchday() {
        return matchday;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public Instant getKickoffAt() {
        return kickoffAt;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}
