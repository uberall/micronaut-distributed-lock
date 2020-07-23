package com.uberall.exceptions;

public class DistributedLockCreationException extends RuntimeException {

    Exception cause;

    public DistributedLockCreationException(String name, Exception cause) {
        super("Creation of lock " + name + " failed as lock was present already");
        this.cause = cause;
    }
}
