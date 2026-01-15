package com.aston.homework_spring.controller;

import com.aston.homework_spring.model.User;
import com.aston.homework_spring.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Пользователи", description = "API для работы с пользователями")
public class UserController {

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
    public ResponseEntity<User.Out> create(@Valid @RequestBody User.In dto) {
        log.info("User created successfully: {}", dto);
        return new ResponseEntity<>(userService.create(dto), HttpStatus.CREATED);
    }


    @Operation(summary = "Поиск пользователя по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно найден", content =
                    { @Content(mediaType = "application/json", schema = @Schema(implementation = User.Out.class)) }),
            @ApiResponse(responseCode = "404", description = "Пользователь с введенным идентификатором в базе данных " +
                    "не найден", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<User.Out> find(
            @Parameter(description = "Уникальный идентификатор отыскиваемого пользователя")
            @PathVariable Long id
    ) {
        log.info("User with id = {} found successfully", id);
        return new ResponseEntity<>(userService.find(id), HttpStatus.OK);
    }


    @Operation(
            summary = "Обновление данных пользователя",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Схема для обновления данных пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.In.class),
                            examples = @ExampleObject(value =
                                    "{\"name\": \"Ivanov Ivan\", \"email\": \"abc@gmail.com\", \"age\": 30}"))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен", content =
                    { @Content(mediaType = "application/json", schema = @Schema(implementation = User.Out.class)) }),
            @ApiResponse(responseCode = "400", description = "Введены некорректные данные", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь с введенным идентификатором в базе данных" +
                    " не найден", content = @Content),
            @ApiResponse(responseCode = "500", description = "Пользователь с таким email уже существует либо возникла" +
                    " иная ошибка сервера", content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<User.Out> update(
            @Parameter(description = "Уникальный идентификатор обновляемого пользователя") @PathVariable Long id,
            @Valid @RequestBody User.In dto
    ) {
        log.info("User with id = {} updated successfully: {}", id, dto);
        return new ResponseEntity<>(userService.update(id, dto), HttpStatus.OK);
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
        log.info("User with id = {} deleted successfully", id);
        userService.delete(id);
        return new ResponseEntity<>("User deleted successfully from database", HttpStatus.NO_CONTENT);
    }


    @Operation(summary = "Поиск пользователей с пропуском определенного количества и в заданном количестве")
    @ApiResponse(responseCode = "200", description = "Список найденных пользователей", content =
            { @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = User.Out.class)))
            })
    @GetMapping("/get-all-by-offset-limit")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User.Out> getAll(
            @Parameter(description = "Количество пропущенных пользователей начиная с первого", required = true)
            @RequestParam int offset,

            @Parameter(description = "Количество отыскиваемых пользователей", required = true)
            @RequestParam int limit
    ) {
        return userService.getAll(offset, limit);
    }


    @Operation(summary = "Получение общего количества пользователей")
    @ApiResponse(responseCode = "200", description = "Общее количество пользователей в базе данных", content =
            { @Content(mediaType = "application/json", schema = @Schema(implementation = int.class)) }
    )
    @GetMapping("/get-all-count")
    @ResponseStatus(HttpStatus.OK)
    public int getAllCount() {
        return userService.getAllCount();
    }

}
