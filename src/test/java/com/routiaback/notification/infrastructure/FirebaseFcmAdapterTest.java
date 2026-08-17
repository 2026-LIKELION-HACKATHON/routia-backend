package com.routiaback.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.application.port.PushDeliveryErrorCode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FirebaseFcmAdapterTest {
    private final FirebaseMessaging messaging = Mockito.mock(FirebaseMessaging.class);
    private final FirebaseFcmAdapter adapter = new FirebaseFcmAdapter(messaging);

    @Test
    void normalizesSuccessfulDelivery() throws Exception {
        given(messaging.send(any(Message.class))).willReturn("message-id");

        PushNotificationPort.DeliveryResult result = adapter.send(
                "test-fid", "title", "body", Map.of("type", "DAILY_ROUTINE_READY"));

        assertThat(result.success()).isTrue();
    }

    @Test
    void classifiesUnregisteredFidAsPermanentFailure() throws Exception {
        FirebaseMessagingException exception = Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNREGISTERED);
        given(messaging.send(any(Message.class))).willThrow(exception);

        PushNotificationPort.DeliveryResult result = adapter.send(
                "test-fid", "title", "body", Map.of("type", "DAILY_ROUTINE_READY"));

        assertThat(result.success()).isFalse();
        assertThat(result.permanentFailure()).isTrue();
        assertThat(result.errorCode()).isEqualTo(PushDeliveryErrorCode.UNREGISTERED);
    }

    @Test
    void keepsFidActiveForTransientFailure() throws Exception {
        FirebaseMessagingException exception = Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.UNAVAILABLE);
        given(messaging.send(any(Message.class))).willThrow(exception);

        PushNotificationPort.DeliveryResult result = adapter.send(
                "test-fid", "title", "body", Map.of("type", "DAILY_ROUTINE_READY"));

        assertThat(result.permanentFailure()).isFalse();
        assertThat(result.errorCode()).isEqualTo(PushDeliveryErrorCode.TEMPORARY_FAILURE);
    }

    @Test
    void classifiesInvalidArgumentAsInvalidTarget() throws Exception {
        FirebaseMessagingException exception = Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.INVALID_ARGUMENT);
        given(messaging.send(any(Message.class))).willThrow(exception);

        PushNotificationPort.DeliveryResult result = adapter.send(
                "invalid-fid", "title", "body", Map.of("type", "DAILY_ROUTINE_READY"));

        assertThat(result.permanentFailure()).isTrue();
        assertThat(result.errorCode()).isEqualTo(PushDeliveryErrorCode.INVALID_TARGET);
    }

    @Test
    void classifiesFirebaseAuthenticationFailureWithoutDisablingFid() throws Exception {
        FirebaseMessagingException exception = Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR);
        given(messaging.send(any(Message.class))).willThrow(exception);

        PushNotificationPort.DeliveryResult result = adapter.send(
                "test-fid", "title", "body", Map.of("type", "DAILY_ROUTINE_READY"));

        assertThat(result.permanentFailure()).isFalse();
        assertThat(result.errorCode()).isEqualTo(PushDeliveryErrorCode.AUTH_FAILURE);
    }
}
