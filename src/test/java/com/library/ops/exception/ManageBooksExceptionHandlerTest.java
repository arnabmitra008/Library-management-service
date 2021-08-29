package com.library.ops.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ManageBooksExceptionHandlerTest {

    @InjectMocks
    private ManageBooksExceptionHandler manageBooksExceptionHandler = new ManageBooksExceptionHandler();

    private ManageBooksException manageBooksException;

    @BeforeEach
    void setUp() {
        manageBooksException = new ManageBooksException("Sample message", HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldHandleAccountException() {
        ResponseEntity<ManageBooksExceptionEntity> resp = manageBooksExceptionHandler.handleBooksException(manageBooksException);
        assertEquals("Sample message", resp.getBody().getUserMessage());
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }
}