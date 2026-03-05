package ru.practicum.main.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.main.request.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class ParticipationRequestDto {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime created;

    private Long event;
    private Long id;
    private Long requester;
    private RequestStatus status;
}
