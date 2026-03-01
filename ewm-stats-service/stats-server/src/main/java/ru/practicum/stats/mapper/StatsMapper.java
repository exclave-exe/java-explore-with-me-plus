package ru.practicum.stats.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;
import ru.practicum.stats.model.Stats;
import ru.practicum.stats.repository.StatView;

import java.util.List;

@Component
public class StatsMapper {
    public Stats mapToStats(StatRequestDto statRequestDto) {
        return Stats.builder()
                .app(statRequestDto.getApp())
                .uri(statRequestDto.getUri())
                .ip(statRequestDto.getIp())
                .timestamp(statRequestDto.getTimestamp())
                .build();
    }

    public List<StatResponseDto> mapToListStatResponseDto(List<StatView> statViews) {
        return statViews.stream()
                .map(v -> StatResponseDto.builder()
                        .app(v.getApp())
                        .uri(v.getUri())
                        .hits(v.getHits())
                        .build())
                .toList();
    }
}
