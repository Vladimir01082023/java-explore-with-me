package ru.practicum.util;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.rating.model.Rating;
import ru.practicum.rating.repository.RatingRepository;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class CheckExistence {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final CompilationRepository compilationRepository;
    private final RatingRepository ratingRepository;


    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Event %d not found", id))
        );
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("User id %d not found", id))
        );
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Category id %d not found", id))
        );
    }

    public Request getRequest(Long id) {
        return requestRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Request id %d not found", id))
        );
    }

    public Compilation getCompilation(Long id) {
        return compilationRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Compilation id %d not found", id))
        );
    }

    public Rating getRating(Long id) {
        return ratingRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("Rating %d not found", id))
        );
    }

    public void checkCategoryExistence(NewCategoryDto category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new ConflictException(String.format("Category %s already exists",
                    category.getName()));
        }
    }

    public void checkStartAndEnd(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Start can't be after the end");
        }
    }
}
