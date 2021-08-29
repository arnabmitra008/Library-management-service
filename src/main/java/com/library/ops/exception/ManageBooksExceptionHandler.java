package com.library.ops.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ManageBooksExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ManageBooksException.class)
    public ResponseEntity<ManageBooksExceptionEntity> handleAccountException(ManageBooksException ex) {
        ManageBooksExceptionEntity entity = ManageBooksExceptionEntity.builder()
                .userMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(entity, ex.getHttpStatus());
    }

}
