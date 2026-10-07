package com.halamadrid.backend.football;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Configuration
@EnableScheduling
class SchedulingConfig {
}

/** 서버가 켜져 있는 동안 정기적으로 동기화한다. (서버가 잠들어 있다가 깨어날 때는 조회 시점의 refreshIfStale이 보완한다) */
@Component
class FootballSyncScheduler {

    private final FootballSyncService syncService;

    FootballSyncScheduler(FootballSyncService syncService) {
        this.syncService = syncService;
    }

    @Scheduled(initialDelayString = "PT10S", fixedRateString = "PT6H")
    void run() {
        syncService.syncAll();
    }
}
