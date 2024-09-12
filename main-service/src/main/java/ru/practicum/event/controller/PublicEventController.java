package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;

    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public List<EventShortDto> getAllEvents(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false) Sort sort,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                            @Positive @RequestParam(defaultValue = "10") int size,
                                            HttpServletRequest request) {
        log.info("GETTING ALL PUBLISHED EVENTS");
        return eventService.findAllEventsByPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                PageRequest.of(from / size, size), request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEventById(id, request);
    }
}
