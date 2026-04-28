package org.example.services;

import org.example.dtos.UserProfileReqDTO;
import org.example.dtos.UserProfileRespDTO;
import org.example.dtos.builders.UserProfileBuilder;
import org.example.entities.UserProfile;
import org.example.handlers.exceptions.model.ResourceNotFoundException;
import org.example.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public List<UserProfileRespDTO> findUsers(){
        return userProfileRepository.findAll().stream()
                .map(UserProfileBuilder::toUserRespDTO)
                .collect(Collectors.toList());
    }

    public UserProfileRespDTO findUserById(UUID id){
        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id.toString()));
        return UserProfileBuilder.toUserRespDTO(user);
    }

    public void update(UUID id, UserProfileReqDTO dto) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id.toString()));


        // not need to check first two for null, because they are not allowed to be null
        userProfile.setFirstName(dto.getFirstName());
        userProfile.setLastName(dto.getLastName());
        // we keep the old data if any, if the current edits are null
        if (dto.getPhone() != null) userProfile.setPhone(dto.getPhone());
        if (dto.getAvatarUrl() != null) userProfile.setAvatarUrl(dto.getAvatarUrl());

        // updatedDate will automatically be set by Spring Data
        userProfileRepository.save(userProfile);
        LOGGER.info("User profile updated for id {}", id);
    }
}