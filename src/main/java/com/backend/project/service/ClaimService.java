package com.backend.project.service;

import com.backend.project.dto.AdminClaimViewDto;
import com.backend.project.dto.ClaimDto;
import com.backend.project.dto.UserClaimViewDto;
import com.backend.project.exceptions.FailedUploadingPhoto;
import com.backend.project.model.*;
import com.backend.project.repository.ClaimRepository;
import com.backend.project.repository.FoundItemRepository;
import com.backend.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemPhotoService itemPhotoService;
    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;

    @Autowired
    public ClaimService(ClaimRepository claimRepository,
                        ItemPhotoService itemPhotoService,
                        FoundItemRepository foundItemRepository,
                        UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.itemPhotoService = itemPhotoService;
        this.foundItemRepository = foundItemRepository;
        this.userRepository = userRepository;
    }


    public Claim submitClaim(String foundItemId, String description, MultipartFile attachment, String userId) throws FailedUploadingPhoto {
        String attachmentId = null;
        if (attachment != null && !attachment.isEmpty()) {
            attachmentId = String.valueOf(itemPhotoService.addPhoto(attachment).getId());
        }

        Claim claim = new Claim(
                UUID.fromString(foundItemId),
                userId,
                description,
                attachmentId
        );

        return claimRepository.save(claim);
    }


    public Claim updateClaimStatus(String claimId, ClaimStatus newStatus) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        ClaimStatus oldStatus = claim.getStatus();

        if (!oldStatus.equals(newStatus)) {
            StatusChange statusChange = new StatusChange(oldStatus, newStatus);
            claim.getStatusHistory().add(statusChange);
        }
        claim.setStatus(newStatus);
        claim.setStatus(newStatus);
        return claimRepository.save(claim);
    }

    public Claim getClaimById(String claimId) {
        return claimRepository.findById(claimId).get();
    }

    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    public List<UserClaimViewDto> getClaimsByUser(String userId) {
        return claimRepository.findAll()
                .stream()
                .filter(claim -> claim.getUserId().equals(userId))
                .map(claim -> new UserClaimViewDto(
                        claim.getId(),
                        claim.getFoundItemId(),
                        claim.getDescription(),
                        claim.getStatus(),
                        claim.getSubmittedAt()
                ))
                .collect(Collectors.toList());
    }


    public List<AdminClaimViewDto> getAllClaimsDetailed(List<Claim> claims, List<FoundItem> foundItems, List<UserEntity> users) {
        return claims.stream().map(claim -> {
            FoundItem foundItem = foundItems.stream()
                    .filter(item -> item.getId().equals(claim.getFoundItemId()))
                    .findFirst()
                    .orElse(null);

            UserEntity user = users.stream()
                    .filter(u -> u.getId().toString().equals(claim.getUserId()))
                    .findFirst()
                    .orElse(null);

            return new AdminClaimViewDto(
                    claim.getId(),
                    claim.getDescription(),
                    claim.getStatus(),
                    claim.getSubmittedAt().toString(),
                    claim.getFoundItemId(),
                    foundItem != null ? foundItem.getName() : null,
                    foundItem != null ? foundItem.getDescription() : null,
                    user != null ? user.getId().toString() : null,
                    user != null ? user.getUsername() : null,
                    claim.getStatusHistory()
            );
        }).collect(Collectors.toList());
    }


    public List<FoundItem> getAllFoundItems() {
        return foundItemRepository.findAll();
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

}
