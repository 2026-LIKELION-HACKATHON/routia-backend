package com.routiaback.notification.application.port;

import java.util.Map;

public interface PushNotificationPort {
    DeliveryResult send(String token, String title, String body, Map<String, String> data);

    record DeliveryResult(boolean success, boolean permanentFailure, String errorCode) {
        public static DeliveryResult sent() {
            return new DeliveryResult(true, false, null);
        }

        public static DeliveryResult failed(String errorCode, boolean permanentFailure) {
            return new DeliveryResult(false, permanentFailure, errorCode);
        }
    }
}
