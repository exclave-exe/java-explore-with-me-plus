package ru.practicum.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.event.model.EventState;
import ru.practicum.main.event.model.Location;
import ru.practicum.main.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventFullDto {
    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private Boolean paid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    private UserShortDto initiator;
    private String description;
    private Integer participantLimit;
    private EventState state;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime publishedOn;

    private Location location;
    private Boolean requestModeration;
    private long views;
    private int confirmedRequests;
}
