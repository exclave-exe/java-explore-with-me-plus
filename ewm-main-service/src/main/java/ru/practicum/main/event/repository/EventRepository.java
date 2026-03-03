package ru.practicum.main.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    //TODO изменить запросы в которых есть параметр onlyAvailable для учета количества участников и одобренных запросов

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE ((upper(e.annotation) LIKE upper(concat('%', ?1, '%'))) OR (upper(e.description) LIKE upper(concat('%', ?1, '%')))) " +
            "AND (e.category.id IN ?2 OR ?2 IS null) " +
            "AND (e.state = PUBLISHED) " +
            "AND (e.paid = ?3 OR ?3 IS null) " +
            "AND (e.eventDate BETWEEN ?4 AND ?5) " +
            "AND (?6 = false AND e.participantLimit > 0)")
    List<Event> getPublishedEventsWithRange(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd,
                                            Boolean onlyAvailable,
                                            PageRequest pageRequest);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE ((upper(e.annotation) LIKE upper(concat('%', ?1, '%'))) OR (upper(e.description) LIKE upper(concat('%', ?1, '%')))) " +
            "AND (e.category.id IN ?2 OR ?2 IS null) " +
            "AND (e.state = PUBLISHED) " +
            "AND (e.paid = ?3 OR ?3 IS null) " +
            "AND (e.eventDate > CURRENT_TIMESTAMP) " +
            "AND (?4 = false AND e.participantLimit > 0)")
    List<Event> getPublishedEvents(String text,
                                   List<Long> categories,
                                   Boolean paid,
                                   Boolean onlyAvailable,
                                   PageRequest pageRequest);

    Optional<Event> findByIdAndStateIs(Long eventId, EventState state);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (e.initiator.id IN ?1 OR ?1 IS null) " +
            "AND (e.state IN ?2 OR ?2 IS null) " +
            "AND (e.category.id IN ?3 OR ?3 IS null) " +
            "AND (e.eventDate BETWEEN ?4 AND ?5) ")
    List<Event> getAllEventsInRange(List<Long> users, List<String> states, List<Long> categories,
                                    LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (e.initiator.id IN ?1 OR ?1 IS null) " +
            "AND (e.state IN ?2 OR ?2 IS null) " +
            "AND (e.category.id IN ?3 OR ?3 IS null) " +
            "AND (e.eventDate > CURRENT_TIMESTAMP) ")
    List<Event> getAllEvents(List<Long> users, List<String> states, List<Long> categories,
                             LocalDateTime timestamp, PageRequest pageRequest);

    List<Event> findAllByInitiator_Id(Long userId, PageRequest pageRequest);

    Optional<Event> findByIdAndInitiator_Id(Long eventId, Long userId);

    boolean existsByCategory_Id(Long categoryId);

    Optional<Event> findById(Long id);

    boolean existsByIdAndInitiatorId(Long eventId, Long userId);
}
