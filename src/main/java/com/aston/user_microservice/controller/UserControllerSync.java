package com.aston.user_microservice.controller;

import com.aston.user_microservice.model.User;
import com.aston.user_microservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "API для работы с пользователями")
@Profile("api")
public class UserControllerSync {

    private final UserService userService;


    @Operation(
            summary = "Создание нового пользователя",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Схема для создания пользователя. При создании пользователя и сохранении его в базу " +
                            "данных пользователю будет присвоен идентификатор, следующий за идентификатором последнего " +
                            "когда-либо создаваемого пользователя в базе данных",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.In.class),
                            examples = @ExampleObject(value =
                                    "{\"name\": \"Ivanov Ivan\", \"email\": \"abc@gmail.com\", \"age\": 30}")
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан", content =
                    { @Content(mediaType = "application/json", schema = @Schema(implementation = User.Out.class)) }),
            @ApiResponse(responseCode = "400", description = "Введены некорректные данные", content = @Content),
            @ApiResponse(responseCode = "500", description = "Пользователь с таким email уже существует либо возникла" +
                    " иная ошибка сервера", content = @Content)
    })
    @PostMapping
    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "recoverMethod")
    public ResponseEntity<User.Out> create(@Valid @RequestBody User.In dto) {
        User.Out out = userService.create(dto);
        log.info("User created successfully: {}", out);
        return new ResponseEntity<>(out, HttpStatus.CREATED);
    }


    @Operation(summary = "Удаление пользователя по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален", content =
                    { @Content(mediaType = "application/json", schema = @Schema(implementation = User.Out.class)) }),
            @ApiResponse(responseCode = "404", description = "Пользователь с введенным идентификатором в базе данных " +
                    "не найден", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @Parameter(description = "Уникальный идентификатор удаляемого пользователя")
            @PathVariable Long id
    ) {
        userService.delete(id);
        log.info("User with id = {} deleted successfully", id);
        return new ResponseEntity<>("User deleted successfully from database", HttpStatus.NO_CONTENT);
    }

}
