package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEventServiceImpl implements AdminEventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatService statService;

    @Override
    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        int from,
                                        int size) {
        PageRequest pr = PageRequest.of(from, size);

        List<Event> events;

        if (rangeStart != null && rangeEnd != null) {
            if (rangeEnd.isBefore(rangeStart))
                throw new BadRequestException("Временные промежутки запроса не имеют смысла");

            events = eventRepository.getAllEventsInRange(users, states, categories, rangeStart, rangeEnd, pr);
        } else {
            events = eventRepository.getAllEvents(users, states, categories, LocalDateTime.now(), pr);
        }

        //TODO заполнить просмотрами и одобренными запросами
        statService.fillListWithViews(events);

        return eventMapper.mapToEventFullDtoList(events);
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getState().equals(EventState.PENDING))
            throw new ConflictException("Событие можно опубликовать или отклонить если оно в состоянии ожидания");

        if (updateEventAdminRequest.getAnnotation() != null)
            event.setAnnotation(updateEventAdminRequest.getAnnotation());

        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " +
                            updateEventAdminRequest.getCategory() + " не найдена")));
        }

        if (updateEventAdminRequest.getDescription() != null)
            event.setDescription(updateEventAdminRequest.getDescription());

        if (updateEventAdminRequest.getEventDate() != null) {

            if (updateEventAdminRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(1)))
                event.setEventDate(updateEventAdminRequest.getEventDate());
            else
                throw new BadRequestException("Дата изменяемого события должна быть минимум через час от текущей");

        } else if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Дата изменяемого события должна быть минимум через час от текущей");
        }

        if (updateEventAdminRequest.getLocation() != null)
            event.setLocation(updateEventAdminRequest.getLocation());

        if (updateEventAdminRequest.getPaid() != null)
            event.setPaid(updateEventAdminRequest.getPaid());

        if (updateEventAdminRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());

        if (updateEventAdminRequest.getRequestModeration() != null)
            event.setRequestedModeration(updateEventAdminRequest.getRequestModeration());

        if (updateEventAdminRequest.getTitle() != null)
            event.setTitle(updateEventAdminRequest.getTitle());

        if (updateEventAdminRequest.getStateAction() != null) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT: {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } break;
                case REJECT_EVENT: event.setState(EventState.CANCELED); break;
            }
        }

        //TODO заполнить просмотрами и одобренными запросами
        statService.fillWithViews(event);

        return eventMapper.mapToEventFullDto(event);
    }
}
