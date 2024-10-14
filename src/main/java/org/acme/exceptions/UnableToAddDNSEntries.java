package org.acme.exceptions;

public class UnableToAddDNSEntries extends RuntimeException{
    public UnableToAddDNSEntries(String message) {
        super(message);
    }
}
