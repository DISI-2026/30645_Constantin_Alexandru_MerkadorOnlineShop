package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.*;
import org.example.services.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileRespDTO>> getAllUsers() {
        return ResponseEntity.ok().body(userProfileService.findUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileRespDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(userProfileService.findUserById(id));
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("principal == #id.toString()")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @Valid @RequestBody UserProfileReqDTO dto) {
        userProfileService.update(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/avatar")
    @PreAuthorize("principal == #id.toString()")
    public ResponseEntity<Map<String, String>> uploadAvatar(@PathVariable UUID id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String avatarUrl = userProfileService.uploadAvatar(id, file);
        // return as a valid JSON
        return ResponseEntity.ok(Collections.singletonMap("avatarUrl", avatarUrl));
    }

    // ==========================================
    // SELLER ROUTES
    // ==========================================

    @GetMapping("/{id}/seller-profile")
    @PreAuthorize("hasRole('SELLER') and principal == #id.toString() or hasRole('ADMIN')")
    public ResponseEntity<SellerProfileRespDTO> getSellerProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userProfileService.getSellerProfile(id));
    }

    @PostMapping("/{id}/seller-profile")
    @PreAuthorize("hasRole('SELLER') and principal == #id.toString()")
    public ResponseEntity<Void> createSellerProfile(@PathVariable UUID id, @Valid @RequestBody SellerProfileReqDTO dto) {
        userProfileService.createOrUpdateSellerProfile(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sellers/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SellerProfileRespDTO>> getPendingSellers() {
        return ResponseEntity.ok(userProfileService.getUnverifiedSellers());
    }

    @PutMapping("/{id}/seller-profile/update")
    @PreAuthorize("hasRole('SELLER') and principal == #id.toString()")
    public ResponseEntity<Void> updateSellerProfile(@PathVariable UUID id, @Valid @RequestBody SellerProfileReqDTO dto) {
        userProfileService.createOrUpdateSellerProfile(id, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/seller-profile/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> verifySeller(@PathVariable UUID id, @Valid @RequestBody VerifySellerReqDTO body) {
        userProfileService.verifySellerProfile(id, body.getAuthorizedCategories());
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // ADDRESS BOOK ROUTES
    // ==========================================

    @GetMapping("/{id}/addresses")
    @PreAuthorize("principal == #id.toString() or hasRole('ADMIN')")
    public ResponseEntity<List<AddressBookRespDTO>> getUserAddresses(@PathVariable UUID id) {
        return ResponseEntity.ok(userProfileService.getUserAddresses(id));
    }

    @GetMapping("/{id}/default_address")
    @PreAuthorize("principal == #id.toString()")
    public ResponseEntity<AddressBookRespDTO> getDefaultUserAddress(@PathVariable UUID id) {
        return ResponseEntity.ok(userProfileService.getDefaultUserAddress(id));
    }

    @PostMapping("/{id}/addresses")
    @PreAuthorize("principal == #id.toString()")
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
    @PreAuthorize("principal == #id.toString()")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable UUID id, @PathVariable UUID addressId) {
        userProfileService.setDefaultAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    @PreAuthorize("principal == #id.toString()")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id, @PathVariable UUID addressId) {
        userProfileService.deleteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }
}