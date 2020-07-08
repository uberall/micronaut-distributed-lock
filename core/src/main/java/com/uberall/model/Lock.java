package com.uberall.model;

import java.time.LocalDateTime;

public class Lock {

    String name;
    LocalDateTime until;

    public Lock() {
    }

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
