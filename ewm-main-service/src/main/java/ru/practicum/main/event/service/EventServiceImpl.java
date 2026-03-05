package ru.practicum.main.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.mapper.RequestMapper;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.main.event.repository.EventRepository.Specs.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final RequestMapper requestMapper;
    private final StatsClient statsClient;

    @Value("${app.name}")
    private String app;

    @Override
    public List<EventFullDto> getEvents(SearchParamsAdmin searchParamsAdmin) {

        PageRequest pr = PageRequest.of(searchParamsAdmin.getFrom(), searchParamsAdmin.getSize());

        List<Event> events;

        Specification<Event> spec = Specification.where(null);

        if (searchParamsAdmin.getUsers() != null && !searchParamsAdmin.getUsers().isEmpty()) {
            spec = spec.and(inInitiators(searchParamsAdmin.getUsers()));
        }

        if (searchParamsAdmin.getStates() != null && !searchParamsAdmin.getStates().isEmpty()) {
            spec = spec.and(inStates(searchParamsAdmin.getStates()));
        }

        if (searchParamsAdmin.getCategories() != null && !searchParamsAdmin.getCategories().isEmpty()) {
            spec = spec.and(inCategories(searchParamsAdmin.getCategories()));
        }

        if (searchParamsAdmin.getRangeStart() != null && searchParamsAdmin.getRangeEnd() != null) {
            spec = spec.and(betweenTime(searchParamsAdmin.getRangeStart(), searchParamsAdmin.getRangeEnd()));
        } else {
            spec = spec.and(afterNow());
        }

        events = eventRepository.findAll(spec, pr).getContent();

        enrichEvents(events);

        return eventMapper.mapToEventFullDtoList(events);
    }

    @Override
    public List<EventShortDto> getEvents(SearchParams searchParams, HttpServletRequest request) {
        PageRequest pr = PageRequest.of(searchParams.getFrom(), searchParams.getSize(),
                getSort(searchParams.getSort()));

        List<Event> events;

        Specification<Event> specification = Specification.where(byState(EventState.PUBLISHED));

        if (searchParams.getText() != null) {
            specification = specification.and(byAnnotationOrDescription(searchParams.getText()));
        }
        if (searchParams.getPaid() != null) {
            specification =  specification.and(byPaid(searchParams.getPaid()));
        }
        if (searchParams.getCategories() != null && !searchParams.getCategories().isEmpty()) {
            specification = specification.and(inCategories(searchParams.getCategories()));
        }
        if (searchParams.getRangeStart() != null && searchParams.getRangeEnd() != null) {
            specification = specification.and(betweenTime(searchParams.getRangeStart(), searchParams.getRangeEnd()));
        } else {
            specification = specification.and(afterNow());
        }

        //TODO сделать по человечески
        events = eventRepository.findAll(specification, pr).getContent();

        enrichEvents(events);

        if (searchParams.getOnlyAvailable() == true) {
            events = events.stream()
                    .filter(e -> e.getConfirmedRequests() < e.getParticipantLimit())
                    .toList();
        }

        hit(app, request);

        return eventMapper.mapToEventShortDtoList(events);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        PageRequest pr = PageRequest.of(from, size);

        List<Event> events = eventRepository.findAllByInitiator_Id(userId, pr);

        enrichEvents(events);

        return eventMapper.mapToEventShortDtoList(events);
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndStateIs(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с id " + id + " не найдено."));

        enrichEvent(event);

        hit(app, request);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " и id инициатора " +
                        userId + " не найдено"));

        enrichEvent(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория " + newEventDto.getCategory() + " не найдена."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь " + userId + " не найден."));

        if (!newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2)))
            throw new BadRequestException("Дата и время не могут быть раньше чем через два часа от текущего момента");

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .title(newEventDto.getTitle())
                .eventDate(newEventDto.getEventDate())
                .description(newEventDto.getDescription())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestedModeration(newEventDto.getRequestModeration())
                .category(category)
                .createdOn(LocalDateTime.now())
                .state(EventState.PENDING)
                .initiator(user)
                .build();

        log.info("Creating event: {}", event);

        return eventMapper.mapToEventFullDto(eventRepository.save(event));
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
                }
                break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        enrichEvent(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = false)
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!Objects.equals(event.getInitiator().getId(), user.getId()))
            throw new ConflictException("Изменять событие может только инициатор.");

        if (event.getState().equals(EventState.PUBLISHED))
            throw new ConflictException("Опубликованные события нельзя изменять");

        if (updateEventUserRequest.getAnnotation() != null)
            event.setAnnotation(updateEventUserRequest.getAnnotation());

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id " +
                            updateEventUserRequest.getCategory() + " не найдена")));
        }

        if (updateEventUserRequest.getDescription() != null)
            event.setDescription(updateEventUserRequest.getDescription());

        if (updateEventUserRequest.getEventDate() != null) {

            if (updateEventUserRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2)))
                event.setEventDate(updateEventUserRequest.getEventDate());
            else
                throw new BadRequestException("Дата изменяемого события должна быть минимум через 2 часа от текущей");

        } else if (!event.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Дата изменяемого события должна быть минимум через 2 часа от текущей");
        }

        if (updateEventUserRequest.getLocation() != null)
            event.setLocation(updateEventUserRequest.getLocation());

        if (updateEventUserRequest.getPaid() != null)
            event.setPaid(updateEventUserRequest.getPaid());

        if (updateEventUserRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());

        if (updateEventUserRequest.getRequestModeration() != null)
            event.setRequestedModeration(updateEventUserRequest.getRequestModeration());

        if (updateEventUserRequest.getTitle() != null)
            event.setTitle(updateEventUserRequest.getTitle());

        if (updateEventUserRequest.getStateAction() != null) {
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }

        enrichEvent(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = false)
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId,
                                                                Long eventId,
                                                                EventRequestStatusUpdateRequest dto) {
        // Проверка на User
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        // Проверка на Initiator
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("User is not the initiator of the event");
        }

        List<Long> requestIds = dto.getRequestIds();
        if (requestIds == null || requestIds.isEmpty()) {
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(List.of())
                    .build();
        }

        List<Request> requests = requestRepository.findAllById(requestIds);

        // Проверка на существование всех переданных запросов
        if (requests.size() != requestIds.size()) {
            throw new NotFoundException("Some participation requests were not found");
        }

        // Проверка запросов, что они относятся к одному Event
        boolean hasForeignEvent = requests.stream()
                .anyMatch(r -> !r.getEvent().getId().equals(eventId));
        if (hasForeignEvent) {
            throw new ConflictException("Some requests do not belong to event id=" + eventId);
        }

        // Менять статус можно только у PENDING
        boolean hasNotPending = requests.stream()
                .anyMatch(r -> r.getStatus() != RequestStatus.PENDING);
        if (hasNotPending) {
            throw new ConflictException("Only PENDING requests can be updated");
        }

        // Проверка статуса на допустимость
        RequestStatus targetStatus = dto.getStatus();
        if (targetStatus != RequestStatus.CONFIRMED && targetStatus != RequestStatus.REJECTED) {
            throw new ConflictException("Only CONFIRMED/REJECTED are allowed");
        }

        // REJECTED: отклоняем переданные заявки
        if (targetStatus == RequestStatus.REJECTED) {
            requests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
            List<Request> saved = requestRepository.saveAll(requests);

            List<ParticipationRequestDto> toResponse = requestMapper.mapToListParticipationRequestDto(saved);
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(toResponse)
                    .build();
        }

        // CONFIRMED: учитываем лимит
        Integer limit = event.getParticipantLimit();
        if (limit != null && limit > 0) {
            long alreadyConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            long available = limit - alreadyConfirmed;

            if (available <= 0) {
                throw new ConflictException("Participant limit reached");
            }

            List<Request> toConfirm = new ArrayList<>();
            List<Request> toReject = new ArrayList<>();

            for (Request r : requests) {
                if (available > 0) {
                    r.setStatus(RequestStatus.CONFIRMED);
                    toConfirm.add(r);
                    available--;
                } else {
                    r.setStatus(RequestStatus.REJECTED);
                    toReject.add(r);
                }
            }

            requestRepository.saveAll(requests);

            // Отклоняем оставшиеся запросы
            long confirmedNow = toConfirm.size();
            long totalConfirmed = alreadyConfirmed + confirmedNow;
            if (totalConfirmed >= limit) {
                List<Request> pending = requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING);

                pending.forEach(r -> r.setStatus(RequestStatus.REJECTED));
                requestRepository.saveAll(pending);
            }

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(requestMapper.mapToListParticipationRequestDto(toConfirm))
                    .rejectedRequests(requestMapper.mapToListParticipationRequestDto(toReject))
                    .build();
        }

        // CONFIRMED: без лимита
        requests.forEach(r -> r.setStatus(RequestStatus.CONFIRMED));
        List<Request> saved = requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestMapper.mapToListParticipationRequestDto(saved))
                .rejectedRequests(List.of())
                .build();
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new ConflictException("User is not the initiator of the event");
        }

        List<Request> requests = requestRepository.findAllByEventId(eventId);
        return requestMapper.mapToListParticipationRequestDto(requests);
    }

    //получение сортировки из параметра
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

    //методы для статистики и одобренных запросов
    private void hit(String appName, HttpServletRequest request) {
        statsClient.hit(StatRequestDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    public void enrichEvent(Event event) {
        //тут требуется приведение типов поскольку одобренные реквесты в репозитории почему то long
        //надеюсь никогда не будет больше 2,147,483,647 подтвержденных заявок
        event.setConfirmedRequests((int) requestRepository.countByEventIdAndStatus(event.getId(),
                RequestStatus.CONFIRMED));

        //использовать LocalDateTime.MIN и MAX хочется но что-то не так с encode методом в клиенте статистики
        //дольше 70 лет не прослужит энивей
        List<StatResponseDto> stats = statsClient
                .getStats(LocalDateTime.of(2020, 1, 1, 0, 0),
                        LocalDateTime.of(2100, 1, 1, 0, 0),
                        List.of("/events/" + event.getId()), true);

        long views = stats.isEmpty() ? 0L : stats.getFirst().getHits();

        event.setViews(views);
    }

    public void enrichEvents(List<Event> events) {
        if (events == null || events.isEmpty())
            return;

        List<Long> eventIds = new ArrayList<>();
        List<String> uris = new ArrayList<>();

        for (Event event : events) {
            Long eventId = event.getId();
            eventIds.add(eventId);
            uris.add("/events/" + eventId);
        }

        List<StatResponseDto> stats = statsClient
                .getStats(LocalDateTime.of(2020, 1, 1, 0, 0),
                        LocalDateTime.of(2100, 1, 1, 0, 0), uris, true);

        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<String, Long> statsMap = new HashMap<>();
        Map<Long, Integer> requestsMap = new HashMap<>();

        for (StatResponseDto stat : stats) {
            statsMap.put(stat.getUri(), stat.getHits());
        }

        for (Request request : requests) {
            Long eventId = request.getEvent().getId();
            requestsMap.put(eventId, requestsMap.getOrDefault(eventId, 0) + 1);
        }

        for (Event event : events) {
            String uri = "/events/" + event.getId();
            event.setViews(statsMap.getOrDefault(uri, 0L));
            event.setConfirmedRequests(requestsMap.getOrDefault(event.getId(), 0));
        }
    }
}
