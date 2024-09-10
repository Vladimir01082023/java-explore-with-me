package ru.practicum.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.util.CheckExistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.event.enums.State.PUBLISHED;
import static ru.practicum.request.enums.RequestStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final CheckExistence checkExistence;

    @Override
    public List<ParticipationRequestDto> findByRequestorId(Long userId) {
        User user = checkExistence.getUser(userId);
        return requestRepository.findAllByRequester(user).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        User user = checkExistence.getUser(userId);
        Event event = checkExistence.getEvent(eventId);

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(event.getRequestModeration() ? PENDING : CONFIRMED)
                .build();

        Request req = requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ConflictException("Repeated request!"));
//        if (req.isPresent()) {
//            throw new ConflictException("Repeated request!");
//        }
        if (event.getInitiator().equals(user)) {
            throw new ConflictException("Event owner can't participate in event");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("You can participate only in published events");
        }
        if (event.getParticipantLimit() > 0) {
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ConflictException("Participation limit is reached");
            }
        } else {
            request.setStatus(CONFIRMED);
        }

        if (request.getStatus() == CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = checkExistence.getUser(userId);
        Request request = checkExistence.getRequest(requestId);

        if (!request.getRequester().equals(user)) {
            throw new ConflictException("You can canceled only your request");
        }

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsEventByUser(Long userId, Long eventId) {
        User user = checkExistence.getUser(userId);
        Event event = checkExistence.getEvent(eventId);

        if (!event.getInitiator().equals(user)) {
            throw new ValidationException("Only initiator can accept the request");
        }
        return requestRepository.findAllByEvent(event)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsByUser(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest updateRequest) {

        User user = checkExistence.getUser(userId);
        Event event = checkExistence.getEvent(eventId);

        if (!Objects.equals(event.getInitiator().getId(), user.getId())) {
            log.info(String.format("Throwing error: accept owner: %d, current user: %d",
                    event.getInitiator().getId(), user.getId()));
            throw new ValidationException("Only initiator can accept the request");
        }
        if (event.getParticipantLimit() <= event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictException("Participation limit is reached");
        }

        List<Request> requestList = requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        if (requestList.size() != updateRequest.getRequestIds().size()) {
            throw new ConflictException("Events not found");
        }

        switch (updateRequest.getStatus()) {
            case CONFIRMED:
                return updateConfirmedStatus(requestList, event);
            case REJECTED:
                return updateRejectedStatus(requestList);
            default:
                throw new ValidationException("Incorrect status");
        }
    }

    private EventRequestStatusUpdateResult updateConfirmedStatus(List<Request> requestList, Event event) {
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Event status must be PENDING");
            }
            if (event.getParticipantLimit() <= event.getConfirmedRequests()) {
                request.setStatus(REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            } else {
                request.setStatus(CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmed.add(RequestMapper.toParticipationRequestDto(request));
            }
            requestRepository.save(request);
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    private EventRequestStatusUpdateResult updateRejectedStatus(List<Request> requestList) {
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : requestList) {
            if (!request.getStatus().equals(PENDING)) {
                throw new ConflictException("Event status must be PENDING");
            }
            request.setStatus(REJECTED);
            requestRepository.save(request);
            rejected.add(RequestMapper.toParticipationRequestDto(request));
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(rejected)
                .build();
    }
}
