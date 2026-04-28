package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.UserProfileReqDTO;
import org.example.dtos.UserProfileRespDTO;
import org.example.services.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}