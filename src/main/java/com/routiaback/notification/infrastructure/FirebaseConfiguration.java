package com.routiaback.notification.infrastructure;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
            @Value("${routia.notification.firebase.credentials-path:}") String credentialsPath) {
        try {
            FirebaseApp existing = FirebaseApp.getApps().stream()
                    .filter(app -> APP_NAME.equals(app.getName()))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
            GoogleCredentials credentials = loadCredentials(credentialsPath);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            return FirebaseApp.initializeApp(options, APP_NAME);
        } catch (IllegalArgumentException | IOException exception) {
            throw new IllegalStateException("Firebase credentials are invalid", exception);
        }
    }

    private GoogleCredentials loadCredentials(String credentialsPath) throws IOException {
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

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
