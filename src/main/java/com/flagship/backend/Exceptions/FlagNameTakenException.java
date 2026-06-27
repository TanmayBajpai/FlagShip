package com.flagship.backend.Exceptions;

public class FlagNameTakenException extends RuntimeException {
    public FlagNameTakenException() {
        super("A flag with this name already exists");
    }
}
