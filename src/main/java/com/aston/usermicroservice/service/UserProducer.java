package com.aston.usermicroservice.service;

import com.aston.usermicroservice.model.User;

public interface UserProducer {

    void sendEventCreatedUser(User user);

    void sendEventDeletedUser(User user);
}
