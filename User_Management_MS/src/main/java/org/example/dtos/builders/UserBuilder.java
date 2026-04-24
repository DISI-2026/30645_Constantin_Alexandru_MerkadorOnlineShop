package org.example.dtos.builders;

import org.example.dtos.UserReqDTO;
import org.example.dtos.UserRespDTO;
import org.example.entities.User;

public class UserBuilder {

    private UserBuilder() {
    }

    public static UserRespDTO toUserRespDTO(User user) {
        return new UserRespDTO(user.getId(), user.getFullName(), user.getAddress(), user.getCreatedDate(), user.getUpdatedDate(), user.getEmail());
    }

    public static User toEntity(UserReqDTO userReqDTO) {
        return new User(userReqDTO.getFullName(),
                userReqDTO.getAddress(),
                userReqDTO.getEmail());
    }
}
