package com.routiaback.auth.infrastructure;

import com.routiaback.auth.domain.VerificationPurpose;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, Long> {

	Optional<EmailVerificationJpaEntity> findFirstByEmailAndPurposeOrderByCreatedAtDescIdDesc(String email, VerificationPurpose purpose);
}
