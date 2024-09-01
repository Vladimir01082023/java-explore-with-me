package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stats")
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "app", nullable = false, length = 64)
    private String app;
    @Column(name = "uri", nullable = false, length = 256)
    private String uri;
    @Column(name = "ip", nullable = false, length = 64)
    private String ip;
    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timestamp;
}
