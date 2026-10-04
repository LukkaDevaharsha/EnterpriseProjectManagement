package com.enterprise.project_management.dto.notification;

import java.util.List;

public record NotificationInboxResponse(List<NotificationResponse> notifications, long unreadCount) {}
