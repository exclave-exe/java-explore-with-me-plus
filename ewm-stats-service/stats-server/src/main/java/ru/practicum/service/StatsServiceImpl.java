package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatRequestDto;
import ru.practicum.dto.StatResponseDto;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatView;
import ru.practicum.repository.StatsRepository;

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
