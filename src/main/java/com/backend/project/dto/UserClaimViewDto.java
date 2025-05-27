package com.backend.project.dto;

import com.backend.project.model.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserClaimViewDto {
    private String claimId;
    private UUID foundItemId;
    private String description;
    private ClaimStatus status;
    private LocalDateTime submittedAt;
}
