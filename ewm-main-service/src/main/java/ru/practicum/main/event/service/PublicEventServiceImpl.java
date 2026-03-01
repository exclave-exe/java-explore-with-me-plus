package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {

    private final EventRepository eventRepository;
    private final StatService statService;
    private final EventMapper eventMapper;

    @Value("${app.name}")
    private String app;

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         String sort,
                                         int from,
                                         int size,
                                         HttpServletRequest request) {

        PageRequest pr = PageRequest.of(from, size, getSort(sort));

        List<Event> eventList;

        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart))
                throw new BadRequestException("Временные промежутки запроса не имеют смысла");

            eventList = eventRepository
                    .getPublishedEventsWithRange(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pr);
        } else {
            eventList = eventRepository
                    .getPublishedEvents(text, categories, paid, onlyAvailable, pr);
        }

        statService.fillListWithViews(eventList);
        //TODO заполнить одобренными заявками на участие

        statService.hit(app, request);

        return eventMapper.mapToEventShortDtoList(eventList);
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {

        Event event = eventRepository.findByIdAndStateIs(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с id " + id + " не найдено."));

        statService.fillWithViews(event);
        //TODO заполнить одобренными заявками на участие

        statService.hit(app, request);

        return eventMapper.mapToEventFullDto(event);
    }

    private Sort getSort(String sort) {
        String sortBy;

        if (sort == null) {
            sortBy = "id";
        } else if (sort.equals("EVENT_DATE")) {
            sortBy = "eventDate";
            return Sort.by(sortBy).ascending();
        } else if (sort.equals("VIEWS")) {
            sortBy = "views";
            return Sort.by(sortBy).descending();
        } else {
            sortBy = "id";
        }

        return Sort.by(sortBy).ascending();
    }
}
