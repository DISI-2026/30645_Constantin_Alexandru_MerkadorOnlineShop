package org.example.services;

import org.example.dtos.*;
import org.example.dtos.builders.UserProfileBuilder;
import org.example.entities.AddressBook;
import org.example.entities.SellerProfile;
import org.example.entities.UserProfile;
import org.example.handlers.exceptions.model.ResourceNotFoundException;
import org.example.ports.AvatarStoragePort;
import org.example.repositories.AddressBookRepository;
import org.example.repositories.SellerProfileRepository;
import org.example.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserProfileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository userProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final AddressBookRepository addressBookRepository;
    private final AvatarStoragePort avatarStoragePort;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              SellerProfileRepository sellerProfileRepository,
                              AddressBookRepository addressBookRepository,
                              AvatarStoragePort avatarStoragePort) {
        this.userProfileRepository = userProfileRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.addressBookRepository = addressBookRepository;
        this.avatarStoragePort = avatarStoragePort;
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

    @Transactional
    public String uploadAvatar(UUID userId, org.springframework.web.multipart.MultipartFile file) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String avatarUrl = avatarStoragePort.uploadAvatar(userId, file);
        user.setAvatarUrl(avatarUrl);
        userProfileRepository.save(user);

        return avatarUrl;
    }

    // ==========================================
    // SELLER PROFILE LOGIC
    // ==========================================

    public SellerProfileRespDTO getSellerProfile(UUID userId) {
        SellerProfile seller = sellerProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found for user " + userId));

        SellerProfileRespDTO dto = new SellerProfileRespDTO();
        dto.setUserId(seller.getUserId());
        dto.setShopName(seller.getShopName());
        dto.setShopSlug(seller.getShopSlug());
        dto.setDescription(seller.getDescription());
        dto.setAvgRating(seller.getAvgRating());
        dto.setTotalSales(seller.getTotalSales());
        dto.setVerified(seller.getVerified());
        return dto;
    }

    @Transactional
    public void createOrUpdateSellerProfile(UUID userId, SellerProfileReqDTO dto) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId.toString()));

        // Verificăm dacă are deja profil de vânzător (pentru update)
        SellerProfile sellerProfile = sellerProfileRepository.findById(userId).orElse(new SellerProfile());

        sellerProfile.setUserProfile(userProfile);
        sellerProfile.setShopName(dto.getShopName());
        sellerProfile.setDescription(dto.getDescription());

        // Dacă nu a trimis un slug, îl generăm din nume
        String slug = (dto.getShopSlug() != null && !dto.getShopSlug().isEmpty())
                ? dto.getShopSlug()
                : dto.getShopName().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        sellerProfile.setShopSlug(slug);

        sellerProfileRepository.save(sellerProfile);
        LOGGER.info("Seller profile created/updated for user with id = {}", userId);
    }

    public List<SellerProfileRespDTO> getUnverifiedSellers() {
        return sellerProfileRepository.findByVerifiedFalse().stream()
                .map(seller -> {
                    SellerProfileRespDTO dto = new SellerProfileRespDTO();
                    dto.setUserId(seller.getUserId());
                    dto.setShopName(seller.getShopName());
                    dto.setShopSlug(seller.getShopSlug());
                    dto.setDescription(seller.getDescription());
                    dto.setAvgRating(seller.getAvgRating());
                    dto.setTotalSales(seller.getTotalSales());
                    dto.setVerified(seller.getVerified());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void verifySellerProfile(UUID userId) {
        SellerProfile sellerProfile = sellerProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No seller profile was found for user with id = " + userId));

        sellerProfile.setVerified(true);
        sellerProfileRepository.save(sellerProfile);
        LOGGER.info("Seller profile has been verified: ", userId);
    }

    // ==========================================
    // ADDRESS BOOK LOGIC
    // ==========================================
    public List<AddressBookRespDTO> getUserAddresses(UUID userId) {
        return addressBookRepository.findAllByUserProfileUserId(userId).stream()
                .map(addr -> {
                    AddressBookRespDTO dto = new AddressBookRespDTO();
                    dto.setId(addr.getId());
                    dto.setUserId(addr.getId());
                    dto.setLabel(addr.getLabel());
                    dto.setAddressLine(addr.getAddressLine());
                    dto.setCity(addr.getCity());
                    dto.setCountry(addr.getCountry());
                    dto.setPostalCode(addr.getPostalCode());
                    dto.setIsDefault(addr.getIsDefault());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional
    public UUID addAddress(UUID userId, AddressBookReqDTO dto) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User cu id-ul " + userId + " nu a fost găsit."));

        // Dacă adresa nouă este setată ca Default, trebuie să debifăm celelalte adrese ale userului
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            resetDefaultAddresses(userId);
        }

        AddressBook address = new AddressBook();
        address.setUserProfile(userProfile);
        address.setLabel(dto.getLabel());
        address.setAddressLine(dto.getAddressLine());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setPostalCode(dto.getPostalCode());
        address.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);

        address = addressBookRepository.save(address);
        return address.getId();
    }

    @Transactional
    public void setDefaultAddress(UUID userId, UUID addressId) {
        // Debifăm toate adresele utilizatorului
        resetDefaultAddresses(userId);

        // Setăm noua adresă ca default
        AddressBook address = addressBookRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(addressId.toString()));

        if (!address.getUserProfile().getUserId().equals(userId)) {
            throw new IllegalArgumentException("The address does not belong to this user.");
        }

        address.setIsDefault(true);
        addressBookRepository.save(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        AddressBook address = addressBookRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException(addressId.toString()));

        if (!address.getUserProfile().getUserId().equals(userId)) {
            throw new IllegalArgumentException("The address does not belong to this user.");
        }

        addressBookRepository.delete(address);
    }

    // Helper method pentru a debifa adresele default
    private void resetDefaultAddresses(UUID userId) {
        List<AddressBook> currentAddresses = addressBookRepository.findAllByUserProfileUserId(userId);
        for (AddressBook addr : currentAddresses) {
            if (Boolean.TRUE.equals(addr.getIsDefault())) {
                addr.setIsDefault(false);
                addressBookRepository.save(addr);
            }
        }
    }
}