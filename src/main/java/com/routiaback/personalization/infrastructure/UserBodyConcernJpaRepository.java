package com.routiaback.personalization.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserBodyConcernJpaRepository extends JpaRepository<UserBodyConcernJpaEntity, UserConcernId> {

    void deleteAllByUserId(Long userId);

    @Query("""
            select relation.concernCode
            from UserBodyConcernJpaEntity relation, BodyConcernJpaEntity concern
            where relation.concernCode = concern.code and relation.userId = :userId
            order by concern.sortOrder, concern.code
            """)
    List<String> findCodesByUserId(@Param("userId") Long userId);
}
