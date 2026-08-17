package com.routiaback.notification.infrastructure;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.routiaback.notification.application.port.PushNotificationPort;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "routia.notification.provider", havingValue = "firebase")
public class FirebaseFcmAdapter implements PushNotificationPort {
    private final FirebaseMessaging messaging;

    public FirebaseFcmAdapter(FirebaseMessaging messaging) {
        this.messaging = messaging;
    }

    @Override
    @SuppressWarnings("deprecation")
    public DeliveryResult send(String token, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build();
        try {
            messaging.send(message);
            return DeliveryResult.sent();
        } catch (FirebaseMessagingException exception) {
            MessagingErrorCode code = exception.getMessagingErrorCode();
            String normalizedCode = code == null ? "FIREBASE_ERROR" : code.name();
            boolean permanent = code == MessagingErrorCode.UNREGISTERED
                    || code == MessagingErrorCode.INVALID_ARGUMENT;
            return DeliveryResult.failed(normalizedCode, permanent);
        }
    }
}
