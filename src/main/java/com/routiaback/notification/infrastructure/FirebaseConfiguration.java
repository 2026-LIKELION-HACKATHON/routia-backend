package com.routiaback.notification.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "routia.notification.provider", havingValue = "firebase")
class FirebaseConfiguration {
    @Bean(destroyMethod = "delete")
    FirebaseApp firebaseApp(
            @Value("${routia.notification.firebase.credentials-base64:}") String credentialsBase64) {
        if (credentialsBase64 == null || credentialsBase64.isBlank()) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS_BASE64 is required for firebase provider");
        }
        try {
            byte[] credentialsJson = Base64.getDecoder().decode(credentialsBase64.trim());
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(credentialsJson));
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            return FirebaseApp.initializeApp(options, "routia-fcm");
        } catch (IllegalArgumentException | IOException exception) {
            throw new IllegalStateException("Firebase credentials are invalid", exception);
        }
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
