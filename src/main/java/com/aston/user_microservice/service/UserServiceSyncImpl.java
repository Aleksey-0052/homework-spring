package com.aston.user_microservice.service;

import com.aston.user_microservice.exception.EntityNotFoundException;
import com.aston.user_microservice.mapper.UserMapper;
import com.aston.user_microservice.model.User;
import com.aston.user_microservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Profile("api")
public class UserServiceSyncImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final RestTemplate restTemplate;


    @Transactional(
            isolation = Isolation.REPEATABLE_READ,
            rollbackFor = {Exception.class}
    )
    @CircuitBreaker(name = "mailService")
    @Retry(name = "myRetry")
    @Override
    public User.Out create(User.In in) {
        User user = new User();
        mapper.updateUserFromUserIn(in, user);
        User createdUser = userRepository.save(user);

        String emailUrl = "http://localhost:email/simple-email/type-operation-create?email="
                + createdUser.getEmail() + "&name=" + createdUser.getName();
        ResponseEntity<String> messageResponse = restTemplate.getForEntity(emailUrl, String.class);

        return mapper.toDTO(createdUser);
    }


    @Transactional(
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = {Exception.class}
    )
    @CircuitBreaker(name = "mailService")
    @Retry(name = "myRetry")
    @Override
    public void delete(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id = " + id + " not found"));
        userRepository.deleteById(user.getId());

        String email = user.getEmail();
        String name = user.getName();
        String emailUrl = "http://localhost:email/simple-email/type-operation-delete/" + email + "/" + name;
        ResponseEntity<String> messageResponse = restTemplate.getForEntity(emailUrl, String.class);
    }


    @Override
    public User.Out find(long id) {
        return null;
    }


    @Override
    public User.Out update(long id, User.In in) {
        return null;
    }


    @Override
    public List<User.Out> getAll(int offset, int limit) {
        return List.of();
    }


    @Override
    public int getAllCount() {
        return 0;
    }

}
