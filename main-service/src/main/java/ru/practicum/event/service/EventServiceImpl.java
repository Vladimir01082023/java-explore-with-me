package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.ViewStats;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.*;
import ru.practicum.event.enums.Sort;
import ru.practicum.event.enums.State;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.util.CheckExistence;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.practicum.event.enums.Sort.VIEWS;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private static final LocalDateTime CURRENT_TIME = LocalDateTime.now();
    private static final int DESCRIPTION_MAX_LENGTH = 7000;
    private static final int DESCRIPTION_MIN_LENGTH = 20;
    private static final int ANNOTATION_MAX_LENGTH = 2000;
    private static final int ANNOTATION_MIN_LENGTH = 20;
    private static final int TITLE_MAX_LENGTH = 120;
    private static final int TITLE_MIN_LENGTH = 3;
    private static final String APP_NAME = "ewm-main-service";

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;
    private final CheckExistence checkExistence;
    private final LocationRepository locationRepository;
    private final CategoryService categoryService;
    private final RatingRepository ratingRepository;

    @Override
    public List<EventFullDto> findAllEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                                   LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        if (states == null && rangeStart == null && rangeEnd == null) {
            return eventRepository.findAll(pageable)
                    .stream()
                    .map(EventMapper::toEventFullDto)
                    .collect(Collectors.toList());
        }
        if (states == null) {
            states = Stream.of(State.values())
                    .collect(Collectors.toList());
        }

        rangeStart = rangeStart == null ? CURRENT_TIME.minusYears(5) : rangeStart;
        rangeEnd = rangeEnd == null ? CURRENT_TIME.plusYears(5) : rangeEnd;

        checkExistence.checkStartAndEnd(rangeStart, rangeEnd);
        List<Event> events = eventRepository.findByParams(
                users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                pageable);

        return events.stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = checkExistence.getEvent(eventId);

        if (request.getEventDate() != null) {
            checkStartTime(request.getEventDate());
            event.setEventDate(request.getEventDate());
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case REJECT_EVENT:
                    checkEventStatus(event, request, List.of(State.PENDING));
                    event.setState(State.CANCELED);
                    break;
                case PUBLISH_EVENT:
                    checkEventStatus(event, request, List.of(State.PENDING));
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(CURRENT_TIME);
                    break;
            }
        }

        return EventMapper.toEventFullDto(eventRepository.save(updateEvent(event, request)));
    }

    @Override
    public List<EventShortDto> findAllEventsByPublic(String text,
                                                     List<Long> categories,
                                                     Boolean paid,
                                                     LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd,
                                                     Boolean onlyAvailable,
                                                     Sort sort,
                                                     Pageable pageable,
                                                     HttpServletRequest request) {

        rangeStart = rangeStart == null ? CURRENT_TIME : rangeStart;
        rangeEnd = rangeEnd == null ? CURRENT_TIME.plusYears(15) : rangeEnd;
        text = text == null ? "" : text;

        checkExistence.checkStartAndEnd(rangeStart, rangeEnd);

        List<Event> events = eventRepository.findByParamsOrderByDate(
                text.toLowerCase(),
                List.of(State.PUBLISHED),
                categories,
                paid,
                rangeStart,
                rangeEnd,
                pageable);

        statsClient.postHit(APP_NAME, request.getRequestURI(), request.getRemoteAddr(), CURRENT_TIME);
        List<Event> eventList = setViewsAndConfirmedRequests(events);

        if (sort != null && sort.equals(VIEWS)) {
            eventList.sort((e1, e2) -> e2.getViews().compareTo(e1.getViews()));
        }

        if (onlyAvailable) {
            return eventList.stream()
                    .filter(event -> event.getParticipantLimit() <= event.getConfirmedRequests())
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
        } else {
            return eventList.stream()
                    .map(EventMapper::toEventShortDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    @Override
    public EventFullDto getEventById(Long id, HttpServletRequest request) {
        Event event = checkExistence.getEvent(id);

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException(String.format("Event %d is not published", event.getId()));
        }

        statsClient.postHit(APP_NAME, request.getRequestURI(), request.getRemoteAddr(), CURRENT_TIME);
        List<ViewStats> viewStatsList = statsClient.getStats(List.of(id), true);
        long hits = viewStatsList
                .stream()
                .filter(s -> Objects.equals(s.getUri(), request.getRequestURI()))
                .count();

        event.setViews(hits + 1);
        event.setConfirmedRequests((long) requestRepository.findAllByEventIdInAndStatus(List.of(id),
                RequestStatus.CONFIRMED).size());
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getAllEventsByUserId(Long userId, Pageable pageable) {
        checkExistence.getUser(userId);
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto addEventByUserId(Long userId, NewEventDto newEventDto) {

        User user = checkExistence.getUser(userId);

        if (newEventDto.getEventDate().isBefore(CURRENT_TIME.plusHours(2))) {
            throw new ValidationException("Datetime of the event must be in two hours from now");
        }

        Event event = EventMapper.toEvent(newEventDto,
                CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory())));
        event.setState(State.PENDING);
        event.setCreatedOn(CURRENT_TIME);
        event.setConfirmedRequests(0L);

        Category category = checkExistence.getCategory(newEventDto.getCategory());

        event.setCategory(category);
        event.setInitiator(user);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByUserId(Long userId, Long eventId) {
        Event event = checkExistence.getEvent(eventId);
        User user = checkExistence.getUser(userId);

        checkEventOwner(event, user);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId,
                                          Long eventId,
                                          UpdateEventUserRequest request) {

        Event event = EventMapper.toEvent(getEventByUserId(userId, eventId));

        checkEventStatus(event, request, List.of(State.PENDING, State.CANCELED));

        checkDate(request);

        if (StateAction.SEND_TO_REVIEW == request.getStateAction()) {
            event.setState(State.PENDING);
        }
        if (StateAction.CANCEL_REVIEW == request.getStateAction()) {
            event.setState(State.CANCELED);
        }

        return EventMapper.toEventFullDto(eventRepository.save(updateEvent(event, request)));
    }

    @Override
    public EventRateDto getRateOfEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ValidationException("Event with id " + eventId + " is not rated");
        }

        List<Optional<Rating>> listOfRating = ratingRepository.getByEventId(eventId);

        if (listOfRating.size() == 0){
            throw new ValidationException(" does not exist");
        }

        double rate = listOfRating.stream()
                .map(rating -> rating.get().getRate())
                .mapToInt(Integer::intValue)
                .average()
                .getAsDouble();

        Event event = checkExistence.getEvent(eventId);
        log.info("FINDING RATE OF EVENT");
        return EventMapper.toEventRateDto(event, rate);
    }

    private Event updateEvent(Event event, UpdateEventRequest request) {
        return constructEvent(event, request);
    }

    private List<Event> setViewsAndConfirmedRequests(List<Event> events) {
        List<Long> eventIds = events
                .stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        List<ViewStats> viewStatsList = statsClient.getStats(eventIds, false);
        Map<Long, Long> views;
        if (viewStatsList != null && !viewStatsList.isEmpty()) {
            views = viewStatsList
                    .stream()
                    .collect(Collectors.toMap(this::getEventIdFromURI, ViewStats::getHits));
        } else {
            views = Collections.emptyMap();
        }

        events.forEach(event ->
                event.setViews(views.getOrDefault(event.getId(), 0L)));

        List<Request> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(new ArrayList<>(eventIds),
                RequestStatus.CONFIRMED);

        events.forEach(event ->
                event.setConfirmedRequests((long) confirmedRequests.size()));

        return events
                .stream()
                .map(eventRepository::save)
                .collect(Collectors.toList());
    }

    private Long getEventIdFromURI(ViewStats viewStats) {
        return Long.parseLong(viewStats.getUri().substring(viewStats.getUri().lastIndexOf("/") + 1));
    }

    private void checkStartTime(LocalDateTime time) {
        if (CURRENT_TIME.isAfter(time)) {
            throw new ValidationException("Start must be before the end");
        }
    }

    private void checkDate(UpdateEventRequest request) {
        if ((request != null && request.getEventDate() != null) &&
                !request.getEventDate().isAfter(CURRENT_TIME.plusHours(2))) {
            throw new ValidationException("Datetime of the event must be in two hours from now");
        }
    }

    private void checkEventStatus(Event event, UpdateEventRequest request, List<State> allowedState) {
        boolean valid = false;
        for (State state : allowedState) {
            if (event.getState() == state) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new ConflictException(String.format("Event can be published only from %s status", allowedState) +
                    request.getStateAction());
        }
    }

    private void checkEventOwner(Event event, User user) {

        if (!event.getInitiator().equals(user)) {
            throw new NotFoundException(String.format("User %s not the owner of the event %d",
                    user.getName(), event.getId()));
        }
    }

    private void checkDescription(UpdateEventRequest request) {
        if (request.getDescription().length() > DESCRIPTION_MAX_LENGTH || request.getDescription().length() < DESCRIPTION_MIN_LENGTH) {
            throw new ValidationException("Can't be shorter than " + DESCRIPTION_MIN_LENGTH + " and longer than " + DESCRIPTION_MAX_LENGTH);
        }
    }

    private void checkAnnotation(UpdateEventRequest request) {
        if (request.getAnnotation().length() > ANNOTATION_MAX_LENGTH || request.getAnnotation().length() < ANNOTATION_MIN_LENGTH) {
            throw new ValidationException("Can't be shorter than " + ANNOTATION_MIN_LENGTH + " and longer than " + ANNOTATION_MAX_LENGTH);
        }
    }

    private void checkTitle(UpdateEventRequest request) {
        if (request.getTitle().length() < TITLE_MIN_LENGTH || request.getTitle().length() > TITLE_MAX_LENGTH) {
            throw new ValidationException("Can't be shorter than " + TITLE_MIN_LENGTH + " and longer than " + TITLE_MAX_LENGTH);
        }
    }

    private Event constructEvent(Event event, UpdateEventRequest request) {

        if (request.getAnnotation() != null) {
            checkAnnotation(request);
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getCategory() != null) {
            event.setCategory(checkExistence.getCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            checkDescription(request);
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            checkTitle(request);
            event.setTitle(request.getTitle());
        }
        return event;
    }
}
