package ru.practicum.main.subscribe.service;

public interface SubscriptionService {

    void follow(Long userId, Long targetUserId);

    void unfollow(Long userId, Long targetUserId);
}
