package com.merkador.productservice.infrastructure.elasticsearch;

import com.merkador.productservice.core.port.out.EsOutboxRepository;
import com.merkador.productservice.infrastructure.persistence.entity.EsIndexOutboxEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Outbox processor: polls unprocessed ES_INDEX_OUTBOX entries
 * and calls the Elasticsearch API to keep the index in sync.
 *
 * In a real deployment this would call the Elasticsearch REST client.
 * The stub logs the operation — replace with actual ES client calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsOutboxProcessor {

    private final EsOutboxRepository outboxRepository;

    @Value("${app.elasticsearch.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.elasticsearch.outbox.scheduler-delay-ms:5000}")
    public void process() {
        List<EsIndexOutboxEntity> entries = outboxRepository.findUnprocessed(batchSize);
        if (entries.isEmpty()) return;

        log.debug("Processing {} ES outbox entries", entries.size());

        for (EsIndexOutboxEntity entry : entries) {
            try {
                syncToElasticsearch(entry);
                outboxRepository.markProcessed(entry.getId());
            } catch (Exception e) {
                log.error("ES sync failed for entry {}: {}", entry.getId(), e.getMessage());
                outboxRepository.incrementRetry(entry.getId());
            }
        }
    }

    private void syncToElasticsearch(EsIndexOutboxEntity entry) {
        // TODO: inject ElasticsearchClient and call index/delete API
        // Example (with Spring Data Elasticsearch or RestClient):
        //   if ("UPSERT".equals(entry.getOperation())) {
        //       Product p = productRepository.findById(entry.getProductId()).orElseThrow();
        //       esClient.index(i -> i.index("products").id(p.getId().toString()).document(toDoc(p)));
        //   } else {
        //       esClient.delete(d -> d.index("products").id(entry.getProductId().toString()));
        //   }
        log.info("ES sync [{}] productId={}", entry.getOperation(), entry.getProductId());
    }
}
