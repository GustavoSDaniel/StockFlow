package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa um evento pendente de publicação na tabela de outbox.
 * <p>
 * Implementa o padrão Transactional Outbox: eventos de domínio são persistidos
 * nesta tabela na mesma transação que a operação de negócio, e um processo
 * assíncrono ({@code OutboxScheduler}) os publica no Kafka posteriormente.
 * </p>
 *
 * <p>Esta entidade estende {@link BaseImmutableEntity} — uma vez criado,
 * o registro de outbox nunca deve ser alterado, exceto pelos campos de
 * controle de processamento ({@code processed}, {@code processedAt},
 * {@code retryCount}, {@code lastError}).</p>
 *
 * @see BaseImmutableEntity
 */
@Table("outbox_events")
public class OutboxEvent extends BaseImmutableEntity {

    /**
     * Construtor padrão necessário para o Spring Data R2DBC.
     */
    public OutboxEvent() {
    }

    /**
     * Construtor completo para criação de um evento de outbox.
     *
     * @param aggregateId  ID do aggregate que originou o evento
     * @param eventType    nome da classe do evento de domínio
     * @param payload      payload JSON serializado
     * @param topic        tópico Kafka de destino
     * @param partitionKey chave de partição Kafka
     */
    public OutboxEvent(UUID aggregateId, String eventType, String payload,
                       String topic, String partitionKey) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.createdAt = LocalDateTime.now(); // Garante a data de criação
        this.processed = false;
        this.retryCount = 0;
    }

    @Column("aggregate_id")
    private UUID aggregateId;

    @Column("event_type")
    private String eventType;

    @Column("payload")
    private String payload;

    @Column("topic")
    private String topic;

    @Column("partition_key")
    private String partitionKey;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("processed")
    private boolean processed;

    @Column("processed_at")
    private LocalDateTime processedAt;

    @Column("retry_count")
    private int retryCount;

    @Column("last_error")
    private String lastError;



    // ========================================================================
    // MÉTODOS DE NEGÓCIO PÚBLICOS
    // (Isto resolve os erros "Cannot resolve method" no OutboxScheduler)
    // ========================================================================

    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.retryCount += 1;
        this.lastError = errorMessage;
    }

    public void abandon() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    // ========================================================================
    // GETTERS
    // ========================================================================

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getTopic() {
        return topic;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }
}