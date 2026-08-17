package com.routiaback.notification.infrastructure;

import com.routiaback.notification.application.port.PushNotificationPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class UnconfiguredPushNotificationConfiguration {
    @Bean
    @ConditionalOnMissingBean(PushNotificationPort.class)
    PushNotificationPort unconfiguredPushNotificationPort() {
        return (token, title, body, data) ->
                PushNotificationPort.DeliveryResult.failed("PROVIDER_NOT_CONFIGURED", false);
    }
}
