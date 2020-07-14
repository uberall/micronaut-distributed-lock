package com.uberall.exceptions;

public class DistributedLockConfigurationException extends Exception {

    public DistributedLockConfigurationException(String reason) {
        super("Invalid Configuration passed to annotation, reason: " + reason);
    }
}
