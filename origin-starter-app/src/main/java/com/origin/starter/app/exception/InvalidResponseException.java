package com.origin.starter.app.exception;

public class InvalidResponseException extends RuntimeException{
    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
