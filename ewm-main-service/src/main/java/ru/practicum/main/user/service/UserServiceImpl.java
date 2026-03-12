package ru.practicum.main.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.subscribe.repository.SubscriptionRepository;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.dto.UserShortDto;
import ru.practicum.main.user.mapper.UserMapper;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.main.util.PaginationUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        validateEmailExists(newUserRequest.getEmail());
        User userToCreate = userMapper.mapToUser(newUserRequest);
        User createdUser = userRepository.save(userToCreate);
        return userMapper.mapToResponseDto(createdUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
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

    @Override
    public List<UserShortDto> getUserFollowers(Long userId, int from, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(from, size);
        List<User> followers = subscriptionRepository.findFollowers(userId, pageable);
        return userMapper.mapToListUserShortDto(followers);
    }

    @Override
    public List<UserShortDto> getUserFollowing(Long userId, int from, int size) {
        Pageable pageable = PaginationUtil.createPageRequest(from, size);
        List<User> followings = subscriptionRepository.findFollowings(userId, pageable);
        return userMapper.mapToListUserShortDto(followings);
    }

    private void validateEmailExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email=" + email + " already exists");
        }
    }
}
