package com.merkador.productservice.infrastructure.persistence.repository;

import com.merkador.productservice.infrastructure.persistence.entity.EsIndexOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaEsOutboxRepository extends JpaRepository<EsIndexOutboxEntity, UUID> {

    @Query(value = """
            SELECT * FROM es_index_outbox
            WHERE processed = false AND retry_count < 5
            ORDER BY created_at ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<EsIndexOutboxEntity> findUnprocessed(@Param("limit") int limit);
}


