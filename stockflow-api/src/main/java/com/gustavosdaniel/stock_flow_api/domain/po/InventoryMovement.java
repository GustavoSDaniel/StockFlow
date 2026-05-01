package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;
import com.gustavosdaniel.stock_flow_api.messaging.event.DomainEvent;
import com.gustavosdaniel.stock_flow_api.messaging.event.StockLowEvent;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Table("inventory_movement")
public class InventoryMovement extends BaseImmutableEntity{

    public InventoryMovement(){}

    public InventoryMovement(UUID productId, UUID stockId, MovementType movementType, Integer quantity, Integer quantityBefore, Integer quantityAfter, MovementReason reason, String referenceNumber, UUID supplierId, UUID customerId, String note, BigDecimal unitCost) {
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

    @Column("product_id")
    private UUID productId;

    @Column("stock_id")
    private UUID stockId;

    @Column("movement_type")
    private MovementType movementType;

    private Integer quantity;

    @Column("quantity_before")
    private Integer quantityBefore;

    @Column("quantity_after")
    private Integer quantityAfter;

    private MovementReason reason;

    @Column("reference_number")
    private String referenceNumber;

    @Column("supplier_id")
    private UUID supplierId;

    @Column("customer_id")
    private UUID customerId;

    private String note;

    @Column("unit_cost")
    private BigDecimal unitCost;

    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();

    public void registerStockLowEvent(Stock stock){

        if (stock == null) return;
        if (stock.isLowStock()){

            domainEvents.add(new StockLowEvent(

                    this.productId,
                    stock.getCurrentQuantity(),
                    stock.getMinimumQuantity()
            ));
        }
    }

    public void clearDomainEvent(){
        domainEvents.clear();
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getStockId() {
        return stockId;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getQuantityBefore() {
        return quantityBefore;
    }

    public Integer getQuantityAfter() {
        return quantityAfter;
    }

    public MovementReason getReason() {
        return reason;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getNote() {
        return note;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

}
