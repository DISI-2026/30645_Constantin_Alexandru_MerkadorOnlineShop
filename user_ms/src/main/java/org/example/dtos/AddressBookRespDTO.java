package org.example.dtos;


import java.util.UUID;


public class AddressBookRespDTO {

    private UUID id;

    private UUID userId;

    private String label;

    private String addressLine;

    private String city;

    private String country;

    private String postalCode;

    private Boolean isDefault;

    public AddressBookRespDTO() {}

    public AddressBookRespDTO(UUID id, UUID userId, String label, String addressLine, String city, String country, String postalCode, Boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.label = label;
        this.addressLine = addressLine;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }
}