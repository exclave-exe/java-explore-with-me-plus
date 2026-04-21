package ru.practicum.main.subscribe.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.main.subscribe.model.Subscription;
import ru.practicum.main.user.model.User;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByFollowerIdAndFollowedId(Long userId, Long targetUserId);

    Optional<Subscription> findByFollowerIdAndFollowedId(Long userId, Long targetUserId);

    @Query("""
            select s.follower
            from Subscription s
            where s.followed.id = :userId
            """)
    List<User> findFollowers(Long userId, Pageable pageable);

    @Query("""
            select s.followed
            from Subscription s
            where s.follower.id = :userId
            """)
    List<User> findFollowings(Long userId, Pageable pageable);
}
