package com.aston.user_microservice.service;

import com.aston.user_microservice.exception.EntityNotFoundException;
import com.aston.user_microservice.mapper.UserMapper;
import com.aston.user_microservice.model.User;
import com.aston.user_microservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyList;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private User user3;
    private User.Out out1;
    private User.Out out2;
    private User.Out out3;

    @BeforeEach
    public void setUp() {

        user1 = new User();
        user1.setId(1L);
        user1.setName("testName1");
        user1.setEmail("test1@gmail.com");
        user1.setAge(30);
        user1.setCreated_at(LocalDateTime.of(2025, 12, 12, 16, 45, 0));

        user2 = new User();
        user2.setId(2L);
        user2.setName("testName2");
        user2.setEmail("test2@gmail.com");
        user2.setAge(36);
        user2.setCreated_at(LocalDateTime.of(2025, 12, 13, 17, 45, 0));

        user3 = new User();
        user3.setId(3L);
        user3.setName("testName3");
        user3.setEmail("test3@gmail.com");
        user3.setAge(33);
        user3.setCreated_at(LocalDateTime.of(2025, 12, 14, 18, 45, 0));

        out1 = User.Out.builder()
                .id(1L)
                .name("testName1")
                .email("test1@gmail.com")
                .age(30)
                .created_at(LocalDateTime.of(2025, 12, 12, 16, 45, 0))
                .build();

        out2 = User.Out.builder()
                .id(2L)
                .name("testName2")
                .email("test2@gmail.com")
                .age(36)
                .created_at(LocalDateTime.of(2025, 12, 13, 17, 45, 0))
                .build();

        out3 = User.Out.builder()
                .id(3L)
                .name("testName3")
                .email("test3@gmail.com")
                .age(33)
                .created_at(LocalDateTime.of(2025, 12, 14, 18, 45, 0))
                .build();
    }


    @Test
    @DisplayName("When get all users then get user id=2, get user id=3")
    void whenGetAllUsers_thenSuccess() {

        // Добавляем в списки 2 последних пользователей
        List<User> users = List.of(user2, user3);
        List<User.Out> expected = List.of(out2, out3);

        doReturn(users).when(userRepository).getAllOffsetLimit(anyInt(), anyInt());
        doReturn(expected).when(mapper).toDTO(anyList());

        List<User.Out> actual = userService.getAll(1, 2);
        // Пропускаем первого пользователя и выводим двух следующих

        assertNotNull(actual, "users is empty");
        assertEquals(expected, actual);
        assertEquals(expected.getFirst().getId(), actual.getFirst().getId());
        assertEquals(expected.get(1).getId(), actual.get(1).getId());

        verify(userRepository, times(1)).getAllOffsetLimit(1, 2);
    }


    @Test
    @DisplayName("When get all count then return 3 users")
    void whenGetAllCount_thenSuccess() {

        // Добавляем в списки 3 пользователей
        List<User> expected = List.of(user1, user2, user3);

        doReturn(expected.size()).when(userRepository).getTotalCountOfUsers();

        int actual = userService.getAllCount();
        // Выводим количество пользователей, которое равно 3

        assertEquals(expected.size(), actual);
        verify(userRepository, times(1)).getTotalCountOfUsers();
    }


    @Test
    @DisplayName("When find user by id then get user id=1 email=test1@gmail.com")
    void whenFindUser_thenSuccess() {

        // Желаем получить пользователя с идентификатором 1
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(mapper.toDTO(any(User.class))).thenReturn(out1);

        User.Out actual = userService.find(1L);

        Assertions.assertNotNull(actual, "User is null");
        Assertions.assertEquals(out1.getId(), actual.getId());
        Assertions.assertEquals(out1.getName(), actual.getName());
        Assertions.assertEquals(out1.getEmail(), actual.getEmail());
        Assertions.assertEquals(out1.getAge(), actual.getAge());
        Assertions.assertEquals(out1.getCreated_at(), actual.getCreated_at());

        verify(userRepository, times(1)).findById(1L);
        verify(mapper, times(1)).toDTO(any(User.class));
    }


    @Test
    @DisplayName("When find user by id=20 then return EntityNotFoundException")
    void whenFindUser_thenReturnNotFoundException() {

        // Желаем получить пользователя с идентификатором 20, который отсутствует в базе данных
        long id = 20L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // Исключение возвращает тестируемый, а не зависимый класс. Поэтому не мокируем поведение на выброс исключения.

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> userService.find(id));

        Assertions.assertEquals("User with id = " + id + " not found", exception.getMessage());
        verify(userRepository, times(1)).findById(20L);
    }


    @Test
    @DisplayName("When create user with id=4 then success")
    void whenCreateUser_thenSuccess() throws ExecutionException, InterruptedException {

        // Создаем User.In, который будет передаваться в контроллер для создания 4 пользователя
        User.In in4 = User.In.builder()
                .name("testName4")
                .email("test4@gmail.com")
                .age(28)
                .build();

        User user4 = new User();
        user4.setId(4L);
        user4.setName("testName4");
        user4.setEmail("test4@gmail.com");
        user4.setAge(28);
        user4.setCreated_at(LocalDateTime.now());

        // Создаем ожидаемого четвертого пользователя User.Out, который будет выходить из контроллера
        User.Out out4 = User.Out.builder()
                .id(4L)
                .name("testName4")
                .email("test4@gmail.com")
                .age(28)
                .created_at(LocalDateTime.now())
                .build();

        UserProducerImpl userProducer = mock(UserProducerImpl.class);
        userService = new UserServiceImpl(userRepository, mapper, userProducer);

        doNothing().when(mapper).updateUserFromUserIn(any(User.In.class), any(User.class));
        doReturn(user4).when(userRepository).save(any(User.class));
        doNothing().when(userProducer).sendEventCreatedUser(user4);
        doReturn(out4).when(mapper).toDTO(any(User.class));

        // Вызываем метод из тестируемого класса
        User.Out actual = userService.create(in4);

        assertNotNull(actual, "user is null");
        assertEquals(out4.getId(), actual.getId());
        assertEquals(out4.getName(), actual.getName());
        assertEquals(out4.getEmail(), actual.getEmail());
        assertEquals(out4.getAge(), actual.getAge());
        assertEquals(out4.getCreated_at(), actual.getCreated_at());

        verify(mapper, times(1)).updateUserFromUserIn(any(User.In.class), any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(userProducer, times(1)).sendEventCreatedUser(user4);
        verify(mapper, times(1)).toDTO(any(User.class));
    }


    @Test
    @DisplayName("When update user with id=2 then success")
    void whenUpdateUser_thenSuccess() {

        // Создаем User.In, который будет передаваться в контроллер для обновления второго пользователя
        User.In in5 = User.In.builder()
                .name("testName5")
                .email("test5@gmail.com")
                .age(31)
                .build();

        // Второй обновленный пользователь
        User updatedUser = new User();
        updatedUser.setId(2L);
        updatedUser.setName("testName5");
        updatedUser.setEmail("test5@gmail.com");
        updatedUser.setAge(31);
        updatedUser.setCreated_at(LocalDateTime.of(2025, 12, 13, 17, 45, 0));

        // Создаем ожидаемого второго пользователя User.Out, который будет выходить из контроллера
        User.Out updatedOut = User.Out.builder()
                .id(2L)
                .name("testName5")
                .email("test5@gmail.com")
                .age(31)
                .created_at(LocalDateTime.of(2025, 12, 13, 17, 45, 0))
                .build();

        doReturn(Optional.of(user2)).when(userRepository).findById(anyLong());
        doNothing().when(mapper).updateUserFromUserIn(any(User.In.class), any(User.class));
        doReturn(updatedUser).when(userRepository).save(any(User.class));
        doReturn(updatedOut).when(mapper).toDTO(any(User.class));

        // Вызываем метод из тестируемого класса
        User.Out actual = userService.update(2L, in5);

        Assertions.assertNotNull(actual, "user is null");
        Assertions.assertEquals(updatedOut.getId(), actual.getId());
        Assertions.assertEquals(updatedOut.getName(), actual.getName());
        Assertions.assertEquals(updatedOut.getEmail(), actual.getEmail());
        Assertions.assertEquals(updatedOut.getAge(), actual.getAge());
        Assertions.assertEquals(updatedOut.getCreated_at(), actual.getCreated_at());

        verify(userRepository, times(1)).findById(anyLong());
        verify(mapper, times(1)).updateUserFromUserIn(any(User.In.class), any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(mapper, times(1)).toDTO(any(User.class));
    }


    @Test
    @DisplayName("When update user with id=20 then return EntityNotFoundException")
    void whenUpdateUser_thenReturnEntityNotFoundException() {

        // Если в базе данных нет пользователя с идентификатором 20, то будет выброшено исключение

        // Создаем User.In, который будет передаваться в контроллер для обновления отсутствующего пользователя
        User.In in5 = User.In.builder()
                .name("testName5")
                .email("test5@gmail.com")
                .age(31)
                .build();

        long id = 20;

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // Исключение возвращает тестируемый, а не зависимый класс. Поэтому не мокируем поведение на выброс исключения.

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> userService.update(id, in5));

        Assertions.assertEquals("User with id = " + id + " not found", exception.getMessage());

        verify(userRepository, times(1)).findById(20L);
    }


    @Test
    @DisplayName("When delete user with id=3 then success")
    void whenDeleteUser_thenSuccess() {

        // Желаем удалить пользователя с идентификатором 3
        long id = 3L;

        UserProducerImpl userProducer = mock(UserProducerImpl.class);
        userService = new UserServiceImpl(userRepository, mapper, userProducer);

        doReturn(Optional.of(user3)).when(userRepository).findById(anyLong());
        doNothing().when(userRepository).deleteById(user3.getId());
        doNothing().when(userProducer).sendEventDeletedUser(user3);

        userService.delete(id);

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).deleteById(id);
        verify(userProducer, times(1)).sendEventDeletedUser(user3);
    }


    @Test
    @DisplayName("When delete user with id=20 then return EntityNotFoundException")
    void whenDeleteUser_thenReturnEntityNotFoundException() {

        // Если в базе данных нет пользователя с идентификатором 20, то будет выброшено исключение

        long id = 20;

        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // Исключение возвращает тестируемый, а не зависимый класс. Поэтому не мокируем поведение на выброс исключения.

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> userService.delete(id));

        Assertions.assertEquals("User with id = " + id + " not found", exception.getMessage());

        verify(userRepository, times(1)).findById(20L);

    }

}