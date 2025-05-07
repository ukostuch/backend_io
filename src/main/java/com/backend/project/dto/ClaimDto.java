package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class ClaimDto {

    @NotNull
    private UUID foundItemId;
    @NotBlank
    private String description;
    private MultipartFile attachment;

    public UUID getFoundItemId() {
        return foundItemId;
    }

    public String getDescription() {
        return description;
    }

    public MultipartFile getAttachment() {
        return attachment;
    }
}
