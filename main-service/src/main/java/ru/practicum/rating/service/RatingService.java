package ru.practicum.rating.service;

import ru.practicum.rating.dto.RatingDto;

public interface RatingService {
    RatingDto addRate(RatingDto ratingDto);

    RatingDto updateRate(RatingDto ratingDto, Long ratingId);

    void deleteRate(Long ratingId, Long userId);

}
