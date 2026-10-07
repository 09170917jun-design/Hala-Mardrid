package com.halamadrid.backend.player;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.halamadrid.backend.common.ApiException;
import com.halamadrid.backend.football.FootballSyncService;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final FootballSyncService syncService;

    public PlayerService(PlayerRepository playerRepository, FootballSyncService syncService) {
        this.playerRepository = playerRepository;
        this.syncService = syncService;
    }

    /** 현재 선수단. 골키퍼 → 수비 → 미드필더 → 공격 순, 같은 포지션은 이름순. */
    @Transactional(readOnly = true)
    public List<PlayerResponse> list(Position position) {
        syncService.refreshIfStale();
        return playerRepository.findByActiveTrue().stream()
                .filter(p -> position == null || p.getPosition() == position)
                .sorted(Comparator.comparing(Player::getPosition).thenComparing(Player::getName))
                .map(PlayerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse get(Long id) {
        return playerRepository.findById(id).filter(Player::isActive).map(PlayerResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PLAYER_NOT_FOUND", "선수를 찾을 수 없습니다."));
    }
}
