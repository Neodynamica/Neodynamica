package com.neodynamica.backendinterface;

/**
 * Exception triggered by Backend if a command is issued but the symbolicRegression is not currently
 * in a RunState which allows that action.
 */
public class InvalidRunStateException extends Exception {

    public InvalidRunStateException(String message) {
        super(message);
    }
}
