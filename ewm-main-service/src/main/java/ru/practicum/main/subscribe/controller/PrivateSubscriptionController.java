package ru.practicum.main.subscribe.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.subscribe.service.SubscriptionService;

@RestController
@RequestMapping("/users/{userId}/subscriptions/{targetUserId}")
@Validated

@RequiredArgsConstructor
@Slf4j
public class PrivateSubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable @Positive Long userId,
                       @PathVariable @Positive Long targetUserId) {
        log.info("Following user: userId={}, targetUserId={}", userId, targetUserId);
        subscriptionService.follow(userId, targetUserId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable @Positive Long userId,
                         @PathVariable @Positive Long targetUserId) {
        log.info("Unfollowing user: userId={}, targetUserId={}", userId, targetUserId);
        subscriptionService.unfollow(userId, targetUserId);
    }
}
