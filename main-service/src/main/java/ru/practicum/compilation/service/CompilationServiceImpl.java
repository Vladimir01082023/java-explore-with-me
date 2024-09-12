package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.util.CheckExistence;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CheckExistence checkExistence;

    @Override
    @Transactional
    public CompilationDto addCompilationByAdmin(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
        }
        Compilation compilation = new Compilation();
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setEvents(events);
        compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public void deleteCompilationByAdmin(Long compId) {
        checkExistence.getCompilation(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilationByAdmin(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = checkExistence.getCompilation(compId);
        if (request.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(request.getEvents()));
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilationByPublic(Boolean pinned, PageRequest pageable) {
        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, pageable)
                    .stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(pageable)
                .stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return CompilationMapper.toCompilationDto(checkExistence.getCompilation(compId));
    }
}