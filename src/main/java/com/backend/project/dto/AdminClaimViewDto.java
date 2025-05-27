package com.backend.project.dto;

import com.backend.project.model.ClaimStatus;
import com.backend.project.model.StatusChange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
