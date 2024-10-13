package org.acme.exceptions;

public class ManagedZoneCreationFailed extends RuntimeException{
    public ManagedZoneCreationFailed(String message) {
        super(message);
    }
}
