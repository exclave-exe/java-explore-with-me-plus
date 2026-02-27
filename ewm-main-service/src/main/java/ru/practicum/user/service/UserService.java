package ru.practicum.user.service;

import ru.practicum.user.dto.UserCreateDto;
import ru.practicum.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(UserCreateDto userCreateDto);

    List<UserResponseDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

}
