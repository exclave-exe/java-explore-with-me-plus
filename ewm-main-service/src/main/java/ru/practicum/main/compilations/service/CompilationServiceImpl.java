package ru.practicum.main.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilations.dto.CompilationDto;
import ru.practicum.main.compilations.dto.NewCompilationDto;
import ru.practicum.main.compilations.dto.UpdateCompilationRequest;
import ru.practicum.main.compilations.mapper.CompilationMapper;
import ru.practicum.main.compilations.model.Compilation;
import ru.practicum.main.compilations.repository.CompilationRepository;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.util.PaginationUtil;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Set<Event> events = new HashSet<>();
        Set<EventShortDto> shortEventDtos = new HashSet<>();
        if (newCompilationDto.getEvents() != null) {
            List<Event> eventsList = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            if (eventsList.size() != newCompilationDto.getEvents().size()) {
                throw new NotFoundException("Некоторые события не найдены и не могут быть добавлены в подборку");
            }
            events.addAll(eventsList);
            shortEventDtos.addAll(eventMapper.mapToEventShortDtoList(eventsList));
        }

        Compilation compilation = compilationMapper.fromNewCompilationDto(newCompilationDto, events);
        compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(compilation, shortEventDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        if (compilationRepository.findById(compilationId).isEmpty()) {
            throw new NotFoundException("Подборка с id " + compilationId + " не найдена");
        }
        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest,
                                            Long compilationId) {
        Compilation compilation = compilationRepository.findByIdWithEvents(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compilationId + " не найдена"));
        Set<Event> events = null;
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> foundEvents = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
            if (foundEvents.size() != updateCompilationRequest.getEvents().size()) {
                throw new NotFoundException("Одно или несколько событий не найдены");
            }
            events = new HashSet<>(foundEvents);
        }
        compilationMapper.applyPatch(updateCompilationRequest, compilation, events);
        Set<EventShortDto> eventShortDtos = new HashSet<>(eventMapper.mapToEventShortDtoList(new ArrayList<>(compilation.getEvents())));
        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compilationId + " не найдена"));
        Set<EventShortDto> eventShortDtos = new HashSet<>(eventMapper.mapToEventShortDtoList(new ArrayList<>(compilation.getEvents())));
        return compilationMapper.toCompilationDto(compilation, eventShortDtos);
    }

    @Override
    public List<CompilationDto> getAllCompilations(int from, int size, Boolean pinned) {
        Pageable pageable = PaginationUtil.createPageRequest(from, size);

        List<Long> ids = compilationRepository.findIds(pinned, pageable);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Compilation> compilations = compilationRepository.findAllWithEvents(ids, pageable.getSort());

        return compilations.stream()
                .map(compilation -> compilationMapper.toCompilationDto(compilation
                        , new HashSet<>(eventMapper.mapToEventShortDtoList(new ArrayList<>(compilation.getEvents())))))
                .toList();
    }
}
