package com.library.ops.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ManageBooksException extends Exception {

    @Getter
    private HttpStatus httpStatus;

    public ManageBooksException() {
        super();
    }

    public ManageBooksException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManageBooksException(String message) {
        super(message);
    }

    public ManageBooksException(Throwable cause) {
        super(cause);
    }

    public ManageBooksException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus=httpStatus;
    }
}
