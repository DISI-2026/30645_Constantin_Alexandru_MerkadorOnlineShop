package com.merkador.productservice.core.port.out;

import com.merkador.productservice.infrastructure.persistence.entity.EsIndexOutboxEntity;

import java.util.List;
import java.util.UUID;

public interface EsOutboxRepository {

    void enqueue(UUID productId, String operation);

    List<EsIndexOutboxEntity> findUnprocessed(int limit);

    void markProcessed(UUID entryId);

    void incrementRetry(UUID entryId);
}


