package org.example.dtos;

import jakarta.validation.constraints.NotBlank;

public class AddressBookReqDTO {
    @NotBlank private String label;
    @NotBlank private String addressLine;
    @NotBlank private String city;
    @NotBlank private String country;
    private String postalCode;
    private Boolean isDefault;


    public AddressBookReqDTO() {}

    public AddressBookReqDTO(String label, String addressLine, String city, String country, String postalCode, Boolean isDefault) {
        this.label = label;
        this.addressLine = addressLine;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    public String getLabel() { return label; }
    public String getAddressLine() { return addressLine; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getPostalCode() { return postalCode; }
    public Boolean getIsDefault() { return isDefault; }

    public void setLabel(String label) { this.label = label; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}