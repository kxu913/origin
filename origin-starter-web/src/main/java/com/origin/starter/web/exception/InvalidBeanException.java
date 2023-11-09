package com.origin.starter.web.exception;

public class InvalidBeanException extends RuntimeException{
    public InvalidBeanException(String message) {
        super(message);
    }

    public InvalidBeanException(String message, Throwable cause) {
        super(message, cause);
    }
}
