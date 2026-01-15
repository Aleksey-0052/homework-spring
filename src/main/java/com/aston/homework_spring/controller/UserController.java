package com.aston.homework_spring.controller;

import com.aston.homework_spring.model.User;
import com.aston.homework_spring.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;


    @PostMapping
    public ResponseEntity<User.Out> create(@Valid @RequestBody User.In dto) {
        log.info("User created successfully: {}", dto);
        return new ResponseEntity<>(userService.create(dto), HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<User.Out> find(@PathVariable Long id) {
        log.info("User with id = {} found successfully", id);
        return new ResponseEntity<>(userService.find(id), HttpStatus.OK);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<User.Out> update(@PathVariable Long id, @Valid @RequestBody User.In dto) {
        log.info("User with id = {} updated successfully: {}", id, dto);
        return new ResponseEntity<>(userService.update(id, dto), HttpStatus.OK);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        log.info("User with id = {} deleted successfully", id);
        userService.delete(id);
        return new ResponseEntity<>("User deleted successfully from database", HttpStatus.NO_CONTENT);
    }


    @GetMapping("/get-all-by-offset-limit")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User.Out> getAll(@RequestParam int offset, @RequestParam int limit) {
        return userService.getAll(offset, limit);
    }


    @GetMapping("/get-all-count")
    @ResponseStatus(HttpStatus.OK)
    public int getAllCount() {
        return userService.getAllCount();
    }

}
