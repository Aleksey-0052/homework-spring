package com.aston.user_microservice.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonConfig {

    // Jackson по умолчанию не поддерживает сериализацию LocalDateTime.
    // В Jackson есть модуль JavaTimeModule, который мы можем использовать для сериализации LocalDateTime в правильном
    // формате.
    // Во-первых, нужно добавить зависимость jackson-datatype-jsr310 в наш pom.xml.
    // Во-вторых, нужно зарегистрировать этот модуль в ObjectMapper перед сериализацией объекта.
    // В-третьих, нужно отключить функцию SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, чтобы получать дату в том же
    // формате, что и входные данные, а не в виде временной метки.
    // Эта функция полностью реализована в тестовом классе.

    // В этом пакете реализуется кастомный модуль, который форматирует дату и время в заданном формате.
    // Этот модуль реализуют классы: JacksonConfig, LocalDateTimeSerializer и LocalDateTimeDeserializer.

    @Bean
    public JavaTimeModule javaTimeModule() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        return module;
    }

}
