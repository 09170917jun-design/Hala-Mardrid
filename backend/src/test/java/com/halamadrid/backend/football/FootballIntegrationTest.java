package com.halamadrid.backend.football;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.halamadrid.backend.football.FootballDataClient.ApiMatch;
import com.halamadrid.backend.football.FootballDataClient.Competition;
import com.halamadrid.backend.football.FootballDataClient.MatchesResponse;
import com.halamadrid.backend.football.FootballDataClient.Score;
import com.halamadrid.backend.football.FootballDataClient.ScoreTime;
import com.halamadrid.backend.football.FootballDataClient.SquadMember;
import com.halamadrid.backend.football.FootballDataClient.TeamRef;
import com.halamadrid.backend.football.FootballDataClient.TeamResponse;
import com.halamadrid.backend.match.MatchRepository;
import com.halamadrid.backend.match.MatchStatus;
import com.halamadrid.backend.player.PlayerRepository;
import com.halamadrid.backend.player.Position;

@SpringBootTest
@AutoConfigureMockMvc
class FootballIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FootballSyncService syncService;

    @Autowired
    PlayerRepository playerRepository;

    @Autowired
    MatchRepository matchRepository;

    @MockitoBean
    FootballDataClient client;

    private static SquadMember player(long id, String name, String position) {
        return new SquadMember(id, name, position, "1990-01-02", "Spain", null);
    }

    private static ApiMatch match(long id, String date, String status, String home, String away, Integer hs, Integer as) {
        return new ApiMatch(id, date, status, 1, "REGULAR_SEASON", new Competition("PD", "Primera Division"),
                new TeamRef(1L, home + " FC", home, "HOM"), new TeamRef(2L, away + " FC", away, "AWY"),
                new Score(new ScoreTime(hs, as)));
    }

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
        matchRepository.deleteAll();
        when(client.isConfigured()).thenReturn(true);
        when(client.fetchTeam()).thenReturn(new TeamResponse(List.of(
                player(1, "Zed Forward", "Offence"),
                player(2, "Amy Keeper", "Goalkeeper"),
                player(3, "Bob Defender", "Defence"),
                player(4, "Cid Mid", "Midfield"),
                player(5, "Dan Back", "Left-Back"))));
        when(client.fetchMatches()).thenReturn(new MatchesResponse(List.of(
                match(10, "2026-01-10T20:00:00Z", "FINISHED", "Real Madrid", "Alpha", 2, 1),
                match(11, "2026-02-10T20:00:00Z", "FINISHED", "Beta", "Real Madrid", 0, 0),
                match(12, "2099-03-10T20:00:00Z", "TIMED", "Real Madrid", "Gamma", null, null),
                match(13, "2099-04-10T20:00:00Z", "SCHEDULED", "Delta", "Real Madrid", null, null),
                match(14, "2099-02-01T20:00:00Z", "IN_PLAY", "Real Madrid", "Epsilon", 1, 0))));
    }

    @Test
    void syncStoresPlayersSortedByPosition() throws Exception {
        syncService.syncAll();

        mockMvc.perform(get("/api/players")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Amy Keeper"))
                .andExpect(jsonPath("$[0].position").value("GK"))
                // 수비수(Defence, Left-Back)가 이름순으로 그다음
                .andExpect(jsonPath("$[1].name").value("Bob Defender"))
                .andExpect(jsonPath("$[2].name").value("Dan Back"))
                .andExpect(jsonPath("$[2].position").value("DF"))
                .andExpect(jsonPath("$[4].name").value("Zed Forward"))
                .andExpect(jsonPath("$[4].position").value("FW"));
    }

    @Test
    void playerFilterDetailAndErrors() throws Exception {
        syncService.syncAll();

        mockMvc.perform(get("/api/players").param("position", "DF"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/players").param("position", "XX")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        mockMvc.perform(get("/api/players/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PLAYER_NOT_FOUND"));

        Long id = playerRepository.findByExternalId(2L).orElseThrow().getId();
        mockMvc.perform(get("/api/players/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amy Keeper"))
                .andExpect(jsonPath("$.nationality").value("Spain"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-01-02"));
    }

    @Test
    void matchesAreSplitIntoUpcomingAndFinished() throws Exception {
        syncService.syncAll();

        // 예정/진행 중: 킥오프가 가까운 순
        mockMvc.perform(get("/api/matches").param("view", "upcoming")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].awayTeam").value("Epsilon"))
                .andExpect(jsonPath("$[0].status").value("LIVE"))
                .andExpect(jsonPath("$[1].awayTeam").value("Gamma"))
                .andExpect(jsonPath("$[1].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[2].homeTeam").value("Delta"));

        // 종료: 최근 경기가 먼저, 점수 포함
        mockMvc.perform(get("/api/matches").param("view", "finished"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].homeTeam").value("Beta"))
                .andExpect(jsonPath("$[1].homeScore").value(2))
                .andExpect(jsonPath("$[1].awayScore").value(1));

        mockMvc.perform(get("/api/matches").param("view", "finished").param("limit", "1"))
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/matches").param("view", "nonsense")).andExpect(status().isBadRequest());
    }

    @Test
    void matchDetailAndNotFound() throws Exception {
        syncService.syncAll();
        Long id = matchRepository.findByExternalId(10L).orElseThrow().getId();
        mockMvc.perform(get("/api/matches/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.competitionCode").value("PD"))
                .andExpect(jsonPath("$.status").value("FINISHED"));
        mockMvc.perform(get("/api/matches/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MATCH_NOT_FOUND"));
    }

    @Test
    void syncIsIdempotentAndUpdatesScores() throws Exception {
        syncService.syncAll();
        syncService.syncAll();
        assertThat(playerRepository.count()).isEqualTo(5);
        assertThat(matchRepository.count()).isEqualTo(5);

        // 예정 경기가 끝나면 같은 경기가 점수와 함께 갱신된다.
        when(client.fetchMatches()).thenReturn(new MatchesResponse(List.of(
                match(12, "2099-03-10T20:00:00Z", "FINISHED", "Real Madrid", "Gamma", 3, 0))));
        syncService.syncAll();
        var updated = matchRepository.findByExternalId(12L).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(MatchStatus.FINISHED);
        assertThat(updated.getHomeScore()).isEqualTo(3);
        assertThat(matchRepository.count()).isEqualTo(5);
    }

    @Test
    void playersLeavingSquadBecomeInactiveButEmptyResponseIsIgnored() throws Exception {
        syncService.syncAll();

        when(client.fetchTeam()).thenReturn(new TeamResponse(List.of(player(2, "Amy Keeper", "Goalkeeper"))));
        syncService.syncAll();
        mockMvc.perform(get("/api/players")).andExpect(jsonPath("$.length()").value(1));
        assertThat(playerRepository.count()).isEqualTo(5); // 지우지 않고 비활성 처리

        // 비정상적으로 빈 선수단이 와도 기존 선수단을 그대로 둔다.
        when(client.fetchTeam()).thenReturn(new TeamResponse(List.of()));
        syncService.syncAll();
        mockMvc.perform(get("/api/players")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void apiFailureKeepsExistingData() throws Exception {
        syncService.syncAll();
        when(client.fetchTeam()).thenThrow(new IllegalStateException("API down"));
        syncService.syncAll(); // 예외를 밖으로 던지지 않는다.
        mockMvc.perform(get("/api/players")).andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void withoutApiKeyNothingIsSynced() throws Exception {
        when(client.isConfigured()).thenReturn(false);
        syncService.syncAll();
        mockMvc.perform(get("/api/players")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void enumMappings() {
        assertThat(Position.fromApi("Goalkeeper")).isEqualTo(Position.GK);
        assertThat(Position.fromApi("Centre-Back")).isEqualTo(Position.DF);
        assertThat(Position.fromApi("Defensive Midfield")).isEqualTo(Position.MF);
        assertThat(Position.fromApi("Right Winger")).isEqualTo(Position.FW);
        assertThat(Position.fromApi(null)).isEqualTo(Position.MF);
        assertThat(MatchStatus.fromApi("TIMED")).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(MatchStatus.fromApi("PAUSED")).isEqualTo(MatchStatus.LIVE);
        assertThat(MatchStatus.fromApi("AWARDED")).isEqualTo(MatchStatus.FINISHED);
        assertThat(MatchStatus.fromApi("POSTPONED")).isEqualTo(MatchStatus.POSTPONED);
    }
}
