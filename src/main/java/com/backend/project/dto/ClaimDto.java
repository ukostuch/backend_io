package com.backend.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ClaimDto {

    @NotNull
    private UUID foundItemId;

    @NotBlank
    private String description;

    private MultipartFile attachment;
}
