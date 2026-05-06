package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.port.out.EsOutboxRepository;
import com.merkador.productservice.infrastructure.persistence.entity.EsIndexOutboxEntity;
import com.merkador.productservice.infrastructure.persistence.repository.JpaEsOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EsOutboxRepositoryAdapter implements EsOutboxRepository {

    private final JpaEsOutboxRepository jpa;

    @Override
    public void enqueue(UUID productId, String operation) {
        EsIndexOutboxEntity entry = EsIndexOutboxEntity.builder()
                .productId(productId)
                .operation(operation)
                .processed(false)
                .retryCount(0)
                .build();
        jpa.save(entry);
    }

    @Override
    public List<EsIndexOutboxEntity> findUnprocessed(int limit) {
        return jpa.findUnprocessed(limit);
    }

    @Override
    public void markProcessed(UUID entryId) {
        jpa.findById(entryId).ifPresent(entry -> {
            entry.setProcessed(true);
            entry.setProcessedAt(OffsetDateTime.now());
            jpa.save(entry);
        });
    }

    @Override
    public void incrementRetry(UUID entryId) {
        jpa.findById(entryId).ifPresent(entry -> {
            entry.setRetryCount(entry.getRetryCount() + 1);
            jpa.save(entry);
        });
    }
}


