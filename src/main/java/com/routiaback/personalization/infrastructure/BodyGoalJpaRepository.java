package com.routiaback.personalization.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface BodyGoalJpaRepository extends JpaRepository<BodyGoalJpaEntity, String> {
    List<BodyGoalJpaEntity> findAllByCodeInAndActiveTrue(Collection<String> codes);
}
