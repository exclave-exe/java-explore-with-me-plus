package ru.practicum.main.user.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.user.dto.UserShortDto;
import ru.practicum.main.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@Validated

@RequiredArgsConstructor
@Slf4j
public class PublicUserController {

    private final UserService userService;

    @GetMapping("/followers")
    public List<UserShortDto> getUserFollowers(@PathVariable @Positive Long userId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Getting user followers: userId={}, from={}, size={}", userId, from, size);
        return userService.getUserFollowers(userId, from, size);
    }

    @GetMapping("/following")
    public List<UserShortDto> getUserFollowing(@PathVariable @Positive Long userId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Getting user following: userId={}, from={}, size={}", userId, from, size);
        return userService.getUserFollowing(userId, from, size);
    }
}
