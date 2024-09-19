package ru.practicum.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.rating.model.Rating;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Rating getById(Long id);

    List<Optional<Rating>> getByEventId(Long eventId);

    List<Rating> getAllByEventId(Long eventId);

}
