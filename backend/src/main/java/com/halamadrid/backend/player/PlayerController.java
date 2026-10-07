package com.halamadrid.backend.player;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<PlayerResponse> list(@RequestParam(required = false) Position position) {
        return playerService.list(position);
    }

    @GetMapping("/{id}")
    public PlayerResponse get(@PathVariable Long id) {
        return playerService.get(id);
    }
}
