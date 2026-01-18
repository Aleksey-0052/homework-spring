package com.aston.user_microservice.service;

import com.aston.user_microservice.exception.EntityNotFoundException;
import com.aston.user_microservice.mapper.UserMapper;
import com.aston.user_microservice.model.User;
import com.aston.user_microservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Profile("!api")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final UserProducer userProducer;


    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = {Exception.class}
    )
    @Override
    public User.Out create(User.In in) {
        User user = new User();
        mapper.updateUserFromUserIn(in, user);
        User createdUser = userRepository.save(user);

        userProducer.sendEventCreatedUser(createdUser);
        return mapper.toDTO(createdUser);
    }


    @Transactional(readOnly = true)
    @Override
    public User.Out find(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id = " + id + " not found"));
        return mapper.toDTO(user);
    }


    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = {Exception.class}
    )
    @Override
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
    @Override
    public void delete(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id = " + id + " not found"));
        userRepository.deleteById(user.getId());

        userProducer.sendEventDeletedUser(user);
    }


    @Transactional(readOnly = true)
    @Override
    public List<User.Out> getAll(int offset, int limit) {
        List<User> users = userRepository.getAllOffsetLimit(offset, limit);
        return mapper.toDTO(users);
    }


    @Transactional(readOnly = true)
    @Override
    public int getAllCount() {
        return userRepository.getTotalCountOfUsers();
    }

}
