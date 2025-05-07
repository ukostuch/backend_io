package com.backend.project.dto;

import com.backend.project.model.ClaimStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserClaimViewDto {
    private String claimId;
    private UUID foundItemId;
    private String description;
    private ClaimStatus status;
    private LocalDateTime submittedAt;

    public UserClaimViewDto(String claimId, UUID foundItemId, String description, ClaimStatus status, LocalDateTime submittedAt) {
        this.claimId = claimId;
        this.foundItemId = foundItemId;
        this.description = description;
        this.status = status;
        this.submittedAt = submittedAt;
    }
}
