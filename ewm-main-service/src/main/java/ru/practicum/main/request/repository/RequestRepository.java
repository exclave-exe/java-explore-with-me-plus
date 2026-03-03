package ru.practicum.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.request.model.Request;
import ru.practicum.main.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<Request> findAllByEventId(Long eventId);
}
