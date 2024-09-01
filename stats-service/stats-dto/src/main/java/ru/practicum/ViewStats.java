package ru.practicum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {
    @NonNull
    private String app;
    @NonNull
    private String uri;
    private Long hits;
}
