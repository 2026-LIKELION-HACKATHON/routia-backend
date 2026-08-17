package com.routiaback.personalization.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.routiaback.personalization.application.command.ProfileImageUpload;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalProfileImageStorageAdapterTest {

    @TempDir
    private Path directory;

    @Test
    void storesOnlyGeneratedKeyAndCanDeleteObject() throws Exception {
        LocalProfileImageStorageAdapter adapter = new LocalProfileImageStorageAdapter(directory.toString());

        String key = adapter.store(7L, new ProfileImageUpload(new byte[]{1, 2, 3}, "image/png"));

        assertThat(key).startsWith("7/").endsWith(".png");
        assertThat(Files.readAllBytes(directory.resolve(key))).containsExactly(1, 2, 3);

        adapter.delete(key);
        assertThat(directory.resolve(key)).doesNotExist();
    }
}
