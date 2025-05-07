package com.backend.project.controller;

import com.backend.project.dto.AdminClaimViewDto;
import com.backend.project.dto.ClaimDto;
import com.backend.project.dto.UserClaimViewDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.model.Claim;
import com.backend.project.model.ClaimStatus;
import com.backend.project.model.FoundItem;
import com.backend.project.model.UserEntity;
import com.backend.project.security.JWTGenerator;
import com.backend.project.service.ClaimService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final JWTGenerator jwtGenerator;

    public ClaimController(ClaimService claimService, JWTGenerator jwtGenerator) {
        this.claimService = claimService;
        this.jwtGenerator = jwtGenerator;
    }

    @GetMapping
    public ResponseEntity<?> getAllClaims(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        List<String> roles = jwtGenerator.getRolesFromJWT(token);

        if (!roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can view all claims.");
        }

        List<Claim> claims = claimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }


    @GetMapping("/{claimId}")
    public ResponseEntity<?> getClaimById(@PathVariable String claimId, HttpServletRequest request) {
        Claim claim = claimService.getClaimById(claimId);
        if (claim == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Claim not found.");
        }
        return ResponseEntity.ok(claim);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyClaims(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String username = jwtGenerator.getUsernameFromJWT(token);

        List<UserClaimViewDto> userClaims = claimService.getClaimsByUser(username);
        return ResponseEntity.ok(userClaims);
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitClaim(
            @RequestParam("foundItemId") String foundItemId,
            @RequestParam("description") String description,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            HttpServletRequest request
    ) throws FailedUploadingPhoto {
        String token = request.getHeader("Authorization").substring(7);
        String username = jwtGenerator.getUsernameFromJWT(token);

        Claim savedClaim = claimService.submitClaim(foundItemId, description, attachment, username);
        return new ResponseEntity<>(savedClaim, HttpStatus.CREATED);
    }



    @PutMapping("/{claimId}/status")
    public ResponseEntity<?> updateClaimStatus(
            @PathVariable String claimId,
            @RequestParam ClaimStatus newStatus,
            HttpServletRequest request
    ) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header.");
        }

        List<String> roles = jwtGenerator.getRolesFromJWT(token);
        if (!roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can change claim status.");
        }

        String adminUsername = jwtGenerator.getUsernameFromJWT(token);
        Claim updatedClaim = claimService.updateClaimStatus(claimId, newStatus);
        return ResponseEntity.ok(updatedClaim);
    }



    @GetMapping("/admin-view")
    public ResponseEntity<?> getAllClaimsDetailed(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header.");
        }

        List<String> roles = jwtGenerator.getRolesFromJWT(token);
        if (!roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can view detailed claims.");
        }

        List<Claim> claims = claimService.getAllClaims();
        List<FoundItem> foundItems = claimService.getAllFoundItems();
        List<UserEntity> users = claimService.getAllUsers();

        List<AdminClaimViewDto> detailedClaims = claimService.getAllClaimsDetailed(claims, foundItems, users);
        return ResponseEntity.ok(detailedClaims);
    }

}
