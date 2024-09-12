package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public List<EventFullDto> getAllEvents(@PathVariable Long userId,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GETTING ALL EVENTS");
        return eventService.getAllEventsByUserId(userId, PageRequest.of(from / size, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        log.info("CREATING EVENT BY USER");
        return eventService.addEventByUserId(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto get(@PathVariable Long userId,
                            @PathVariable Long eventId) {
        log.info("FINDING EVENT BY USER");
        return eventService.getEventByUserId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long userId,
                               @PathVariable Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest userRequest) {
        log.info("STARTING UPDATE EVENT BY USER");
        return eventService.updateEventByUser(userId, eventId, userRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsEventByUser(@PathVariable Long userId,
                                                                @PathVariable Long eventId) {
        log.info("CREATING REQUEST EVENT BY USER");
        return requestService.getRequestsEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequests(@PathVariable Long userId,
                                                         @PathVariable Long eventId,
                                                         @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("STARTING UPDATE REQUEST EVENT BY USER");
        return requestService.updateRequestsByUser(userId, eventId, updateRequest);
    }
}
