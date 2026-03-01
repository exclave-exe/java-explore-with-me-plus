package ru.practicum.main.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.user.dto.UserCreateDto;
import ru.practicum.main.user.dto.UserResponseDto;
import ru.practicum.main.user.dto.UserShortDto;
import ru.practicum.main.user.model.User;

import java.util.List;

@Component
public class UserMapper {

    public User mapToUser(UserCreateDto userCreateDto) {
        return User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .build();
    }

    public UserResponseDto mapToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserResponseDto> mapToListResponseDto(List<User> users) {
        return users.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public UserShortDto mapToUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
