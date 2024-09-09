package ru.practicum.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void postHit(@Validated @RequestBody EndpointHit hit) {
        log.info("Posting hit: {}", hit);
        statsService.postHit(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Getting stats");
        if (start.isAfter(end)) {
            throw new ValidationException(
                    String.format("Unexpected time interval: start %s; end %s", start, end));
        }
        return statsService.getStats(start, end, uris, unique);
    }
}
