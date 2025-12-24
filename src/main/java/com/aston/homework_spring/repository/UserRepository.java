package com.aston.homework_spring.repository;

import com.aston.homework_spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {


    @Query(value = "select u.* from users u offset :OFFSET limit :LIMIT", nativeQuery = true)
    List<User> getAllOffsetLimit(@Param("OFFSET") int offset, @Param("LIMIT") int limit);


    @Query(value = "select count(*) from users", nativeQuery = true)
    Integer getTotalCountOfUsers();


}
