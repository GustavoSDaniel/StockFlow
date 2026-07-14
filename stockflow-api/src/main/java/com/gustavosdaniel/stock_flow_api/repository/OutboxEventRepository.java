package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.OutboxEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Repositório reativo para a tabela de outbox.
 * <p>
 * Fornece consultas para o {@code OutboxScheduler} buscar eventos pendentes
 * e atualizar seu status de processamento.
 * </p>
 */
@Repository
public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, UUID> {

    /**
     * Busca até {@code limit} eventos não processados, ordenados por data de
     * criação (FIFO).
     *
     * @param limit número máximo de eventos a retornar
     * @return fluxo de eventos pendentes
     */
    @Query("SELECT * FROM outbox_events WHERE processed = FALSE ORDER BY created_at ASC LIMIT :limit")
    Flux<OutboxEvent> findPendingEvents(int limit);
}
