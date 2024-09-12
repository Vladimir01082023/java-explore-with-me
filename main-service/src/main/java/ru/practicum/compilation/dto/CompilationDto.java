package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    @NotNull
    private Long id;

    private List<EventShortDto> events;

    @NotNull
    private Boolean pinned;

    @NotNull
    private String title;
}
