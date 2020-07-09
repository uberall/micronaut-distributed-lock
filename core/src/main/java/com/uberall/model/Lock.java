package com.uberall.model;

import java.time.LocalDateTime;

public class Lock {

    String name;
    LocalDateTime until;

    public Lock() {
    }

    /**
     * initiates a new Lock with the given name and until date set.
     * @param name of the lock
     * @param until when this lock is valid
     */
    public Lock(String name, LocalDateTime until) {
        this();
        this.name = name;
        this.until = until;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getUntil() {
        return until;
    }

    public void setUntil(LocalDateTime until) {
        this.until = until;
    }
}
