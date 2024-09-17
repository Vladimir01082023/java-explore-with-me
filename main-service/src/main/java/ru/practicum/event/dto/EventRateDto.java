package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRateDto {
    private Long eventId;
    private double rate;
    private String description;
    private String annotation;
    private String title;
}
