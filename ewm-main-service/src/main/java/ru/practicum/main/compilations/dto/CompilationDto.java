package ru.practicum.main.compilations.dto;

import lombok.Data;
import ru.practicum.main.event.dto.EventShortDto;

import java.util.Set;

@Data
public class CompilationDto {

    private Long id;
    private Boolean pinned;
    private String title;
    private Set<EventShortDto> events;

}
