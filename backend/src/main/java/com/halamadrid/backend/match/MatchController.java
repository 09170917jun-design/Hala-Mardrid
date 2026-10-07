package com.halamadrid.backend.match;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchResponse> list(@RequestParam(defaultValue = "upcoming") String view,
            @RequestParam(defaultValue = "100") int limit) {
        return matchService.list(view, limit);
    }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable Long id) {
        return matchService.get(id);
    }
}
