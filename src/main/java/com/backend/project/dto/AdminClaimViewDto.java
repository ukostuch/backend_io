package com.backend.project.dto;

import com.backend.project.model.ClaimStatus;
import com.backend.project.model.StatusChange;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminClaimViewDto {
    private String claimId;
    private String description;
    private ClaimStatus status;
    private String submittedAt;

    private UUID foundItemId;
    private String foundItemName;
    private String foundItemDescription;

    private String userId;
    private String username;
    private List<StatusChange> statusHistory = new ArrayList<>();

    public AdminClaimViewDto(String claimId, String description, ClaimStatus status, String submittedAt,
                             UUID foundItemId, String foundItemName, String foundItemDescription,
                             String userId, String username, List<StatusChange> statusHistory) {
        this.claimId = claimId;
        this.description = description;
        this.status = status;
        this.submittedAt = submittedAt;
        this.foundItemId = foundItemId;
        this.foundItemName = foundItemName;
        this.foundItemDescription = foundItemDescription;
        this.userId = userId;
        this.username = username;
        this.statusHistory = statusHistory;
    }

}
