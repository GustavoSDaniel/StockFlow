package com.gustavosdaniel.stock_flow_api.domain.po;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

abstract class BaseImmutableEntity implements Persistable<UUID> {

    @Id
    private UUID id = UUID.randomUUID();

    @CreatedBy
    private UUID createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @Override
    @Transient
    public boolean isNew() {
        return this.createdAt == null;
    }


    @Override
    public UUID getId() {
        return id;
    }


    public UUID getCreatedBy() {
        return createdBy;
    }

     void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

     void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseImmutableEntity that = (BaseImmutableEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
