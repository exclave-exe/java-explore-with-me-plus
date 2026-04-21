package ru.practicum.main.subscribe.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.exception.BadRequestException;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.subscribe.model.Subscription;
import ru.practicum.main.subscribe.repository.SubscriptionRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) throw new BadRequestException("User cannot follow himself");

        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
        User followed = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("User with id=" + targetUserId + " not found"));

        boolean alreadyExists = subscriptionRepository.existsByFollowerIdAndFollowedId(userId, targetUserId);
        if (alreadyExists) throw new ConflictException("User already subscribed to this user");

        Subscription subscription = Subscription.builder()
                .follower(follower)
                .followed(followed)
                .created(LocalDateTime.now())
                .build();
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {

        if (userId.equals(targetUserId)) throw new BadRequestException("User cannot unfollow himself");

        Subscription subscription = subscriptionRepository.findByFollowerIdAndFollowedId(userId, targetUserId)
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        subscriptionRepository.delete(subscription);
    }

}
