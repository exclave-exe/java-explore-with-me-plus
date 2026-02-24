package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatView;

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
