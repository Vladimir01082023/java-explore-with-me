package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.exception.EmptyStatsException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);

    @Override
    public void postHit(EndpointHit hit) {
        log.info("Posting hit: {}", hit);
        Stats stats = StatsMapper.toStats(hit);
        log.info("New hit was created");
        statsRepository.save(stats);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new EmptyStatsException("Error: start or end can't be null!");
        }
        if (statsRepository.getStatsByUris(start, end, uris).isEmpty()) {
            log.info("No hits found");
            throw new EmptyStatsException("No hits are in DB");
        }
        if (unique) {
            log.info("Stats with unique ip for these uris is found");
            return statsRepository.getAllUniqueStatsInUris(start, end, uris);
        } else
            log.info("Stats for these uris is found");
        return statsRepository.getStatsByUris(start, end, uris);
    }
}
