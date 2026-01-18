package com.aston.user_microservice.service;

import com.aston.user_microservice.model.User;

public interface UserProducer {

    void sendEventCreatedUser(User user);

    void sendEventDeletedUser(User user);
}
