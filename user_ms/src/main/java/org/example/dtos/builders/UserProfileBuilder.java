package org.example.dtos.builders;

import org.example.dtos.PublicUserProfileRespDTO;
import org.example.dtos.UserProfileRespDTO;
import org.example.entities.UserProfile;

import java.util.Collections;
import java.util.HashSet;

public class UserProfileBuilder {

    private UserProfileBuilder() {
    }

    public static UserProfileRespDTO toUserRespDTO(UserProfile userProfile) {
        // Build DTO using plain Java collections to avoid leaking Hibernate-managed
        // collection implementations into the JSON serializer.
        return new UserProfileRespDTO(
                userProfile.getUserId(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getAvatarUrl(),
                userProfile.getPhone(),
                userProfile.getCreatedDate(),
                userProfile.getUpdatedDate(),
                userProfile.getPreferredCategories() != null
                        ? new HashSet<>(userProfile.getPreferredCategories())
                        : Collections.emptySet()
        );
    }

    public static PublicUserProfileRespDTO toPublicUserRespDTO(UserProfile userProfile) {

        return new PublicUserProfileRespDTO(
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getAvatarUrl()
        );
    }
}
