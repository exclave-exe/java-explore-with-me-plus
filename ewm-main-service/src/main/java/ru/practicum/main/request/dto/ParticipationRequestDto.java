package ru.practicum.main.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.main.request.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipationRequestDto {
    private LocalDateTime created;
    private Long event;
    private Long id;
    private Long requester;
    private RequestStatus status;
}
