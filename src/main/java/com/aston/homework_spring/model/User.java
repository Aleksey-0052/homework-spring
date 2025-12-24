package com.aston.homework_spring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @Column(name = "age", nullable = false)
    private Integer age;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class In {

        // Входящее DTO

        @Size(min = 3, max = 25, message = "Имя должно состоять от 3 до 25 символов")
        @NotBlank(message = "Имя не может быть пустым или состоять только из пробелов")
        String name;

        @Email(message = "Email должен быть валидным адресом электронной почты")
        @NotBlank(message = "Email не может быть пустым или состоять только из пробелов")
        String email;

        @Min(value = 18, message = "Минимальный возраст составляет 18 лет")
        @Max(value = 65, message = "Максимальный возраст составляет 65 лет")
        Integer age;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Out {

        // Выходящее DTO
        Long id;
        String name;
        String email;
        LocalDateTime created_at;
        Integer age;

    }

}
