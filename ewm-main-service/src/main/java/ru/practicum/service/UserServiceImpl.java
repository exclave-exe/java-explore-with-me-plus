package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserCreateDto;
import ru.practicum.dto.UserResponseDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        validateEmailExists(userCreateDto.getEmail());
        User userToCreate = userMapper.mapToUser(userCreateDto);
        User createdUser = userRepository.save(userToCreate);
        return userMapper.mapToResponseDto(createdUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers(List<Long> ids, Integer from, Integer size) {
        List<User> existingUsers = (ids == null || ids.isEmpty())
                ? userRepository.findAllWithOffset(from, size)
                : userRepository.findByIdsWithOffset(ids, from, size);

        return userMapper.mapToListResponseDto(existingUsers);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    private void validateEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email=" + email + " already exists");
        }
    }
}
