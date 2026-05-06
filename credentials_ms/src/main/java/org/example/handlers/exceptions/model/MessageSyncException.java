package org.example.handlers.exceptions.model;

import org.springframework.http.HttpStatus;
import java.util.ArrayList;

public class MessageSyncException extends CustomException {
    private static final HttpStatus httpStatus = HttpStatus.SERVICE_UNAVAILABLE;

    public MessageSyncException(String message) {
        // Apelăm constructorul din CustomException cu mesajul dorit
        super(message, httpStatus, "message_broker", new ArrayList<>());
    }
}