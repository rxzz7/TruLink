package com.tru_link.Trulink.repo;

import com.tru_link.Trulink.entity.LinkItemResponse;
import com.tru_link.Trulink.entity.ResolvedLinkProjection;
import com.tru_link.Trulink.entity.WebUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UrlRepo extends JpaRepository<WebUrl, UUID> {

    @Query("SELECT u.shortKey FROM WebUrl u")
    List<String> findAllShortKeys();

    @Query("""
                SELECT w.linkId AS linkId, w.originalUrl AS originalUrl, w.userId AS userId
                FROM WebUrl w
                WHERE w.shortKey = :shortKey
            """)
    ResolvedLinkProjection findResolvedByShortKey(@Param("shortKey") String shortKey);

    @Query("SELECT w.linkId FROM WebUrl w WHERE w.shortKey = :shortKey AND w.userId = :userId")
    Optional<UUID> findLinkIdByShortKeyAndUserId(@Param("shortKey") String shortKey, @Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WebUrl w WHERE w.shortKey = :shortKey AND w.userId = :userId")
    void deleteByShortKeyAndUserId(@Param("shortKey") String shortKey, @Param("userId") UUID userId);

    @Query("""
                SELECT new com.tru_link.Trulink.entity.LinkItemResponse(
                    w.shortKey, w.shortKey, w.originalUrl, w.createdAt
                )
                FROM WebUrl w
                WHERE w.userId = :userId
            """)
    Page<LinkItemResponse> findUserLinks(@Param("userId") UUID userId, Pageable pageable);
}
