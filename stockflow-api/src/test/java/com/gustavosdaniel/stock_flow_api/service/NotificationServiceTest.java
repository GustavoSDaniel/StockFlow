package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.mapping.NotificationMapper;
import com.gustavosdaniel.stock_flow_api.repository.NotificationRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

}