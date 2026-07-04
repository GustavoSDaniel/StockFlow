package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;

import java.time.LocalDateTime;

public record NotificationFilter(

        LocalDateTime from,
        LocalDateTime to,
        NotificationType type,
        NotificationPriority priority,
        Boolean read,
        Boolean resolved
) {
    public NotificationFilter{

        if (from != null && to != null && from.isAfter(to))
            throw new BusinessRuleException("A data inicial não pode ser maior que a data final");
    }
}
