package com.halamadrid.backend.football;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** football-data.org v4 API 호출. 무료 플랜은 분당 10회라서 동기화 때만 호출한다. */
@Component
public class FootballDataClient {

    // ---- API 응답 형태 (필요한 필드만) ----
    public record SquadMember(Long id, String name, String position, String dateOfBirth, String nationality,
            Integer shirtNumber) {
    }

    public record TeamResponse(List<SquadMember> squad) {
    }

    public record TeamRef(Long id, String name, String shortName, String tla) {
    }

    public record Competition(String code, String name) {
    }

    public record ScoreTime(Integer home, Integer away) {
    }

    public record Score(ScoreTime fullTime) {
    }

    public record ApiMatch(Long id, String utcDate, String status, Integer matchday, String stage,
            Competition competition, TeamRef homeTeam, TeamRef awayTeam, Score score) {
    }

    public record MatchesResponse(List<ApiMatch> matches) {
    }

    private final RestClient http;
    private final String apiKey;
    private final long teamId;

    public FootballDataClient(@Value("${app.football.api-key:}") String apiKey,
            @Value("${app.football.base-url}") String baseUrl,
            @Value("${app.football.team-id}") long teamId) {
        this.apiKey = apiKey;
        this.teamId = teamId;
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    public TeamResponse fetchTeam() {
        return http.get().uri("/teams/{id}", teamId).header("X-Auth-Token", apiKey).retrieve()
                .body(TeamResponse.class);
    }

    public MatchesResponse fetchMatches() {
        return http.get().uri("/teams/{id}/matches", teamId).header("X-Auth-Token", apiKey).retrieve()
                .body(MatchesResponse.class);
    }
}
