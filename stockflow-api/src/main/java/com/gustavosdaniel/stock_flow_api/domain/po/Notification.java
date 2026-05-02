package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("notifications")
public class Notification extends BaseEntity{

    public Notification(){}

    public Notification(UUID productId, String productName, String productSku, NotificationType notificationType, NotificationPriority notificationPriority, String title, String message, Integer currentQuantity, Integer minimumQuantity, UUID assignedTo) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.notificationType = notificationType;
        this.notificationPriority = notificationPriority;
        this.title = title;
        this.message = message;
        this.currentQuantity = currentQuantity;
        this.minimumQuantity = minimumQuantity;
        this.assignedTo = assignedTo;
    }

    @Column("product_id")
    private UUID productId;

    @Column("product_name")
    private String productName;

    @Column("product_sku")
    private String productSku;

    @Column("notification_type")
    private NotificationType notificationType;

    @Column("notification_priority")
    private NotificationPriority notificationPriority;

    private String title;

    private String message;

    @Column("current_quantity")
    private Integer currentQuantity;

    @Column("minimum_quantity")
    private Integer minimumQuantity;

    @Column("is_read")
    private boolean read =  false;

    @Column("is_resolved")
    private boolean resolved = false;

    @Column("assigned_to")
    private UUID assignedTo;

    @Column("read_at")
    private LocalDateTime readAt;

    @Column("resolved_at")
    private LocalDateTime resolvedAt;

    public void assingTo(UUID userId){
        if (userId == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
        this.assignedTo = userId;
    }

    public void markAsRead(){
        if (this.read) return;
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsResolved(){

        if (this.resolved) return;
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        if (!this.read) markAsRead();
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationPriority getNotificationPriority() {
        return notificationPriority;
    }

    public void setNotificationPriority(NotificationPriority notificationPriority) {
        this.notificationPriority = notificationPriority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(Integer currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

}
