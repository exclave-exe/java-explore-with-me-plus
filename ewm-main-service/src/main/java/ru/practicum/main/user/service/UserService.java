package ru.practicum.main.user.service;

import ru.practicum.main.user.dto.UserCreateDto;
import ru.practicum.main.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(UserCreateDto userCreateDto);

    List<UserResponseDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

}
