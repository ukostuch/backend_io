package com.backend.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record OfficeRetDto(
        @NotNull UUID id,
        @NotNull @Valid DistrictDto district,
        @NotBlank @Pattern(regexp = "^(?:\\+48|48|0)?\\s?\\d{3}[\\s-]?\\d{3}[\\s-]?\\d{3}$", message = "Invalid phone number format") String phoneNumber,
        @NotBlank String address,
        @NotBlank String photo,
        @NotBlank @Size(max = 300, message = "Description cannot exceed 300 characters") String description

) {}