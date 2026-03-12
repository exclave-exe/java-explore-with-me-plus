package ru.practicum.main.subscribe.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.main.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne
    @JoinColumn(name = "followed_id")
    private User followed;

    private LocalDateTime created;
}
