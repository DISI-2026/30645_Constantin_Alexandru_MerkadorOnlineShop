package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.*;
import org.example.services.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
public class UserProfileController {
    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ResponseEntity<List<UserProfileRespDTO>> getAllUsers() {
        return ResponseEntity.ok().body(userProfileService.findUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileRespDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(userProfileService.findUserById(id));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @Valid @RequestBody UserProfileReqDTO dto) {
        userProfileService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable UUID id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String avatarUrl = userProfileService.uploadAvatar(id, file);
        return ResponseEntity.ok(avatarUrl);
    }

    // ==========================================
    // SELLER ROUTES
    // ==========================================
    @GetMapping("/{id}/seller-profile")
    public ResponseEntity<SellerProfileRespDTO> getSellerProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userProfileService.getSellerProfile(id));
    }

    @PostMapping("/{id}/seller-profile")
    public ResponseEntity<Void> createSellerProfile(@PathVariable UUID id, @Valid @RequestBody SellerProfileReqDTO dto) {
        userProfileService.createOrUpdateSellerProfile(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sellers/pending")
    public ResponseEntity<List<SellerProfileRespDTO>> getPendingSellers() {
        return ResponseEntity.ok(userProfileService.getUnverifiedSellers());
    }

    @PatchMapping("/{id}/seller-profile/verify")
    public ResponseEntity<Void> verifySeller(@PathVariable UUID id) {
        userProfileService.verifySellerProfile(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ADDRESS BOOK ROUTES
    // ==========================================

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<AddressBookRespDTO>> getUserAddresses(@PathVariable UUID id) {
        return ResponseEntity.ok(userProfileService.getUserAddresses(id));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<Void> addAddress(@PathVariable UUID id, @Valid @RequestBody AddressBookReqDTO dto) {
        UUID addressId = userProfileService.addAddress(id, dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{addressId}")
                .buildAndExpand(addressId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}/addresses/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable UUID id, @PathVariable UUID addressId) {
        userProfileService.setDefaultAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id, @PathVariable UUID addressId) {
        userProfileService.deleteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }
}