package ru.practicum.rating.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.enums.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ValidationException;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.mapper.RatingMapper;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.util.CheckExistence;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CheckExistence checkExistence;

    @Override
    public RatingDto addRate(RatingDto ratingDto) {
        checkExistence.getUser(ratingDto.getUserId());
        checkExistence.getEvent(ratingDto.getEventId());

        if (!checkEventPublished(ratingDto.getEventId())) {
            throw new ValidationException("Event is not published");
        }
        log.info("Add rating {} to {}", ratingDto.getUserId(), ratingDto.getEventId());

        ratingRepository.save(RatingMapper.toRating(ratingDto, userRepository.findById(ratingDto.getUserId()).get(),
                eventRepository.findById(ratingDto.getEventId()).get()));

        return ratingDto;
    }

    @Override
    public RatingDto updateRate(RatingDto ratingDto, Long ratingId) {
        checkExistence.getUser(ratingDto.getUserId());
        checkExistence.getEvent(ratingDto.getEventId());

        if (!checkEventPublished(ratingDto.getEventId())) {
            throw new ValidationException("Event is not published");
        }

        if (!ratingRepository.existsById(ratingId)) {
            throw new ValidationException(ratingId + " does not exist");
        }

        if (!ratingRepository.getById(ratingId).getUser().getId().equals(ratingDto.getUserId()) &&
                !ratingRepository.getById(ratingId).getEvent().getId().equals(ratingDto.getEventId())) {
            throw new ValidationException(String.format("User {} never rated event {}",
                    ratingDto.getUserId(), ratingDto.getEventId()));
        }

        if (ratingDto.getRate() == ratingRepository.findById(ratingId).get().getRate()) {
            throw new ValidationException("The updating rate is equal to the rate in BD");
        }

        Rating rating = ratingRepository.getById(ratingId);
        rating.setRate(ratingDto.getRate());
        rating.setId(ratingId);
        ratingRepository.save(rating);
        log.info("User {} updated rating of event {} with rate {}",
                ratingDto.getUserId(), ratingDto.getEventId(), ratingDto.getRate());

        return ratingDto;
    }

    @Override
    @Transactional
    public void deleteRate(Long userId, Long ratingId) {

        checkExistence.getRating(ratingId);

        if (ratingRepository.getById(ratingId) == null) {
            throw new ValidationException(ratingId + " does not exist");
        }

        if (!ratingRepository.getById(ratingId).getUser().getId().equals(userId)) {
            throw new ValidationException("Only author of rate can delete it!");
        }

        log.info("User {} deleted his rate to event {}", userId, ratingId);
        ratingRepository.deleteById(ratingId);
    }

    public boolean checkEventPublished(Long eventId) {
        State state = eventRepository.getById(eventId).getState();
        if (state.equals(State.PUBLISHED)) {
            return true;
        }
        return false;
    }
}
