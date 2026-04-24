package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.UserRespDTO;
import org.example.services.UserService;
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
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserRespDTO>> getAllUsers() {
        return ResponseEntity.ok().body(userService.findUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRespDTO> getUserById(@PathVariable UUID id) {
       return  ResponseEntity.ok().body(userService.findUserById(id));
    }

    @GetMapping("/{fullName}")
    public ResponseEntity<List<UserRespDTO>> getUserByFullName(@PathVariable String fullName) {
        return ResponseEntity.ok().body(userService.findUserByName(fullName));
    }

    @PostMapping("/add")
    public ResponseEntity<Void> createUser(@Valid @RequestBody String rawJson) {
        UUID id = userService.create(rawJson);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Void> updateUser(@PathVariable UUID id, @Valid @RequestBody String rawJson) {
        userService.update(id, rawJson);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
