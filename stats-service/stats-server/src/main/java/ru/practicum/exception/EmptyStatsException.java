package ru.practicum.exception;

import java.util.NoSuchElementException;

public class EmptyStatsException extends NoSuchElementException {
    public EmptyStatsException(String message) {
        super(message);
    }
}

