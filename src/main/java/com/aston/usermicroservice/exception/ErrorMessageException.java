package com.aston.usermicroservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@ToString
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ErrorMessageException extends RuntimeException {

    private LocalDateTime dateTime;
    private String message;

}
