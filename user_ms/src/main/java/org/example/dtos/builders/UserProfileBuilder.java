package org.example.dtos.builders;

import org.example.dtos.UserProfileRespDTO;
import org.example.entities.UserProfile;

public class UserProfileBuilder {

    private UserProfileBuilder() {
    }

    public static UserProfileRespDTO toUserRespDTO(UserProfile userProfile) {
        return new UserProfileRespDTO(
                userProfile.getUserId(),
                userProfile.getFirstName(),
                userProfile.getLastName(),
                userProfile.getAvatarUrl(),
                userProfile.getPhone(),
                userProfile.getCreatedDate(),
                userProfile.getUpdatedDate()
        );
    }
}
