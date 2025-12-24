package com.aston.homework_spring.service;

import com.aston.homework_spring.exception.EntityNotFoundException;
import com.aston.homework_spring.mapper.UserMapper;
import com.aston.homework_spring.model.User;
import com.aston.homework_spring.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;


    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = {Exception.class}
    )
    public User.Out create(User.In in) {
        User user = new User();
        mapper.updateUserFromUserIn(in, user);
        User createdUser = userRepository.save(user);
        return mapper.toDTO(createdUser);
    }


    @Transactional(readOnly = true)
    public User.Out find(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id = " + id + " not found"));
        return mapper.toDTO(user);
    }


    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = {Exception.class}
    )
    public User.Out update(long id, User.In in) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id = " + id + " not found"));

        mapper.updateUserFromUserIn(in, user);
        User updatedUser = userRepository.save(user);
        return mapper.toDTO(user);
    }


    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = {Exception.class}
    )
    public void delete(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id = " + id + " not found"));
        userRepository.deleteById(user.getId());
    }


    @Transactional(readOnly = true)
    public List<User.Out> getAll(int offset, int limit) {
        List<User> users = userRepository.getAllOffsetLimit(offset, limit);
        return mapper.toDTO(users);
    }


    @Transactional(readOnly = true)
    public int getAllCount() {
        return userRepository.getTotalCountOfUsers();
    }

}
