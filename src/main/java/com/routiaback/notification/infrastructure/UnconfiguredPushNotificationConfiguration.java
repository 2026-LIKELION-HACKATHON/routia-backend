package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.application.port.PushDeliveryErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "routia.notification.provider", havingValue = "unconfigured", matchIfMissing = true)
class UnconfiguredPushNotificationConfiguration {
    @Bean
    PushNotificationPort unconfiguredPushNotificationPort() {
        return (installationId, title, body, data) ->
                PushNotificationPort.DeliveryResult.failed(PushDeliveryErrorCode.AUTH_FAILURE, false);
    }
}
