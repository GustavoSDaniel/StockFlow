package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toNotificationResponse(Notification notification){

        if (notification == null) return null;

        return new NotificationResponse(

                notification.getId(),
                notification.getProductId(),
                notification.getProductName(),
                notification.getProductSku(),
                notification.getNotificationType(),
                notification.getNotificationPriority(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getCurrentQuantity(),
                notification.getMinimumQuantity(),
                notification.getMaximumQuantity(),
                notification.getReorderPoint(),
                notification.isRead(),
                notification.getReadAt(),
                notification.isResolved(),
                notification.getResolvedAt()

        );
    }
}
