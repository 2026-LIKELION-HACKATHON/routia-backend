package com.routiaback.notification.infrastructure;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.application.port.PushDeliveryErrorCode;
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
    public DeliveryResult send(String installationId, String title, String body, Map<String, String> data) {
        Message message = Message.builder()
                .setFid(installationId)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .putAllData(data)
                .build();
        try {
            messaging.send(message);
            return DeliveryResult.sent();
        } catch (FirebaseMessagingException exception) {
            MessagingErrorCode code = exception.getMessagingErrorCode();
            return classify(code);
        }
    }

    private DeliveryResult classify(MessagingErrorCode code) {
        if (code == MessagingErrorCode.UNREGISTERED) {
            return DeliveryResult.failed(PushDeliveryErrorCode.UNREGISTERED, true);
        }
        if (code == MessagingErrorCode.INVALID_ARGUMENT) {
            return DeliveryResult.failed(PushDeliveryErrorCode.INVALID_TARGET, true);
        }
        if (code == MessagingErrorCode.UNAVAILABLE || code == MessagingErrorCode.INTERNAL
                || code == MessagingErrorCode.QUOTA_EXCEEDED) {
            return DeliveryResult.failed(PushDeliveryErrorCode.TEMPORARY_FAILURE, false);
        }
        if (code == MessagingErrorCode.THIRD_PARTY_AUTH_ERROR
                || code == MessagingErrorCode.SENDER_ID_MISMATCH) {
            return DeliveryResult.failed(PushDeliveryErrorCode.AUTH_FAILURE, false);
        }
        return DeliveryResult.failed(PushDeliveryErrorCode.UNKNOWN, false);
    }
}
