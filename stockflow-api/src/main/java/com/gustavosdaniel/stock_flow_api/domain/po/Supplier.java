package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.*;

@Table("suppliers")
public class Supplier extends BaseEntity{

    public Supplier(){}

    public Supplier(String name, String cnpj, String tradeName, List<SupplierContact> contacts, String website, BigDecimal minOrderValue, String notes, List<Address> addresses) {
        this.name = name;
        this.cnpj = cnpj;
        this.tradeName = tradeName;
        this.contacts = contacts != null ? contacts : new ArrayList<>();
        this.website = website;
        this.minOrderValue = minOrderValue;
        this.notes = notes;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
    }

    private String name;

    private String cnpj;

    private String tradeName;

    @Transient
    private List<SupplierContact> contacts = new ArrayList<>();

    private String website;

    private BigDecimal minOrderValue;

    private String notes;

    @Transient
    private List<Address> addresses = new ArrayList<>();

    @Transient
    public Optional<Address> getMainAddress(){

        return addresses
                .stream()
                .filter(Address::isMain)
                .findFirst();
    }

    public void addAddress(Address address){

        address.setSupplierId(this.getId());
        this.addresses.add(address);
    }

    public void addContact(SupplierContact contact){

        contact.setSupplierId(this.getId());
        this.contacts.add(contact);
    }

    public void removeAddress(Address address){

        if (address == null) return;
        this.addresses.remove(address);
        address.setSupplierId(null);


    }

    public void removeContact(SupplierContact contact){

        this.contacts.remove(contact);

        if (contact != null) contact.setSupplierId(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public List<SupplierContact> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

}
