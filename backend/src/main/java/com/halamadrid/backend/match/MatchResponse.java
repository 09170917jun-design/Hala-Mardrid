package com.halamadrid.backend.match;

import java.time.Instant;

public record MatchResponse(Long id, String competitionCode, String competitionName, Integer matchday,
        String homeTeam, String awayTeam, Instant kickoffAt, MatchStatus status, Integer homeScore,
        Integer awayScore) {

    static MatchResponse from(FootballMatch m) {
        return new MatchResponse(m.getId(), m.getCompetitionCode(), m.getCompetitionName(), m.getMatchday(),
                m.getHomeTeam(), m.getAwayTeam(), m.getKickoffAt(), m.getStatus(), m.getHomeScore(),
                m.getAwayScore());
    }
}
