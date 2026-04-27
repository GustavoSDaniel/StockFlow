package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;


@Table("supplier_contact")
public class SupplierContact extends BaseEntity{

    public SupplierContact(){}

    public SupplierContact( String contactName, String email, String phoneNumber) {

        this.contactName = contactName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Column("supplier_id")
    private UUID supplierId;

    @Column("contact_name")
    private String contactName;

    private String email;

    @Column("phone_number")
    private String phoneNumber;

    private boolean active = true;

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
