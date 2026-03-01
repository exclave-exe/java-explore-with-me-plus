package ru.practicum.main.event.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private double lat;
    private double lon;
}
