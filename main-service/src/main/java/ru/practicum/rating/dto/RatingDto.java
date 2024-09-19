package ru.practicum.rating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDto {

    @NotBlank
    Long eventId;

    @NotBlank
    Long userId;

    @NotBlank
    @Size(min = 1, max = 10)
    int rate;
}
