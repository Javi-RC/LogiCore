package com.logicore.notificationservice.application.port.in;

import com.logicore.notificationservice.application.command.RecordNotificationCommand;

/**
 * Inbound port: record a notification derived from a business event.
 */
public interface RecordNotificationUseCase {

    void record(RecordNotificationCommand command);
}