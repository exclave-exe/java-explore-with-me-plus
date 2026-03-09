package ru.practicum.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.main.event.model.Location;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000, message = "Аннотация должна быть 20-2000 символов")
    private String annotation;

    @Positive
    private Long category;

    @NotBlank
    @Size(min = 20, max = 7000, message = "Описание должно быть 20-7000 символов")
    private String description;

    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventDate;

    private Location location;

    @JsonProperty(defaultValue = "false")
    private Boolean paid;

    @PositiveOrZero
    @JsonProperty(defaultValue = "0")
    private Integer participantLimit;

    @JsonProperty(defaultValue = "true")
    private Boolean requestModeration;

    @NotBlank
    @Size(min = 3, max = 120, message = "Заголовок должен быть 3-120 символов")
    private String title;

    //значения по умолчанию
    public Integer getParticipantLimit() {
        return participantLimit == null ? 0 : participantLimit;
    }

    public Boolean getPaid() {
        return paid != null && paid;
    }

    public Boolean getRequestModeration() {
        return requestModeration == null || requestModeration;
    }
}
