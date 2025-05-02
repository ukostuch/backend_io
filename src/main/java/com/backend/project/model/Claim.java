package com.backend.project.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "claims")
public class Claim {

    @Id
    private String id;

    private UUID foundItemId;
    private String userId;
    private String description;
    private String attachmentId;
    private LocalDateTime submittedAt;
    private ClaimStatus status;
    private List<StatusChange> statusHistory = new ArrayList<>();

    public Claim() {
    }

    public Claim(UUID foundItemId, String userId, String description, String attachmentId) {
        this.foundItemId = foundItemId;
        this.userId = userId;
        this.description = description;
        this.attachmentId = attachmentId;
        this.submittedAt = LocalDateTime.now();
        this.status = ClaimStatus.UNDER_REVIEW;
    }

}
