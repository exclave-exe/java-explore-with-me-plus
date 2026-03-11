package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;
import ru.practicum.stats.exception.BadRequestException;
import ru.practicum.stats.mapper.StatsMapper;
import ru.practicum.stats.model.Stats;
import ru.practicum.stats.repository.StatView;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    public void saveHit(StatRequestDto statRequestDto) {
        Stats stats = statsMapper.mapToStats(statRequestDto);
        statsRepository.save(stats);
    }

    @Override
    public List<StatResponseDto> getHitStatistic(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start)) {
            throw new BadRequestException("End time must not be before start time");
        }

        boolean hasUris = uris != null && !uris.isEmpty();

        List<StatView> rows;
        if (unique) {
            rows = hasUris
                    ? statsRepository.findUniqueStatsWithUris(start, end, uris)
                    : statsRepository.findUniqueStats(start, end);
        } else {
            rows = hasUris
                    ? statsRepository.findStatsWithUris(start, end, uris)
                    : statsRepository.findStats(start, end);
        }
        return statsMapper.mapToListStatResponseDto(rows);
    }
}
