package com.routiaback.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.routiaback.notification.application.port.PushNotificationPort;
import com.routiaback.notification.domain.NotificationType;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("external")
class FirebaseFcmExternalIntegrationTest {
    @Test
    void initializesFirebaseAdminWithApplicationDefaultCredentials() {
        assumeTrue(hasEnvironment("GOOGLE_APPLICATION_CREDENTIALS"));

        FirebaseConfiguration configuration = new FirebaseConfiguration();
        FirebaseApp app = configuration.firebaseApp("");
        try {
            assertThat(app.getName()).isEqualTo("routia-fcm");
            assertThat(configuration.firebaseApp("")).isSameAs(app);
        } finally {
            app.delete();
        }
    }

    @Test
    void sendsToRealFirebaseInstallationOnlyWhenExplicitFidExists() {
        String testFid = System.getenv("TEST_FIREBASE_FID");
        assumeTrue(hasEnvironment("GOOGLE_APPLICATION_CREDENTIALS")
                && testFid != null && !testFid.isBlank());

        FirebaseApp app = new FirebaseConfiguration().firebaseApp("");
        try {
            PushNotificationPort.DeliveryResult result = new FirebaseFcmAdapter(
                    FirebaseMessaging.getInstance(app)).send(testFid, "Routia FCM 연동 테스트",
                    "Web Push 연동 확인용 메시지입니다.",
                    Map.of("type", NotificationType.DAILY_ROUTINE_READY.name(),
                            "routineId", "0", "routineDate", "2026-08-17"));
            assertThat(result.success()).isTrue();
        } finally {
            app.delete();
        }
    }

    private boolean hasEnvironment(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank();
    }
}
