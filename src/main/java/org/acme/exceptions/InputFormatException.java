package org.acme.exceptions;

public class InputFormatException extends RuntimeException{
    public InputFormatException(String message) {
        super(message);
    }
}
