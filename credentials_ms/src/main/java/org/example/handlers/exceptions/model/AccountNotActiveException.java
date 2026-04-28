package org.example.handlers.exceptions.model;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class AccountNotActiveException extends CustomException {
    private static final String MESSAGE = "Account not active!";
    private static final HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

    public AccountNotActiveException(String resource) {
        super(MESSAGE,httpStatus, resource, new ArrayList<>());
    }
}
