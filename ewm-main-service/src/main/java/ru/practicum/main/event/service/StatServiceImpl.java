package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.event.model.Event;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatsClient statsClient;

    @Override
    public void hit(String appName, HttpServletRequest request) {
        statsClient.hit(StatRequestDto.builder()
                        .app(appName)
                        .uri(request.getRequestURI())
                        .ip(request.getRemoteAddr())
                        .timestamp(LocalDateTime.now())
                .build());
    }

    @Override
    public void fillWithViews(Event event) {

        //тут лучше использовать LocalDateTime.MIN и MAX но поскольку требуется паттерн именно в параметрах контроллера
        //сервис статистики выбрасывает исключение о невозможности конвертации
        //просмотры считаются только от уникальных ip
        List<StatResponseDto> stats = statsClient
                .getStats(LocalDateTime.of(2020, 1, 1, 0, 0),
                        LocalDateTime.of(2100, 1, 1, 0, 0),
                        List.of("/events/" + event.getId()), true);

        long views = stats.isEmpty() ? 0L : stats.getFirst().getHits();

        event.setViews(views);
    }

    @Override
    public void fillListWithViews(List<Event> events) {
        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        List<StatResponseDto> stats = statsClient
                .getStats(LocalDateTime.of(2020, 1, 1, 0, 0),
                        LocalDateTime.of(2100, 1, 1, 0, 0), uris, true);

        Map<String, Long> statsMap = new HashMap<>();

        for (StatResponseDto stat : stats) {
            statsMap.put(stat.getUri(), stat.getHits());
        }

        for (Event event : events) {
            String uri = "/events/" + event.getId();
            event.setViews(statsMap.getOrDefault(uri, 0L));
        }
    }
}
