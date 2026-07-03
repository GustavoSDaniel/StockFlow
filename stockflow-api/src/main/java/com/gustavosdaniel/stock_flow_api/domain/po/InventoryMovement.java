package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.messaging.event.*;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Representa um movimento de estoque (entrada, saída, ajuste, etc.) de forma imutável.
 * <p>
 * Esta entidade estende {@link BaseImmutableEntity}, portanto não possui campos de versão
 * nem de última modificação – uma vez persistida, a movimentação não pode ser alterada.
 * É utilizada para auditoria, rastreabilidade e histórico de todas as alterações nas quantidades
 * de produtos em estoque.
 * </p>
 *
 * <p><strong>Informações armazenadas:</strong>
 * <ul>
 *   <li>Produto e estoque afetados ({@code productId}, {@code stockId})</li>
 *   <li>Tipo de movimento ({@link MovementType}) e motivo ({@link MovementReason})</li>
 *   <li>Quantidade movimentada, quantidade antes e depois da movimentação</li>
 *   <li>Custo unitário no momento da movimentação ({@code unitCost})</li>
 *   <li>Referências a fornecedor ou cliente (quando aplicável)</li>
 *   <li>Nota opcional e número de referência (ex.: nota fiscal, pedido)</li>
 * </ul>
 * </p>
 *

 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>inventory_movement</strong>.
 * Os mapeamentos de colunas são explícitos via {@code @Column}.</p>
 *
 * @see BaseImmutableEntity
 * @see MovementType
 * @see MovementReason
 * @see Stock
 * @see DomainEvent
 */
@Table("inventory_movement")
public class InventoryMovement extends BaseImmutableEntity {

    /**
     * Construtor completo para criação de uma movimentação de estoque.
     *
     * @param productId        identificador do produto movimentado
     * @param stockId          identificador do registro de estoque (tabela {@code Stock})
     * @param movementType     tipo de movimentação (entrada, saída, etc.)
     * @param quantity         quantidade movimentada (valor positivo)
     * @param quantityBefore   quantidade em estoque antes da movimentação
     * @param quantityAfter    quantidade em estoque após a movimentação
     * @param reason           motivo da movimentação (compra, venda, ajuste, etc.)
     * @param referenceNumber  número de referência (pedido, NF, etc.)
     * @param supplierId       fornecedor envolvido (em compras/devoluções)
     * @param customerId       cliente envolvido (em vendas)
     * @param note             observação opcional
     * @param unitCost         custo unitário do produto na data da movimentação
     */
    public InventoryMovement(UUID productId, UUID stockId, MovementType movementType,
                             Integer quantity, Integer quantityBefore, Integer quantityAfter,
                             MovementReason reason, String referenceNumber, UUID supplierId,
                             UUID customerId, String note, BigDecimal unitCost) {
        this.productId = productId;
        this.stockId = stockId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.reason = reason;
        this.referenceNumber = referenceNumber;
        this.supplierId = supplierId;
        this.customerId = customerId;
        this.note = note;
        this.unitCost = unitCost;
    }

    public static InventoryMovement createTransfer(UUID productId, UUID stockId, MovementType movementType,
    Integer quantity, Integer quantityBefore, Integer quantityAfter, String referenceNumber, String note){

        return new InventoryMovement(productId, stockId, movementType, quantity, quantityBefore, quantityAfter,
                MovementReason.TRANSFER, referenceNumber, null, null, note, null);
    }


    /**
     * Identificador do produto afetado.
     * Mapeado para a coluna {@code product_id}.
     */
    @Column("product_id")
    private UUID productId;

    /**
     * Identificador do registro de estoque (tabela {@code Stock}).
     * Mapeado para a coluna {@code stock_id}.
     */
    @Column("stock_id")
    private UUID stockId;

    /**
     * Tipo da movimentação (entrada, saída, ajuste etc.).
     * Mapeado para a coluna {@code movement_type}.
     */
    @Column("movement_type")
    private MovementType movementType;

    /**
     * Quantidade movimentada (positiva).
     */
    private Integer quantity;

    /**
     * Quantidade em estoque imediatamente antes do movimento.
     * Mapeado para a coluna {@code quantity_before}.
     */
    @Column("quantity_before")
    private Integer quantityBefore;

    /**
     * Quantidade em estoque imediatamente após o movimento.
     * Mapeado para a coluna {@code quantity_after}.
     */
    @Column("quantity_after")
    private Integer quantityAfter;

    /**
     * Motivo da movimentação (compra, devolução, perda, etc.).
     */
    private MovementReason reason;

    /**
     * Número de referência externa (nota fiscal, pedido de compra, pedido de venda).
     * Mapeado para a coluna {@code reference_number}.
     */
    @Column("reference_number")
    private String referenceNumber;

    /**
     * Identificador do fornecedor (em movimentações de entrada ou devolução a fornecedor).
     * Mapeado para a coluna {@code supplier_id}.
     */
    @Column("supplier_id")
    private UUID supplierId;

    /**
     * Identificador do cliente (em movimentações de saída ou devolução de cliente).
     * Mapeado para a coluna {@code customer_id}.
     */
    @Column("customer_id")
    private UUID customerId;

    /**
     * Observação textual opcional sobre a movimentação.
     */
    private String note;

    /**
     * Custo unitário do produto na data da movimentação (útil para avaliação de estoque).
     * Mapeado para a coluna {@code unit_cost}.
     */
    @Column("unit_cost")
    private BigDecimal unitCost;

    /**
     * Lista de eventos de domínio gerados por esta movimentação (ex.: estoque baixo).
     * Anotada com {@code @Transient} – não é persistida, apenas usada em memória
     * para comunicação entre camadas (padrão Domain Events).
     */
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public void evaluateAndRegisterAlerts(Stock stock){
        if (stock == null) return;

        NotificationType notificationType = switch (stock.getStockStatus()){

            case OUT_OF_STOCK -> NotificationType.OUT_OF_STOCK;
            case LOW -> NotificationType.STOCK_LOW;
            case REORDER_POINT -> NotificationType.REORDER_POINT;
            case OVER_STOCKED -> NotificationType.OVERSTOCK;

            case NORMAL -> null;
        };

        if (notificationType != null) {
            this.domainEvents.add(new InventoryAlertEvent(
                    this.productId,
                    notificationType,
                    stock.getCurrentQuantity(),
                    stock.getMinimumQuantity(),
                    stock.getMaximumQuantity(),
                    stock.getReorderPoint()
            ));
        }
    }


    /**
     * Limpa todos os eventos de domínio registrados.
     * Normalmente chamado após os eventos terem sido publicados.
     */
    public void clearDomainEvent() {
        domainEvents.clear();
    }


    // Nota: não há setters públicos, pois a classe é imutável.

    /**
     * Identificador do produto.
     *
     * @return UUID do produto
     */
    public UUID getProductId() {
        return productId;
    }

    /**
     * Identificador do estoque associado.
     *
     * @return UUID do registro {@code Stock}
     */
    public UUID getStockId() {
        return stockId;
    }

    /**
     * Tipo de movimentação.
     *
     * @return {@link MovementType}
     */
    public MovementType getMovementType() {
        return movementType;
    }

    /**
     * Quantidade movimentada (sempre positiva).
     *
     * @return quantidade movimentada
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Quantidade antes da movimentação.
     *
     * @return quantidade anterior
     */
    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    /**
     * Quantidade após a movimentação.
     *
     * @return quantidade posterior
     */
    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    /**
     * Motivo da movimentação.
     *
     * @return {@link MovementReason}
     */
    public MovementReason getReason() {
        return reason;
    }

    /**
     * Número de referência (pedido, NF, etc.).
     *
     * @return referência externa
     */
    public String getReferenceNumber() {
        return referenceNumber;
    }

    /**
     * Identificador do fornecedor relacionado (se houver).
     *
     * @return UUID do fornecedor ou {@code null}
     */
    public UUID getSupplierId() {
        return supplierId;
    }

    /**
     * Identificador do cliente relacionado (se houver).
     *
     * @return UUID do cliente ou {@code null}
     */
    public UUID getCustomerId() {
        return customerId;
    }

    /**
     * Observação da movimentação.
     *
     * @return nota textual ou {@code null}
     */
    public String getNote() {
        return note;
    }

    /**
     * Custo unitário no momento da movimentação.
     *
     * @return custo unitário ou {@code null}
     */
    public BigDecimal getUnitCost() {
        return unitCost;
    }

    /**
     * Retorna uma visão imutável da lista de eventos de domínio registrados.
     *
     * @return lista não modificável de {@link DomainEvent}
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}