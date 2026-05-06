package com.merkador.productservice.core.port.out;

public interface EventPublisher {

    void publishProductCreated(Object event);

    void publishProductUpdated(Object event);

    void publishProductDeleted(Object event);

    void publishStockUpdated(Object event);
}


