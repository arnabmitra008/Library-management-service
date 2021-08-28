package com.library.ops.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class BookSearchException extends Exception {

    @Getter
    private HttpStatus httpStatus;

    public BookSearchException() {
        super();
    }

    public BookSearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookSearchException(String message) {
        super(message);
    }

    public BookSearchException(Throwable cause) {
        super(cause);
    }

    public BookSearchException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus=httpStatus;
    }
}
