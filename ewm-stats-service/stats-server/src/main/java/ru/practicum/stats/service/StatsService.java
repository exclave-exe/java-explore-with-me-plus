package ru.practicum.stats.service;

import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void saveHit(StatRequestDto statRequestDto);

    List<StatResponseDto> getHitStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
