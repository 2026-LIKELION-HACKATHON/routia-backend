package com.routiaback.personalization.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface OwnedToolJpaRepository extends JpaRepository<OwnedToolJpaEntity, String> {
    List<OwnedToolJpaEntity> findAllByCodeInAndActiveTrue(Collection<String> codes);
}
