package com.aston.usermicroservice.mapper;

import com.aston.usermicroservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User.Out toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created_at", ignore = true)
    void updateUserFromUserIn(User.In dto, @MappingTarget User user);

    List<User.Out> toDTO(List<User> users);

}
