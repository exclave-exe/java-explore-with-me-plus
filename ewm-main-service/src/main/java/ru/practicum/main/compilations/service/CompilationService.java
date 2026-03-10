package ru.practicum.main.compilations.service;

import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compilationId);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest,
                                     Long compilationId);

    CompilationDto getCompilationById(Long compilationId);

    List<CompilationDto> getAllCompilations(int from, int size, Boolean pinned);
}
