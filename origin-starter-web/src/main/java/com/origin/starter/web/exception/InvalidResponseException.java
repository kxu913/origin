package com.origin.starter.web.exception;

public class InvalidResponseException extends RuntimeException{
    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
