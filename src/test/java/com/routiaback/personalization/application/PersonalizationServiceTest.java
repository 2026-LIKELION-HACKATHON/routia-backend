package com.routiaback.personalization.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.domain.User;
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
import com.routiaback.personalization.domain.BodyGoal;
import com.routiaback.personalization.domain.Gender;
import com.routiaback.personalization.domain.LocationSource;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import com.routiaback.personalization.domain.SkinType;
import com.routiaback.personalization.domain.UserPreference;
import com.routiaback.personalization.domain.UserProfile;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersonalizationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-15T00:00:00Z");
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeProfileRepository profiles = new FakeProfileRepository();
    private final FakeNeedsRepository needs = new FakeNeedsRepository();
    private final FakeImageStorage images = new FakeImageStorage();
    private PersonalizationService service;

    @BeforeEach
    void setUp() {
        users.user = User.create("user@example.com", "hash", "Soeun", NOW).withId(1L);
        needs.activeBodyCodes.addAll(Set.of("SWELLING", "FATIGUE"));
        needs.activeSkinCodes.addAll(Set.of("ACNE", "PORE"));
        service = new PersonalizationService(users, profiles, needs, images,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void returnsNullableProfileWhenProfileRowDoesNotExist() {
        ProfileResult result = service.getProfile(1L, 1L);

        assertThat(result.height()).isNull();
        assertThat(result.profileImage()).isNull();
    }

    @Test
    void rejectsAccessToAnotherUsersPath() {
        assertThatThrownBy(() -> service.getProfile(1L, 2L))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_DATA_ACCESS_DENIED);
    }

    @Test
    void returnsNotFoundWhenAuthenticatedUserNoLongerExists() {
        users.user = null;

        assertThatThrownBy(() -> service.getProfile(1L, 1L))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void createsProfileAndStoresGpsLocation() {
        ProfileResult result = service.updateProfile(1L, 1L, new UpdateProfileCommand(
                new BigDecimal("165.5"), new BigDecimal("55.2"), Gender.FEMALE, null,
                null, null, new BigDecimal("37.5665000"),
                new BigDecimal("126.9780000"), LocationSource.GPS));

        assertThat(result.height()).isEqualByComparingTo("165.5");
        assertThat(result.locationSource()).isEqualTo(LocationSource.GPS);
        assertThat(result.locationUpdatedAt()).isEqualTo(NOW);
        assertThat(profiles.profile).isNotNull();
    }

    @Test
    void rejectsLocationChangeWithoutLocationSource() {
        assertThatThrownBy(() -> service.updateProfile(1L, 1L, new UpdateProfileCommand(
                null, null, null, null, null, null,
                new BigDecimal("37.5665000"), new BigDecimal("126.9780000"), null)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PROFILE_DATA);

        assertThat(profiles.profile).isNull();
    }

    @Test
    void replacesAndDeduplicatesConcernLists() {
        NeedsResult result = service.updateNeeds(1L, 1L, new UpdateNeedsCommand(
                BodyGoal.MAINTAIN, List.of("swelling", "SWELLING", "FATIGUE"),
                SkinType.DRY, List.of("ACNE"), RoutineTimePreference.MORNING,
                RoutineDifficulty.SIMPLE));

        assertThat(result.bodyConcerns()).containsExactly("SWELLING", "FATIGUE");
        assertThat(result.skinConcerns()).containsExactly("ACNE");
    }

    @Test
    void rejectsUnknownConcernWithoutChangingStoredNeeds() {
        assertThatThrownBy(() -> service.updateNeeds(1L, 1L, new UpdateNeedsCommand(
                null, List.of("UNKNOWN"), null, null, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_BODY_CONCERN);

        assertThat(needs.preference).isNull();
        assertThat(needs.bodyCodes).isEmpty();
    }

    @Test
    void emptyConcernArrayRemovesEverySelection() {
        needs.bodyCodes.add("SWELLING");

        NeedsResult result = service.updateNeeds(1L, 1L, new UpdateNeedsCommand(
                null, List.of(), null, null, null, null));

        assertThat(result.bodyConcerns()).isEmpty();
    }

    @Test
    void storageFailureDoesNotChangeProfileImageKey() {
        profiles.profile = UserProfile.empty(1L, NOW).updateProfileImage("old/key.jpg", NOW);
        images.failStore = true;

        assertThatThrownBy(() -> service.uploadProfileImage(
                1L, 1L, new ProfileImageUpload(new byte[]{1}, "image/jpeg")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_IMAGE_STORAGE_FAILED);

        assertThat(profiles.profile.profileImageKey()).isEqualTo("old/key.jpg");
    }

    @Test
    void rejectsUnsupportedOrOversizedProfileImage() {
        assertThatThrownBy(() -> service.uploadProfileImage(
                1L, 1L, new ProfileImageUpload(new byte[]{1}, "image/gif")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PROFILE_IMAGE);

        assertThatThrownBy(() -> service.uploadProfileImage(
                1L, 1L, new ProfileImageUpload(new byte[5 * 1024 * 1024 + 1], "image/jpeg")))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROFILE_IMAGE_TOO_LARGE);

        assertThat(profiles.profile).isNull();
    }

    @Test
    void removesNewObjectWhenDatabaseSaveFails() {
        profiles.failSave = true;

        assertThatThrownBy(() -> service.uploadProfileImage(
                1L, 1L, new ProfileImageUpload(new byte[]{1}, "image/png")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(images.deleted).containsExactly("1/new-key.png");
    }

    private static class FakeUserRepository implements UserRepositoryPort {
        private User user;

        @Override public boolean existsByEmail(String email) { return user != null && user.email().equals(email); }
        @Override public Optional<User> findByEmail(String email) { return existsByEmail(email) ? Optional.of(user) : Optional.empty(); }
        @Override public Optional<User> findById(Long id) { return user != null && user.id().equals(id) ? Optional.of(user) : Optional.empty(); }
        @Override public User save(User user) { this.user = user; return user; }
    }

    private static class FakeProfileRepository implements UserProfileRepositoryPort {
        private UserProfile profile;
        private boolean failSave;

        @Override public Optional<UserProfile> findByUserId(Long userId) { return Optional.ofNullable(profile); }
        @Override public UserProfile save(UserProfile profile) {
            if (failSave) throw new IllegalStateException("database unavailable");
            this.profile = profile;
            return profile;
        }
    }

    private static class FakeNeedsRepository implements UserNeedsRepositoryPort {
        private UserPreference preference;
        private final LinkedHashSet<String> bodyCodes = new LinkedHashSet<>();
        private final LinkedHashSet<String> skinCodes = new LinkedHashSet<>();
        private final Set<String> activeBodyCodes = new LinkedHashSet<>();
        private final Set<String> activeSkinCodes = new LinkedHashSet<>();

        @Override public Optional<UserPreference> findPreferenceByUserId(Long userId) { return Optional.ofNullable(preference); }
        @Override public UserPreference savePreference(UserPreference preference) { this.preference = preference; return preference; }
        @Override public List<String> findBodyConcernCodes(Long userId) { return List.copyOf(bodyCodes); }
        @Override public List<String> findSkinConcernCodes(Long userId) { return List.copyOf(skinCodes); }
        @Override public Set<String> findActiveBodyConcernCodes(Collection<String> codes) {
            Set<String> found = new LinkedHashSet<>(codes); found.retainAll(activeBodyCodes); return found;
        }
        @Override public Set<String> findActiveSkinConcernCodes(Collection<String> codes) {
            Set<String> found = new LinkedHashSet<>(codes); found.retainAll(activeSkinCodes); return found;
        }
        @Override public void replaceBodyConcerns(Long userId, Collection<String> codes) { bodyCodes.clear(); bodyCodes.addAll(codes); }
        @Override public void replaceSkinConcerns(Long userId, Collection<String> codes) { skinCodes.clear(); skinCodes.addAll(codes); }
    }

    private static class FakeImageStorage implements ProfileImageStoragePort {
        private boolean failStore;
        private final List<String> deleted = new ArrayList<>();

        @Override public String store(Long userId, ProfileImageUpload upload) {
            if (failStore) throw new IllegalStateException("storage unavailable");
            return userId + "/new-key." + (upload.contentType().equals("image/png") ? "png" : "jpg");
        }
        @Override public void delete(String key) { deleted.add(key); }
    }
}
