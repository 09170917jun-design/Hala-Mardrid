package com.halamadrid.backend.player;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // football-data.org의 선수 ID. 동기화할 때 같은 선수를 찾는 기준이다.
    @Column(nullable = false, unique = true)
    private Long externalId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Position position;

    private Integer shirtNumber;

    @Column(length = 60)
    private String nationality;

    private LocalDate dateOfBirth;

    // 현재 선수단에서 빠진 선수는 지우지 않고 비활성으로 둔다. (과거 기록 보존)
    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant syncedAt;

    protected Player() {
    }

    public Player(Long externalId) {
        this.externalId = externalId;
    }

    public void sync(String name, Position position, Integer shirtNumber, String nationality, LocalDate dateOfBirth) {
        this.name = name;
        this.position = position;
        this.shirtNumber = shirtNumber;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.active = true;
        this.syncedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public Long getId() {
        return id;
    }

    public Long getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public Integer getShirtNumber() {
        return shirtNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }
}
