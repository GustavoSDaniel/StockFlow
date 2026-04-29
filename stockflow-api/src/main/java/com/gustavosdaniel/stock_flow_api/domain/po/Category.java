package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Table("categories")
public class Category extends BaseEntity {

    public Category(){}

    public Category(String name, String description, UUID parentId, boolean active) {
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.active = active;
    }

    private String name;

    private String description;

    @Column("parent_id")
    private UUID parentId;

    private boolean active = true;

    @Transient
    private List<Category> subCategories = new ArrayList<>();

    public boolean isRootCategory(){

        return parentId == null;
    }

    public void addSubCategory(Category category){

        if (category == null ) return;

        category.setParentId(this.getId());

        this.subCategories.add(category);
    }

    public void removeSubCategory(Category category){

        if (category == null) return;

        this.subCategories.remove(category);

        category.setParentId(null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Category> getSubCategories() {
        return Collections.unmodifiableList(subCategories);
    }

}
