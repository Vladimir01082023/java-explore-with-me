package ru.practicum.mapper;

import ru.practicum.EndpointHit;
import ru.practicum.model.Stats;

public class StatsMapper {

    public static Stats toStats(EndpointHit endpointHit) {
        Stats stats = new Stats();
        stats.setApp(endpointHit.getApp());
        stats.setIp(endpointHit.getIp());
        stats.setUri(endpointHit.getUri());
        stats.setTimestamp(endpointHit.getTimestamp());
        return stats;
    }
}
