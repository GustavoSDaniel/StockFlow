package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("addresses")
public class Address extends BaseEntity{

    public Address (){}

    public Address(UUID supplierId, String label, String street, String streetNumber, String complement, String neighborhood, String city, String zipCode, StateUF stateUF, String country,  boolean isMain) {

        this.supplierId = supplierId;
        this.label = label;
        this.street = street;
        this.streetNumber = streetNumber;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.zipCode = zipCode;
        this.stateUF = stateUF;
        this.country = country;
        this.isMain = isMain;
    }

    @Column("supplier_id")
    private UUID supplierId;

    private String label;

    private String street;

    @Column("street_number")
    private String streetNumber;

    private String complement;

    private String neighborhood;

    private String city;

    @Column("zip_code")
    private String zipCode;

    @Column("state")
    private StateUF stateUF;

    private String country;

    @Column("is_main")
    private boolean isMain;

    private boolean active = true;

    public UUID getSupplierId() {
        return supplierId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public StateUF getStateUF() {
        return stateUF;
    }

    public void setStateUF(StateUF stateUF) {
        this.stateUF = stateUF;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean isMain) {
        this.isMain = isMain;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
