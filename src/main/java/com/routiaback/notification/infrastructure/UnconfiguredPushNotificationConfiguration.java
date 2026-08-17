package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.PushNotificationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "routia.notification.provider", havingValue = "unconfigured", matchIfMissing = true)
class UnconfiguredPushNotificationConfiguration {
    @Bean
    PushNotificationPort unconfiguredPushNotificationPort() {
        return (token, title, body, data) ->
                PushNotificationPort.DeliveryResult.failed("PROVIDER_NOT_CONFIGURED", false);
    }
}
