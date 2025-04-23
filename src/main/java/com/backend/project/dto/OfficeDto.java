package com.backend.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record OfficeDto(
        @NotNull @Valid DistrictDto district,
        @NotBlank @Pattern(regexp = "^(?:\\+48|48|0)?\\s?\\d{3}[\\s-]?\\d{3}[\\s-]?\\d{3}$", message = "Invalid phone number format") String phoneNumber,
        @NotBlank @Size(max = 500, message = "Description cannot exceed 500 characters") String description,
        @NotBlank String address
) {}
