package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.UserCreateDto;
import ru.practicum.dto.UserResponseDto;
import ru.practicum.model.User;

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
}
