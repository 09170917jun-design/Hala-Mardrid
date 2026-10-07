package com.halamadrid.backend.player;

/** 선언 순서가 화면 정렬 순서(골키퍼 → 수비 → 미드필더 → 공격)다. */
public enum Position {
    GK,
    DF,
    MF,
    FW;

    /** football-data.org의 포지션 문자열("Goalkeeper", "Defence", "Left-Back", "Offence" 등)을 변환한다. */
    public static Position fromApi(String value) {
        String v = value == null ? "" : value.toLowerCase();
        if (v.contains("goalkeeper")) {
            return GK;
        }
        // "Defensive Midfield"는 수비수가 아니라 미드필더이므로 미드필더를 먼저 판별한다.
        if (v.contains("midfield")) {
            return MF;
        }
        if (v.contains("defen") || v.contains("back")) {
            return DF;
        }
        if (v.contains("offence") || v.contains("forward") || v.contains("winger") || v.contains("striker")) {
            return FW;
        }
        return MF;
    }
}
