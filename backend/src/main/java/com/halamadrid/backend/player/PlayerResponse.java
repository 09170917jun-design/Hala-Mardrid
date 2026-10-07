package com.halamadrid.backend.player;

import java.time.LocalDate;

public record PlayerResponse(Long id, String name, Position position, Integer shirtNumber, String nationality,
        LocalDate dateOfBirth) {

    static PlayerResponse from(Player p) {
        return new PlayerResponse(p.getId(), p.getName(), p.getPosition(), p.getShirtNumber(), p.getNationality(),
                p.getDateOfBirth());
    }
}
