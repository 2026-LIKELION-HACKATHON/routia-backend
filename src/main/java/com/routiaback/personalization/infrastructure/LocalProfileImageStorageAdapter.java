package com.routiaback.personalization.infrastructure;

import com.routiaback.personalization.application.command.ProfileImageUpload;
import com.routiaback.personalization.application.port.ProfileImageStoragePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class LocalProfileImageStorageAdapter implements ProfileImageStoragePort {

    private static final Logger log = LoggerFactory.getLogger(LocalProfileImageStorageAdapter.class);
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path rootDirectory;

    LocalProfileImageStorageAdapter(
            @Value("${routia.profile-image.directory:${java.io.tmpdir}/routia-profile-images}") String rootDirectory
    ) {
        this.rootDirectory = Path.of(rootDirectory).toAbsolutePath().normalize();
    }

    @Override
    public String store(Long userId, ProfileImageUpload upload) {
        String extension = EXTENSIONS.get(upload.contentType());
        String key = userId + "/" + UUID.randomUUID() + "." + extension;
        Path target = resolveKey(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, upload.content());
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store profile image", exception);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveKey(key));
        } catch (IOException | IllegalArgumentException exception) {
            log.warn("Failed to delete profile image. key={}", key, exception);
        }
    }

    private Path resolveKey(String key) {
        Path resolved = rootDirectory.resolve(key).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("Invalid profile image key");
        }
        return resolved;
    }
}
