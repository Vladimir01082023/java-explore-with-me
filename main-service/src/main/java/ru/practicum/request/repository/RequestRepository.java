package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventIdInAndStatus(List<Long> eventId, RequestStatus status);

    List<Request> findAllByRequester(User user);

    Optional<Request> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    List<Request> findAllByIdIn(List<Long> id);

    List<Request> findAllByEvent(Event event);
}
