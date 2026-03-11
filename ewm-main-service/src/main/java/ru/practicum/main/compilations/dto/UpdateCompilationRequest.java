package ru.practicum.main.compilations.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateCompilationRequest {

    private Set<Long> events;

    @Size(max = 50)
    private String title;
    private Boolean pinned;
}
