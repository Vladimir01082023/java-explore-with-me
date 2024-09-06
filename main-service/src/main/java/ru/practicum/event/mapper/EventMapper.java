package ru.practicum.event.mapper;

import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

public class EventMapper {

    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto dto = new EventFullDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setCreatedOn(event.getCreatedOn());
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setLocation(event.getLocation());
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        dto.setPublishedOn(event.getPublishedOn());
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState());
        dto.setTitle(event.getTitle());
        dto.setConfirmedRequests(Math.toIntExact(event.getConfirmedRequests()));
        dto.setViews(event.getViews());
        return dto;
    }

    public static EventShortDto toEventShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(CategoryMapper.toCategoryDto(event.getCategory()));
        dto.setConfirmedRequests(dto.getConfirmedRequests());
        dto.setEventDate(event.getEventDate());
        dto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(event.getViews());
        return dto;
    }

    public static Event toEvent(NewEventDto newEventDto, Category category) {
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setLocation(newEventDto.getLocation());
        event.setPaid(newEventDto.isPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setTitle(newEventDto.getTitle());
        return event;
    }

    public static Event toEvent(EventFullDto eventFullDto, Long userId) {
        Event event = new Event();
        event.setId(eventFullDto.getId());
        event.setAnnotation(eventFullDto.getAnnotation());
        event.setCategory(CategoryMapper.toCategory(eventFullDto.getCategory()));
        event.setCreatedOn(eventFullDto.getCreatedOn());
        event.setDescription(eventFullDto.getDescription());
        event.setEventDate(eventFullDto.getEventDate());
        event.setInitiator(UserMapper.toUser(eventFullDto.getInitiator()));
        event.setLocation(eventFullDto.getLocation());
        event.setPaid(eventFullDto.getPaid());
        event.setParticipantLimit(eventFullDto.getParticipantLimit());
        event.setPublishedOn(eventFullDto.getPublishedOn());
        event.setRequestModeration(eventFullDto.getRequestModeration());
        event.setState(eventFullDto.getState());
        event.setTitle(eventFullDto.getTitle());
        event.setConfirmedRequests(Long.valueOf(eventFullDto.getConfirmedRequests()));
        event.setViews(eventFullDto.getViews());
        return event;
    }
}
