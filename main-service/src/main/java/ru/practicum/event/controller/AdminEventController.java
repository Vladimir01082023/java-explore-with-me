package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.enums.State;
import ru.practicum.event.service.EventService;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private static final Logger log = LoggerFactory.getLogger(AdminEventController.class);
    private final EventService eventService;

    private static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    public List<EventFullDto> get(@RequestParam(required = false) List<Long> users,
                                  @RequestParam(required = false) List<State> states,
                                  @RequestParam(required = false) List<Long> categoriesId,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeStart,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = FORMATTER) LocalDateTime rangeEnd,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                  @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GETTING LIST OF EVENTS");
        return eventService.findAllEventsByAdmin(users, states, categoriesId, rangeStart, rangeEnd,
                PageRequest.of(from / size, size));
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long eventId,
                               @RequestBody UpdateEventAdminRequest request) {
        log.info("UPDATING EVENT {}", eventId);
        return eventService.updateEventByAdmin(eventId, request);
    }
}
