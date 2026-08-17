package com.routiaback.personalization.infrastructure;

import com.routiaback.personalization.application.port.UserNeedsRepositoryPort;
import com.routiaback.personalization.application.port.UserProfileRepositoryPort;
import com.routiaback.personalization.domain.UserPreference;
import com.routiaback.personalization.domain.UserProfile;
import com.routiaback.personalization.domain.BodyGoal;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
class PersonalizationPersistenceAdapter implements UserProfileRepositoryPort, UserNeedsRepositoryPort {

    private final PersonalizationProfileJpaRepository profileRepository;
    private final UserPreferenceJpaRepository preferenceRepository;
    private final BodyConcernJpaRepository bodyConcernRepository;
    private final SkinConcernJpaRepository skinConcernRepository;
    private final UserBodyConcernJpaRepository userBodyConcernRepository;
    private final UserSkinConcernJpaRepository userSkinConcernRepository;
    private final BodyGoalJpaRepository bodyGoalRepository;
    private final UserBodyGoalJpaRepository userBodyGoalRepository;
    private final OwnedToolJpaRepository ownedToolRepository;
    private final UserOwnedToolJpaRepository userOwnedToolRepository;

    PersonalizationPersistenceAdapter(
            PersonalizationProfileJpaRepository profileRepository,
            UserPreferenceJpaRepository preferenceRepository,
            BodyConcernJpaRepository bodyConcernRepository,
            SkinConcernJpaRepository skinConcernRepository,
            UserBodyConcernJpaRepository userBodyConcernRepository,
            UserSkinConcernJpaRepository userSkinConcernRepository,
            BodyGoalJpaRepository bodyGoalRepository,
            UserBodyGoalJpaRepository userBodyGoalRepository,
            OwnedToolJpaRepository ownedToolRepository,
            UserOwnedToolJpaRepository userOwnedToolRepository
    ) {
        this.profileRepository = profileRepository;
        this.preferenceRepository = preferenceRepository;
        this.bodyConcernRepository = bodyConcernRepository;
        this.skinConcernRepository = skinConcernRepository;
        this.userBodyConcernRepository = userBodyConcernRepository;
        this.userSkinConcernRepository = userSkinConcernRepository;
        this.bodyGoalRepository = bodyGoalRepository;
        this.userBodyGoalRepository = userBodyGoalRepository;
        this.ownedToolRepository = ownedToolRepository;
        this.userOwnedToolRepository = userOwnedToolRepository;
    }

    @Override
    public Optional<UserProfile> findByUserId(Long userId) {
        return profileRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public UserProfile save(UserProfile profile) {
        return toDomain(profileRepository.saveAndFlush(toEntity(profile)));
    }

    @Override
    public Optional<UserPreference> findPreferenceByUserId(Long userId) {
        return preferenceRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public UserPreference savePreference(UserPreference preference) {
        return toDomain(preferenceRepository.save(toEntity(preference)));
    }

    @Override
    public List<String> findBodyConcernCodes(Long userId) {
        return userBodyConcernRepository.findCodesByUserId(userId);
    }

    @Override
    public List<String> findSkinConcernCodes(Long userId) {
        return userSkinConcernRepository.findCodesByUserId(userId);
    }

    @Override
    public List<BodyGoal> findBodyGoals(Long userId) {
        return userBodyGoalRepository.findCodesByUserId(userId).stream().map(BodyGoal::valueOf).toList();
    }

    @Override
    public List<String> findOwnedToolCodes(Long userId) {
        return userOwnedToolRepository.findCodesByUserId(userId);
    }

    @Override
    public Map<String, String> findBodyConcernNames(Collection<String> codes) {
        Map<String, String> namesByCode = bodyConcernRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .collect(Collectors.toMap(BodyConcernJpaEntity::code, BodyConcernJpaEntity::name));
        Map<String, String> ordered = new LinkedHashMap<>();
        codes.forEach(code -> {
            if (namesByCode.containsKey(code)) ordered.put(code, namesByCode.get(code));
        });
        return ordered;
    }

    @Override
    public Map<String, String> findSkinConcernNames(Collection<String> codes) {
        Map<String, String> namesByCode = skinConcernRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .collect(Collectors.toMap(SkinConcernJpaEntity::code, SkinConcernJpaEntity::name));
        Map<String, String> ordered = new LinkedHashMap<>();
        codes.forEach(code -> {
            if (namesByCode.containsKey(code)) ordered.put(code, namesByCode.get(code));
        });
        return ordered;
    }

    @Override
    public Map<String, String> findBodyGoalNames(Collection<BodyGoal> goals) {
        List<String> codes = goals.stream().map(Enum::name).toList();
        Map<String, String> names = bodyGoalRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .collect(Collectors.toMap(BodyGoalJpaEntity::code, BodyGoalJpaEntity::name));
        Map<String, String> ordered = new LinkedHashMap<>();
        codes.forEach(code -> { if (names.containsKey(code)) ordered.put(code, names.get(code)); });
        return ordered;
    }

    @Override
    public Map<String, String> findOwnedToolNames(Collection<String> codes) {
        Map<String, String> names = ownedToolRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .collect(Collectors.toMap(OwnedToolJpaEntity::code, OwnedToolJpaEntity::name));
        Map<String, String> ordered = new LinkedHashMap<>();
        codes.forEach(code -> { if (names.containsKey(code)) ordered.put(code, names.get(code)); });
        return ordered;
    }

    @Override
    public Set<String> findActiveBodyConcernCodes(Collection<String> codes) {
        return bodyConcernRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .map(BodyConcernJpaEntity::code)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> findActiveSkinConcernCodes(Collection<String> codes) {
        return skinConcernRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .map(SkinConcernJpaEntity::code)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<BodyGoal> findActiveBodyGoals(Collection<BodyGoal> goals) {
        return bodyGoalRepository.findAllByCodeInAndActiveTrue(goals.stream().map(Enum::name).toList()).stream()
                .map(entity -> BodyGoal.valueOf(entity.code())).collect(Collectors.toSet());
    }

    @Override
    public Set<String> findActiveOwnedToolCodes(Collection<String> codes) {
        return ownedToolRepository.findAllByCodeInAndActiveTrue(codes).stream()
                .map(OwnedToolJpaEntity::code).collect(Collectors.toSet());
    }

    @Override
    public void replaceBodyConcerns(Long userId, Collection<String> codes) {
        userBodyConcernRepository.deleteAllByUserId(userId);
        userBodyConcernRepository.flush();
        userBodyConcernRepository.saveAll(codes.stream()
                .map(code -> new UserBodyConcernJpaEntity(userId, code))
                .toList());
    }

    @Override
    public void replaceSkinConcerns(Long userId, Collection<String> codes) {
        userSkinConcernRepository.deleteAllByUserId(userId);
        userSkinConcernRepository.flush();
        userSkinConcernRepository.saveAll(codes.stream()
                .map(code -> new UserSkinConcernJpaEntity(userId, code))
                .toList());
    }

    @Override
    public void replaceBodyGoals(Long userId, Collection<BodyGoal> goals) {
        userBodyGoalRepository.deleteAllByUserId(userId);
        userBodyGoalRepository.flush();
        userBodyGoalRepository.saveAll(goals.stream()
                .map(goal -> new UserBodyGoalJpaEntity(userId, goal.name())).toList());
    }

    @Override
    public void replaceOwnedTools(Long userId, Collection<String> codes) {
        userOwnedToolRepository.deleteAllByUserId(userId);
        userOwnedToolRepository.flush();
        userOwnedToolRepository.saveAll(codes.stream()
                .map(code -> new UserOwnedToolJpaEntity(userId, code)).toList());
    }

    private PersonalizationProfileJpaEntity toEntity(UserProfile profile) {
        return new PersonalizationProfileJpaEntity(profile.userId(), profile.height(), profile.weight(),
                profile.gender(), profile.ageGroup(), profile.profileImageKey(), profile.regionSido(),
                profile.regionSigungu(), profile.latitude(), profile.longitude(), profile.locationSource(),
                profile.locationUpdatedAt(), profile.createdAt(), profile.updatedAt());
    }

    private UserProfile toDomain(PersonalizationProfileJpaEntity entity) {
        return new UserProfile(entity.userId(), entity.height(), entity.weight(), entity.gender(),
                entity.ageGroup(), entity.profileImageKey(), entity.regionSido(), entity.regionSigungu(),
                entity.latitude(), entity.longitude(), entity.locationSource(), entity.locationUpdatedAt(),
                entity.createdAt(), entity.updatedAt());
    }

    private UserPreferenceJpaEntity toEntity(UserPreference preference) {
        return new UserPreferenceJpaEntity(preference.userId(), preference.bodyGoal(), preference.skinType(),
                preference.routineTimePreference(), preference.routineDifficulty(), preference.createdAt(),
                preference.updatedAt());
    }

    private UserPreference toDomain(UserPreferenceJpaEntity entity) {
        return new UserPreference(entity.userId(), entity.bodyGoal(), entity.skinType(),
                entity.routineTimePreference(), entity.routineDifficulty(), entity.createdAt(), entity.updatedAt());
    }
}
