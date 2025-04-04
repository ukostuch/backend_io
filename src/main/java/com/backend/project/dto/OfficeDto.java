package com.backend.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record OfficeDto(
        @NotNull @Valid DistrictDto district,
        @NotBlank @Pattern(regexp = "^(?:\\+43|0)(?:\\s?\\d{1,4})(?:\\s?\\d{1,4})(?:\\s?\\d{4})$", message = "Invalid phone number format") String phoneNumber,
        @NotBlank @Size(max = 500, message = "Description cannot exceed 500 characters") String description,
        @NotBlank String address
) {}
