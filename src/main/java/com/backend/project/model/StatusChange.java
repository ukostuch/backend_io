package com.backend.project.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StatusChange {
    private ClaimStatus oldStatus;
    private ClaimStatus newStatus;
    private LocalDateTime changedAt;

    public StatusChange(ClaimStatus oldStatus, ClaimStatus newStatus) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = LocalDateTime.now();
    }
}
