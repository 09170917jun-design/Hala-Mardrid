package com.halamadrid.backend.match;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halamadrid.backend.common.ApiException;
import com.halamadrid.backend.football.FootballSyncService;

@Service
public class MatchService {

    private static final int MAX_LIMIT = 100;

    private final MatchRepository matchRepository;
    private final FootballSyncService syncService;

    public MatchService(MatchRepository matchRepository, FootballSyncService syncService) {
        this.matchRepository = matchRepository;
        this.syncService = syncService;
    }

    /** view: upcoming(예정·진행 중, 가까운 순) 또는 finished(종료, 최근 순). */
    @Transactional(readOnly = true)
    public List<MatchResponse> list(String view, int limit) {
        syncService.refreshIfStale();
        Limit size = Limit.of(Math.min(Math.max(limit, 1), MAX_LIMIT));
        List<FootballMatch> matches = switch (view) {
            case "upcoming" -> matchRepository.findByStatusInOrderByKickoffAtAsc(
                    Set.of(MatchStatus.SCHEDULED, MatchStatus.LIVE), size);
            case "finished" -> matchRepository.findByStatusInOrderByKickoffAtDesc(Set.of(MatchStatus.FINISHED), size);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                    "view는 upcoming 또는 finished만 가능합니다.");
        };
        return matches.stream().map(MatchResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse get(Long id) {
        return matchRepository.findById(id).map(MatchResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MATCH_NOT_FOUND", "경기를 찾을 수 없습니다."));
    }
}
