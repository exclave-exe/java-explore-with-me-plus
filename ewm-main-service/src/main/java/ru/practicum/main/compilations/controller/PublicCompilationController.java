package ru.practicum.main.compilations.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable(name = "compId") @Positive Long compilationId) {
        return compilationService.getCompilationById(compilationId);
    }

    @GetMapping
    public List<CompilationDto> getAllCompilations(@RequestParam(name = "pinned",
                                                           required = false) Boolean pinned,
                                                   @RequestParam(name = "from",
                                                           defaultValue = "0") @PositiveOrZero int from,
                                                   @RequestParam(name = "size",
                                                           defaultValue = "10") @Positive int size) {
        return compilationService.getAllCompilations(from, size, pinned);

    }
}
