package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto getEventById(Long id, HttpServletRequest request);

    List<EventFullDto> getAllEventsByUserId(Long userId, Pageable pageable);

    EventFullDto addEventByUserId(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByUserId(Long userId, Long eventId);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);


    List<EventFullDto> findAllEventsByAdmin(List<Long> userIds, List<State> states, List<Long> categoryIds,
                                            LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<EventShortDto> findAllEventsByPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                              LocalDateTime rangeEnd, Boolean onlyAvailable, Sort sort, Pageable pageable,
                                              HttpServletRequest request);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest userRequest);
}

