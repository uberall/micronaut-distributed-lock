package com.uberall;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class DistributedLock {

    @NotNull
    LocalDateTime until;

    @NotNull
    String name;

    @Id
    @GeneratedValue
    private Long id;

    public DistributedLock() {
    }

    public DistributedLock(@NotNull String name, @NotNull LocalDateTime until) {
        this();
        this.name = name;
        this.until = until;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getUntil() {
        return until;
    }

    public void setUntil(LocalDateTime until) {
        this.until = until;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
