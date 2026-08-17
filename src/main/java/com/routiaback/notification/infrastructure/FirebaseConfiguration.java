package com.routiaback.notification.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "routia.notification.provider", havingValue = "firebase")
class FirebaseConfiguration {
    private static final String APP_NAME = "routia-fcm";

    @Bean(destroyMethod = "delete")
    FirebaseApp firebaseApp(
            @Value("${routia.notification.firebase.service-account-base64:}") String serviceAccountBase64,
            @Value("${routia.notification.firebase.credentials-path:}") String credentialsPath,
            @Value("${routia.notification.firebase.require-service-account-base64:false}") boolean requireBase64) {
        return initializeFirebaseApp(serviceAccountBase64, credentialsPath, requireBase64);
    }

    FirebaseApp firebaseApp(String credentialsPath) {
        return initializeFirebaseApp("", credentialsPath, false);
    }

    private FirebaseApp initializeFirebaseApp(
            String serviceAccountBase64, String credentialsPath, boolean requireBase64) {
        try {
            FirebaseApp existing = FirebaseApp.getApps().stream()
                    .filter(app -> APP_NAME.equals(app.getName()))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
            GoogleCredentials credentials = loadCredentials(
                    serviceAccountBase64, credentialsPath, requireBase64);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            return FirebaseApp.initializeApp(options, APP_NAME);
        } catch (IllegalArgumentException | IOException exception) {
            throw new IllegalStateException("Firebase credentials are invalid", exception);
        }
    }

    GoogleCredentials loadCredentials(
            String serviceAccountBase64, String credentialsPath, boolean requireBase64) throws IOException {
        if (serviceAccountBase64 != null && !serviceAccountBase64.isBlank()) {
            return decodeCredentials(serviceAccountBase64);
        }
        if (requireBase64) {
            throw new IOException("Firebase Base64 service account credential is required");
        }
        if (credentialsPath == null || credentialsPath.isBlank()) {
            return GoogleCredentials.getApplicationDefault();
        }
        Path path = Path.of(credentialsPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IOException("Firebase credential file is not readable");
        }
        try (InputStream input = Files.newInputStream(path)) {
            return GoogleCredentials.fromStream(input);
        }
    }

    private GoogleCredentials decodeCredentials(String serviceAccountBase64) throws IOException {
        try {
            byte[] json = Base64.getDecoder().decode(serviceAccountBase64.strip());
            try (InputStream input = new ByteArrayInputStream(json)) {
                return GoogleCredentials.fromStream(input);
            }
        } catch (IllegalArgumentException exception) {
            throw new IOException("Firebase Base64 service account credential is malformed", exception);
        }
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
