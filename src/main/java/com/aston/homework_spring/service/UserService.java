package com.aston.homework_spring.service;

import com.aston.homework_spring.model.User;

import java.util.List;

public interface UserService {

    User.Out create(User.In in);

    User.Out find(long id);

    User.Out update(long id, User.In in);

    void delete(long id);

    List<User.Out> getAll(int offset, int limit);

    int getAllCount();

}
