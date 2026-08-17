package com.routiaback.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.domain.NotificationType;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

@Tag("external")
class FirebaseFcmExternalIntegrationTest {
    @Test
    void sendsToRealWebRegistrationTokenOnlyWhenExplicitCredentialsExist() throws Exception {
        String credentialsBase64 = System.getenv("FIREBASE_CREDENTIALS_BASE64");
        String testToken = System.getenv("FCM_TEST_TOKEN");
        assumeTrue(credentialsBase64 != null && !credentialsBase64.isBlank()
                && testToken != null && !testToken.isBlank());

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(
                Base64.getDecoder().decode(credentialsBase64)));
        FirebaseApp app = FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(credentials).build(),
                "routia-fcm-external-test");
        try {
            PushNotificationPort.DeliveryResult result = new FirebaseFcmAdapter(
                    FirebaseMessaging.getInstance(app)).send(testToken, "Routia FCM 연동 테스트",
                    "Web Push 연동 확인용 메시지입니다.",
                    Map.of("type", NotificationType.DAILY_ROUTINE_READY.name(),
                            "routineId", "0", "routineDate", "2026-08-17"));
            assertThat(result.success()).isTrue();
        } finally {
            app.delete();
        }
    }
}
