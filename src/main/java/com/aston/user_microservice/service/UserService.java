package com.aston.user_microservice.service;

import com.aston.user_microservice.model.User;

import java.util.List;

public interface UserService {

    User.Out create(User.In in);

    User.Out find(long id);

    User.Out update(long id, User.In in);

    void delete(long id);

    List<User.Out> getAll(int offset, int limit);

    int getAllCount();

}
