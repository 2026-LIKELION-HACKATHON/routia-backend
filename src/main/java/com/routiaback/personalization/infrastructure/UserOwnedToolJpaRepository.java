package com.routiaback.personalization.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserOwnedToolJpaRepository extends JpaRepository<UserOwnedToolJpaEntity, UserSelectionId> {
    void deleteAllByUserId(Long userId);
    @Query("""
            select relation.code from UserOwnedToolJpaEntity relation, OwnedToolJpaEntity tool
            where relation.code = tool.code and relation.userId = :userId
            order by tool.sortOrder, tool.code
            """)
    List<String> findCodesByUserId(@Param("userId") Long userId);
}
