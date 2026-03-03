package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventServiceImpl implements PrivateEventService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final StatService statService;
    private final RequestMapper requestMapper;

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        PageRequest pr = PageRequest.of(from, size);

        List<Event> events = eventRepository.findAllByInitiator_Id(userId, pr);

        //TODO заполнить просмотрами и одобренными запросами
        statService.fillListWithViews(events);

        return eventMapper.mapToEventShortDtoList(events);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " и id инициатора " +
                        userId + " не найдено"));

        //TODO заполнить просмотрами и одобренными запросами
        statService.fillWithViews(event);

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

        return eventMapper.mapToEventFullDto(eventRepository.save(event));
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

        //TODO заполнить просмотрами и одобренными запросами
        statService.fillWithViews(event);

        return eventMapper.mapToEventFullDto(event);
    }

    @Override
    @Transactional
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
}