package org.example.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dtos.UserReqDTO;
import org.example.dtos.UserRespDTO;
import org.example.dtos.builders.UserBuilder;
import org.example.entities.User;
import org.example.handlers.exceptions.model.ResourceNotFoundException;
import org.example.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserSyncProducer userSyncProducer;

    @Autowired
    public UserService(UserRepository userRepository, UserSyncProducer userSyncProducer) {
        this.userRepository = userRepository;
        this.userSyncProducer = userSyncProducer;
    }

    public List<UserRespDTO> findUsers(){
        List<User> userList = userRepository.findAll();
        return userList.stream()
                .map(UserBuilder::toUserRespDTO)
                .collect(Collectors.toList());
    }

    public UserRespDTO findUserById(UUID id){
        Optional<User> user = userRepository.findById(id);
        if(!user.isPresent()){
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }else{
            return UserBuilder.toUserRespDTO(user.get());
        }
    }

    public List<UserRespDTO> findUserByName(String fullName){
        List<User> userList = userRepository.findByFullName(fullName);
        return userList.stream()
                .map(UserBuilder::toUserRespDTO)
                .collect(Collectors.toList());
    }

    public UUID create(String fullJsonPayload) {
        ObjectMapper mapper = new ObjectMapper();

        // Deserialize with ignored fields
        UserReqDTO userReqDTO;
        try {
            userReqDTO = mapper.readValue(fullJsonPayload, UserReqDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }

        User user = UserBuilder.toEntity(userReqDTO);
        user = userRepository.save(user);
        LOGGER.debug("User with id {} was inserted in db", user.getId());

        try {
            userSyncProducer.sendUserCreated(user.getId(), fullJsonPayload);
        } catch (Exception e) {
            // If RabbitMQ is down, we rollback the DB transaction
            LOGGER.error("Failed to send sync message for created user. Rolling back.", e);
            userRepository.deleteById(user.getId());
            throw e;
        }

        return user.getId();
    }

    public void update(UUID id, String fullJsonPayload) {
        Optional<User> maybeUser = userRepository.findById(id);
        if(!maybeUser.isPresent()){
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException("User with id " + id);
        }else{
            User user = maybeUser.get();
            ObjectMapper mapper = new ObjectMapper();

            try {
                // Update Local DB
                UserReqDTO updatedUserInfo = mapper.readValue(fullJsonPayload, UserReqDTO.class);
                if (updatedUserInfo.getFullName() != null) user.setFullName(updatedUserInfo.getFullName());
                if (updatedUserInfo.getAddress() != null) user.setAddress(updatedUserInfo.getAddress());
                if (updatedUserInfo.getEmail() != null) user.setEmail(updatedUserInfo.getEmail());
                user.setUpdatedDate();
                userRepository.save(user);

                // Publish update event
                try {
                    userSyncProducer.sendUserUpdated(id, fullJsonPayload);
                } catch (Exception e) {
                    // If RabbitMQ is down, we rollback the DB transaction
                    userRepository.save(maybeUser.get());
                    throw e;
                }

            } catch (Exception e) {
                LOGGER.error("Error updating user or sending message", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void delete(UUID id) {
        if(userRepository.findById(id).isPresent()){
            try{
                userSyncProducer.sendUserDeleted(id);
                userRepository.deleteById(id);
                LOGGER.debug("User with id {} was deleted from db", id);
            } catch (Exception e) {
                LOGGER.error("Failed to send sync message for deleted user. Rolling back.", e);
                throw e;
            }
        }else {
            LOGGER.error("User with id {} was not found in db", id);
            throw new ResourceNotFoundException("User with id " + id);
        }
    }
}
