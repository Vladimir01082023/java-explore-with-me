package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
public class PublicCompilationController {

    private static final Logger log = LoggerFactory.getLogger(PublicCompilationController.class);
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> get(@RequestParam(required = false) Boolean pinned,
                                    @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                    @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GETTING LIST OF COMPILATIONS");
        return compilationService.getCompilationByPublic(pinned, PageRequest.of(from / size, size));
    }

    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable Long compId) {
        log.info("GETTING COMPILATION BY ID {}", compId);
        return compilationService.getCompilationById(compId);
    }
}
