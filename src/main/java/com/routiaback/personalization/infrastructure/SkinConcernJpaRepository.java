package com.routiaback.personalization.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface SkinConcernJpaRepository extends JpaRepository<SkinConcernJpaEntity, String> {

    List<SkinConcernJpaEntity> findAllByCodeInAndActiveTrue(Collection<String> codes);
}
