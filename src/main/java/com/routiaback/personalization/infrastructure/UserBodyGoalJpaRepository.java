package com.routiaback.personalization.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserBodyGoalJpaRepository extends JpaRepository<UserBodyGoalJpaEntity, UserSelectionId> {
    void deleteAllByUserId(Long userId);
    @Query("""
            select relation.code from UserBodyGoalJpaEntity relation, BodyGoalJpaEntity goal
            where relation.code = goal.code and relation.userId = :userId
            order by goal.sortOrder, goal.code
            """)
    List<String> findCodesByUserId(@Param("userId") Long userId);
}
