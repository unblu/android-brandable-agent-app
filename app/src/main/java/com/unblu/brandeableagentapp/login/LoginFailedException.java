package com.unblu.brandeableagentapp.login;

/**
 * Exception thrown if login fails
 */
public class LoginFailedException extends Exception {

    public LoginFailedException() {
    }

    public LoginFailedException(String message) {
        super(message);
    }

    public LoginFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginFailedException(Throwable cause) {
        super(cause);
    }
}