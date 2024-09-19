package ru.practicum.rating.mapper;

import ru.practicum.event.model.Event;
import ru.practicum.rating.dto.RatingDto;
import ru.practicum.rating.model.Rating;
import ru.practicum.user.model.User;

public class RatingMapper {

    public static RatingDto toRatingDto(Rating rating) {
        RatingDto ratingDto = new RatingDto();
        ratingDto.setEventId(ratingDto.getEventId());
        ratingDto.setRate(rating.getRate());
        ratingDto.setUserId(rating.getUser().getId());
        return ratingDto;
    }

    public static Rating toRating(RatingDto ratingDto, User user, Event event) {
        Rating rating = new Rating();
        rating.setEvent(event);
        rating.setUser(user);
        rating.setRate(ratingDto.getRate());
        return rating;
    }
}
