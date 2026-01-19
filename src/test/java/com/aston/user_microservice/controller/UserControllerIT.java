package com.aston.user_microservice.controller;

import com.aston.user_microservice.config.LocalDateTimeDeserializer;
import com.aston.user_microservice.config.LocalDateTimeSerializer;
import com.aston.user_microservice.model.User;
import com.aston.user_microservice.service.UserProducerImpl;
import com.aston.user_microservice.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends ContainerIT {

    // Перед запуском тестов необходимо запустить Config Server, Eureka Server, Api Gateway

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserProducerImpl userProducer;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private User.In in;
    private User.Out out;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        //objectMapper.registerModule(new JavaTimeModule());
        // Используется стандартный модуль. Чтобы он включился в работу, необходимо отключить создание бина
        // в классе JacksonConfig.
        objectMapper.registerModule(new SimpleModule().addSerializer(LocalDateTime.class, new LocalDateTimeSerializer()));
        // Используется кастомный модуль, который форматирует дату и время в заданном формате.
        // Этот модуль реализуют классы: JacksonConfig, LocalDateTimeSerializer LocalDateTimeDeserializer.
        objectMapper.registerModule(new SimpleModule().addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer()));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When get all users then get user id=4, get user id=5, get user id=6")
    public void whenGetAllUsers_thenSuccess() throws Exception {

        // В базу данных загружено 10 пользователей
        List<User.Out> expected =
                new ArrayList<>(List.of(userService.find(4L), userService.find(5L), userService.find(6L)));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/get-all-by-offset-limit")
                        .param("offset", "3")
                        .param("limit", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expected)));
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When get all count users then return 10 users")
    public void whenGetAllCountUsers_thenSuccess() throws Exception {

        // В базу данных загружено 10 пользователей
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/get-all-count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(10)));
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When get user by id then return user id=5")
    public void whenGetUser_thenSuccess() throws Exception {

        // На основании пользователя с id = 5, находящегося в базе данных, подготовим ожидаемого User.Out
        User.Out out = User.Out.builder()
                .id(5L)
                .name("testName5")
                .email("test5@gmail.com")
                .age(38)
                .created_at(LocalDateTime.of(2025, 12, 15,
                        14, 6, 44, 555))
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(out.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(out.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(out.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(out.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created_at").exists());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When get user by id=20 then return EntityNotFoundException")
    public void whenGetUser_thenReturnEntityNotFoundException() throws Exception {

        // Пользователь с id = 20 отсутствует в базе данных
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/users/{id}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When create user then return user with id=11")
    public void whenCreateUser_thenSuccess() throws Exception {

        // Создаем нового одиннадцатого пользователя, которого будем сохранять в базу данных
        User.In in = User.In.builder()
                .name("testName11")
                .email("test11@gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        // Создаем нового одиннадцатого пользователя, которого будем ожидать на выходе из контроллера
        User.Out out = User.Out.builder()
                .id(11L)
                .name("testName11")
                .email("test11@gmail.com")
                .age(30)
                .build();

        doNothing().when(userProducer).sendEventCreatedUser(any(User.class));

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(out.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(out.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(out.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(out.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created_at").exists());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When create user with email=test10@gmail.com then return Exception")
    public void whenCreateUserWithDuplicateEmail_thenReturnException() throws Exception {

        // Создаем пользователя с адресом электронной почты, который уже имеется в базе данных
        User.In in = User.In.builder()
                .name("testName11")
                .email("test10@gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        doNothing().when(userProducer).sendEventCreatedUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When create user with invalid email then return Exception")
    public void whenCreateUserWithInvalidEmail_thenReturnException() throws Exception {

        // Создаем пользователя с невалидным адресом электронной почты
        User.In in = User.In.builder()
                .name("testName11")
                .email("test11gmail.com")
                .age(17)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        doNothing().when(userProducer).sendEventCreatedUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When create user with invalid name then return Exception")
    public void whenCreateUserWithInvalidName_thenReturnException() throws Exception {

        // Создаем пользователя с именем состоящим из 26 символов
        User.In in = User.In.builder()
                .name("aaaaaaaaaaaaaaannnnnnnnnnn")
                .email("test11@gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        doNothing().when(userProducer).sendEventCreatedUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When create user with invalid age then return Exception")
    public void whenCreateUserWithInvalidAge_thenReturnException() throws Exception {

        // Создаем пользователя с возрастом 17 лет
        User.In in = User.In.builder()
                .name("testName11")
                .email("test11@gmail.com")
                .age(17)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        doNothing().when(userProducer).sendEventCreatedUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When update user with id=10 then success")
    public void whenUpdateUser_thenSuccess() throws Exception {

        // В базе данных имеется пользователь с id = 10
        User user = new User();
        user.setId(10L);
        user.setName("testName10");
        user.setEmail("test10@gmail.com");
        user.setAge(42);
        user.setCreated_at(LocalDateTime.of(2025, 12, 16, 2, 6, 44, 555));

        // Создаем User.In, которого будем использовать для обновления десятого пользователя
        User.In in = User.In.builder()
                .name("testName12")
                .email("test12@gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        // Создаем User.Out, которого будем ожидать на выходе из контроллера
        User.Out out = User.Out.builder()
                .id(10L)
                .name("testName12")
                .email("test12@gmail.com")
                .age(30)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(out.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(out.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(out.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(out.getAge()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.created_at").exists());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When update user with id=20 then return EntityNotFoundException")
    public void whenUpdateUser_thenReturnEntityNotFoundException() throws Exception {

        // В базе данных отсутствует пользователь с id = 20
        // Создаем User.In, которого будем использовать для обновления 20 пользователя
        User.In in = User.In.builder()
                .name("testName12")
                .email("test12@gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When update user with email=test6@gmail.com then return Exception")
    public void whenUpdateUserWithDuplicateEmail_thenReturnException() throws Exception {

        // В базе данных имеется пользователь с id = 10
        // Создаем User.In, которого будем использовать для обновления 10 пользователя.
        // Присваиваем в поле email значение, которое уже есть в базе данных.
        User.In in = User.In.builder()
                .name("testName12")
                .email("test6@gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When update user with invalid email then return Exception")
    public void whenUpdateUserWithInvalidEmail_thenReturnException() throws Exception {

        // В базе данных имеется пользователь с id = 10
        // Создаем User.In, которого будем использовать для обновления 10 пользователя.
        // Присваиваем в поле email невалидное значение адреса электронной почты.
        User.In in = User.In.builder()
                .name("testName12")
                .email("test12gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When update user with invalid name then return Exception")
    public void whenUpdateUserWithInvalidName_thenReturnException() throws Exception {

        // В базе данных имеется пользователь с id = 10
        // Создаем User.In, которого будем использовать для обновления 10 пользователя.
        // Присваиваем в поле name невалидное значение, состоящее из 2 символов.
        User.In in = User.In.builder()
                .name("tt")
                .email("test12gmail.com")
                .age(30)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When update user with invalid age then return Exception")
    public void whenUpdateUserWithInvalidAge_thenReturnException() throws Exception {

        // В базе данных имеется пользователь с id = 10
        // Создаем User.In, которого будем использовать для обновления 10 пользователя.
        // Присваиваем в поле age невалидное значение: возраст 66 лет.
        User.In in = User.In.builder()
                .name("testName12")
                .email("test12gmail.com")
                .age(66)
                .build();

        // Сериализуем созданного пользователя
        String json = objectMapper.writeValueAsString(in);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When delete user with id=1 then success")
    public void whenDeleteUser_thenSuccess() throws Exception {

        // В базе данных имеется пользователь с id = 1

        doNothing().when(userProducer).sendEventDeletedUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }


    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @DisplayName("When delete user with id=20 then return EntityNotFoundException")
    public void whenDeleteUser_thenReturnEntityNotFoundException() throws Exception {

        // В базе данных отсутствует пользователь с id = 20

        doNothing().when(userProducer).sendEventDeletedUser(any(User.class));

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/users/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}