package ru.practicum.main.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.user.dto.UserCreateDto;
import ru.practicum.main.user.dto.UserResponseDto;
import ru.practicum.main.user.mapper.UserMapper;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.main.exception.ConflictException;

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
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    private void validateEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email=" + email + " already exists");
        }
    }
}
