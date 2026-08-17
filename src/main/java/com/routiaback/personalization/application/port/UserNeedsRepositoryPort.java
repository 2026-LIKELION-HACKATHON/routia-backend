package com.routiaback.personalization.application.port;

import com.routiaback.personalization.domain.UserPreference;
import com.routiaback.personalization.domain.BodyGoal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UserNeedsRepositoryPort {

    Optional<UserPreference> findPreferenceByUserId(Long userId);

    UserPreference savePreference(UserPreference preference);

    List<String> findBodyConcernCodes(Long userId);

    List<String> findSkinConcernCodes(Long userId);

    List<BodyGoal> findBodyGoals(Long userId);

    List<String> findOwnedToolCodes(Long userId);

    Map<String, String> findBodyConcernNames(Collection<String> codes);

    Map<String, String> findSkinConcernNames(Collection<String> codes);

    Map<String, String> findBodyGoalNames(Collection<BodyGoal> goals);

    Map<String, String> findOwnedToolNames(Collection<String> codes);

    Set<String> findActiveBodyConcernCodes(Collection<String> codes);

    Set<String> findActiveSkinConcernCodes(Collection<String> codes);

    Set<BodyGoal> findActiveBodyGoals(Collection<BodyGoal> goals);

    Set<String> findActiveOwnedToolCodes(Collection<String> codes);

    void replaceBodyConcerns(Long userId, Collection<String> codes);

    void replaceSkinConcerns(Long userId, Collection<String> codes);

    void replaceBodyGoals(Long userId, Collection<BodyGoal> goals);

    void replaceOwnedTools(Long userId, Collection<String> codes);
}
