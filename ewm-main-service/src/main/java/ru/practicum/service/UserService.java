package ru.practicum.service;

import ru.practicum.dto.UserCreateDto;
import ru.practicum.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(UserCreateDto userCreateDto);

    List<UserResponseDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);

}
