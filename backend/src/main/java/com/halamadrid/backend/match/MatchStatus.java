package com.halamadrid.backend.match;

public enum MatchStatus {
    SCHEDULED,
    LIVE,
    FINISHED,
    POSTPONED,
    CANCELLED;

    /** football-data.org 상태값(SCHEDULED, TIMED, IN_PLAY, PAUSED, FINISHED, AWARDED, POSTPONED ...)을 변환한다. */
    public static MatchStatus fromApi(String value) {
        if (value == null) {
            return SCHEDULED;
        }
        return switch (value) {
            case "IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTY_SHOOTOUT" -> LIVE;
            case "FINISHED", "AWARDED" -> FINISHED;
            case "POSTPONED", "SUSPENDED" -> POSTPONED;
            case "CANCELLED" -> CANCELLED;
            default -> SCHEDULED; // SCHEDULED, TIMED
        };
    }
}
