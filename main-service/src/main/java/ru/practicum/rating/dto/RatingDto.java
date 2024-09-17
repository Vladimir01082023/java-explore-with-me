package ru.practicum.rating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDto {

    @NotNull
    @NotBlank
    Long eventId;

    @NotNull
    @NotBlank
    Long userId;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 10)
    int rate;
}
