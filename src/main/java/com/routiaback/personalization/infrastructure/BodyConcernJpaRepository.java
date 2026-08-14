package com.routiaback.personalization.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface BodyConcernJpaRepository extends JpaRepository<BodyConcernJpaEntity, String> {

    List<BodyConcernJpaEntity> findAllByCodeInAndActiveTrue(Collection<String> codes);
}
