package com.routiaback.personalization.application;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.personalization.application.command.ProfileImageUpload;
import com.routiaback.personalization.application.command.UpdateNeedsCommand;
import com.routiaback.personalization.application.command.UpdateProfileCommand;
import com.routiaback.personalization.application.port.ProfileImageStoragePort;
import com.routiaback.personalization.application.port.UserNeedsRepositoryPort;
import com.routiaback.personalization.application.port.UserProfileRepositoryPort;
import com.routiaback.personalization.application.result.NeedsResult;
import com.routiaback.personalization.application.result.ProfileResult;
import com.routiaback.personalization.domain.LocationSource;
import com.routiaback.personalization.domain.UserPreference;
import com.routiaback.personalization.domain.UserProfile;
import com.routiaback.personalization.domain.UserProfilePatch;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalizationService {

    private static final long MAX_PROFILE_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final UserRepositoryPort userRepository;
    private final UserProfileRepositoryPort profileRepository;
    private final UserNeedsRepositoryPort needsRepository;
    private final ProfileImageStoragePort imageStorage;
    private final Clock clock;

    public PersonalizationService(
            UserRepositoryPort userRepository,
            UserProfileRepositoryPort profileRepository,
            UserNeedsRepositoryPort needsRepository,
            ProfileImageStoragePort imageStorage,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.needsRepository = needsRepository;
        this.imageStorage = imageStorage;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProfileResult getProfile(Long authenticatedUserId, Long userId) {
        ensureAccessibleUser(authenticatedUserId, userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.empty(userId, clock.instant()));
        return ProfileResult.from(profile);
    }

    @Transactional
    public ProfileResult updateProfile(Long authenticatedUserId, Long userId, UpdateProfileCommand command) {
        ensureAccessibleUser(authenticatedUserId, userId);
        Instant now = clock.instant();
        UserProfile current = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.empty(userId, now));
        UserProfilePatch patch = toPatch(command);
        UserProfile updated = current.update(patch, now);
        validateProfile(updated, patch.hasLocationChange());
        return ProfileResult.from(profileRepository.save(updated));
    }

    @Transactional(readOnly = true)
    public NeedsResult getNeeds(Long authenticatedUserId, Long userId) {
        ensureAccessibleUser(authenticatedUserId, userId);
        UserPreference preference = needsRepository.findPreferenceByUserId(userId)
                .orElseGet(() -> UserPreference.empty(userId, clock.instant()));
        return NeedsResult.from(preference, needsRepository.findBodyConcernCodes(userId),
                needsRepository.findSkinConcernCodes(userId));
    }

    @Transactional
    public NeedsResult updateNeeds(Long authenticatedUserId, Long userId, UpdateNeedsCommand command) {
        ensureAccessibleUser(authenticatedUserId, userId);
        LinkedHashSet<String> bodyConcerns = normalizeCodes(command.bodyConcerns());
        LinkedHashSet<String> skinConcerns = normalizeCodes(command.skinConcerns());
        validateConcernCodes(bodyConcerns, true);
        validateConcernCodes(skinConcerns, false);

        Instant now = clock.instant();
        UserPreference current = needsRepository.findPreferenceByUserId(userId)
                .orElseGet(() -> UserPreference.empty(userId, now));
        UserPreference saved = needsRepository.savePreference(current.update(
                command.bodyGoal(), command.skinType(), command.routineTimePreference(),
                command.routineDifficulty(), now));

        if (bodyConcerns != null) {
            needsRepository.replaceBodyConcerns(userId, bodyConcerns);
        }
        if (skinConcerns != null) {
            needsRepository.replaceSkinConcerns(userId, skinConcerns);
        }
        return NeedsResult.from(saved, needsRepository.findBodyConcernCodes(userId),
                needsRepository.findSkinConcernCodes(userId));
    }

    @Transactional
    public ProfileResult uploadProfileImage(
            Long authenticatedUserId,
            Long userId,
            ProfileImageUpload upload
    ) {
        ensureAccessibleUser(authenticatedUserId, userId);
        validateImage(upload);
        Instant now = clock.instant();
        UserProfile current = profileRepository.findByUserId(userId)
                .orElseGet(() -> UserProfile.empty(userId, now));
        String newKey;
        try {
            newKey = imageStorage.store(userId, upload);
        } catch (RuntimeException exception) {
            throw new ApiException(ErrorCode.PROFILE_IMAGE_STORAGE_FAILED, exception);
        }

        try {
            UserProfile saved = profileRepository.save(current.updateProfileImage(newKey, now));
            if (current.profileImageKey() != null && !current.profileImageKey().equals(newKey)) {
                imageStorage.delete(current.profileImageKey());
            }
            return ProfileResult.from(saved);
        } catch (RuntimeException exception) {
            imageStorage.delete(newKey);
            throw exception;
        }
    }

    private void ensureAccessibleUser(Long authenticatedUserId, Long userId) {
        if (!Objects.equals(authenticatedUserId, userId)) {
            throw new ApiException(ErrorCode.USER_DATA_ACCESS_DENIED);
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND))
                .validateLoginAllowed();
    }

    private UserProfilePatch toPatch(UpdateProfileCommand command) {
        return new UserProfilePatch(command.height(), command.weight(), command.gender(), command.ageGroup(),
                command.regionSido(), command.regionSigungu(), command.latitude(), command.longitude(),
                command.locationSource());
    }

    private void validateProfile(UserProfile profile, boolean locationChanged) {
        if (profile.height() != null && profile.height().signum() <= 0
                || profile.weight() != null && profile.weight().signum() <= 0
                || outside(profile.height(), "0.1", "9999.9")
                || outside(profile.weight(), "0.1", "9999.9")
                || hasTooManyFractionDigits(profile.height(), 1)
                || hasTooManyFractionDigits(profile.weight(), 1)
                || outside(profile.latitude(), "-90", "90")
                || outside(profile.longitude(), "-180", "180")
                || hasTooManyFractionDigits(profile.latitude(), 7)
                || hasTooManyFractionDigits(profile.longitude(), 7)) {
            throw new ApiException(ErrorCode.INVALID_PROFILE_DATA);
        }
        if (locationChanged && profile.locationSource() == null) {
            throw new ApiException(ErrorCode.INVALID_PROFILE_DATA);
        }
        if (profile.locationSource() == LocationSource.GPS
                && (profile.latitude() == null || profile.longitude() == null)) {
            throw new ApiException(ErrorCode.INVALID_PROFILE_DATA);
        }
        if (profile.locationSource() == LocationSource.MANUAL
                && (isBlank(profile.regionSido()) || isBlank(profile.regionSigungu()))) {
            throw new ApiException(ErrorCode.INVALID_PROFILE_DATA);
        }
    }

    private boolean outside(BigDecimal value, String minimum, String maximum) {
        return value != null && (value.compareTo(new BigDecimal(minimum)) < 0
                || value.compareTo(new BigDecimal(maximum)) > 0);
    }

    private boolean hasTooManyFractionDigits(BigDecimal value, int maximumScale) {
        return value != null && Math.max(value.stripTrailingZeros().scale(), 0) > maximumScale;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private LinkedHashSet<String> normalizeCodes(List<String> codes) {
        if (codes == null) {
            return null;
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String code : codes) {
            if (code == null || code.isBlank()) {
                normalized.add("");
            } else {
                normalized.add(code.trim().toUpperCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private void validateConcernCodes(Collection<String> codes, boolean body) {
        if (codes == null || codes.isEmpty()) {
            return;
        }
        Set<String> activeCodes = body
                ? needsRepository.findActiveBodyConcernCodes(codes)
                : needsRepository.findActiveSkinConcernCodes(codes);
        if (!activeCodes.containsAll(codes)) {
            throw new ApiException(body ? ErrorCode.INVALID_BODY_CONCERN : ErrorCode.INVALID_SKIN_CONCERN);
        }
    }

    private void validateImage(ProfileImageUpload upload) {
        if (upload == null || upload.size() == 0 || !ALLOWED_PROFILE_IMAGE_TYPES.contains(upload.contentType())) {
            throw new ApiException(ErrorCode.INVALID_PROFILE_IMAGE);
        }
        if (upload.size() > MAX_PROFILE_IMAGE_SIZE) {
            throw new ApiException(ErrorCode.PROFILE_IMAGE_TOO_LARGE);
        }
    }
}
