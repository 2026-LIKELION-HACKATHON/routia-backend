package com.routiaback.notification.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class FirebaseConfigurationTest {

    private final FirebaseConfiguration configuration = new FirebaseConfiguration();

    @Test
    void loadsBase64CredentialWithoutUsingLocalFile() throws Exception {
        String credentialJson = """
                {
                  "type": "authorized_user",
                  "client_id": "test-client.apps.googleusercontent.com",
                  "client_secret": "test-client-secret",
                  "refresh_token": "test-refresh-token"
                }
                """;
        String encoded = Base64.getEncoder().encodeToString(
                credentialJson.getBytes(StandardCharsets.UTF_8));

        GoogleCredentials credentials = configuration.loadCredentials(encoded, "", true);

        assertThat(credentials).isNotNull();
    }

    @Test
    void requiresBase64CredentialWhenProductionPolicyIsEnabled() {
        assertThatThrownBy(() -> configuration.loadCredentials("", "", true))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Base64 service account credential is required");
    }

    @Test
    void rejectsMalformedBase64WithoutIncludingCredentialValueInMessage() {
        String malformed = "not-a-base64-credential";

        assertThatThrownBy(() -> configuration.loadCredentials(malformed, "", true))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("malformed")
                .hasMessageNotContaining(malformed);
    }
}
