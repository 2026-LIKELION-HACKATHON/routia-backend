package com.routiaback.personalization.application.port;

import com.routiaback.personalization.application.command.ProfileImageUpload;

public interface ProfileImageStoragePort {

    String store(Long userId, ProfileImageUpload upload);

    void delete(String key);
}
