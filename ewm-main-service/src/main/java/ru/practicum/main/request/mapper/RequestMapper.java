package ru.practicum.main.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.Request;

import java.util.List;

@Component
public class RequestMapper {

    public ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> mapToListParticipationRequestDto(List<Request> requests) {
        return requests.stream()
                .map(this::mapToParticipationRequestDto)
                .toList();
    }
}
