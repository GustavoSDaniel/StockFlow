package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;
import com.gustavosdaniel.stock_flow_api.exception.InsuficientStockException;
import com.gustavosdaniel.stock_flow_api.exception.InvalidQuantityException;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("stocks")
public class Stock extends BaseEntity{

    public static final int DEFAULT_MIN_QUANTITY = 0;
    public static final int DEFAULT_MAX_QUANTITY = 999999999;
    public static final int DEFAULT_REORDER_POINT = 0;
    public static final int DEFAULT_REORDER_QUANTITY = 0;

    public Stock(){}

    public Stock(UUID productId, Integer minimumQuantity, Integer maximumQuantity, Integer reorderPoint, Integer reorderQuantity, String location, String warehouseId) {
        this.productId = productId;
        this.currentQuantity = 0;
        this.minimumQuantity = minimumQuantity != null ? minimumQuantity :  DEFAULT_MIN_QUANTITY;
        this.maximumQuantity = maximumQuantity != null ? maximumQuantity : DEFAULT_MAX_QUANTITY;
        this.reorderPoint = reorderPoint != null ? reorderPoint : DEFAULT_REORDER_POINT;
        this.reorderQuantity = reorderQuantity != null ? reorderQuantity : DEFAULT_REORDER_QUANTITY;
        this.location = location;
        this.warehouseId = warehouseId;
    }

    @Column("product_id")
    private UUID productId;

    @Column("current_quantity")
    private Integer currentQuantity = 0;

    @Column("minimum_quantity")
    private Integer minimumQuantity = DEFAULT_MIN_QUANTITY;

    @Column("maximum_quantity")
    private Integer maximumQuantity = DEFAULT_MAX_QUANTITY;

    @Column("reorder_point")
    private Integer reorderPoint = DEFAULT_REORDER_POINT;

    @Column("reorder_quantity")
    private Integer reorderQuantity = DEFAULT_REORDER_QUANTITY;

    private String location;

    private String warehouseId;

    public boolean isLowStock(){

        return currentQuantity <= minimumQuantity;

    }

    public boolean isCriticalStock() {

        return currentQuantity == 0;
    }

    public boolean isOverStock() {
        return currentQuantity > maximumQuantity;
    }

    public void addStock(int quantity) {

        if (quantity <= 0) throw new InvalidQuantityException();
        this.currentQuantity += quantity;

    }

    public void removeStock(int quantity){

        if (quantity <= 0) throw new InvalidQuantityException();
        if (currentQuantity < quantity) throw new InsuficientStockException();
        this.currentQuantity -= quantity;
    }

    public void adjustStock(int newQuantity){

        if (newQuantity < 0) throw new InvalidQuantityException();

        this.currentQuantity = newQuantity;
    }

    public StockStatus getStockStatus (){

        if (currentQuantity == 0) return StockStatus.OUT_OF_STOCK;

        if(isLowStock()) return StockStatus.LOW;

        if (isOverStock()) return StockStatus.OVER_STOCKED;

        return StockStatus.NORMAL;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public Integer getMaximumQuantity() {
        return maximumQuantity;
    }

    public void setMaximumQuantity(Integer maximumQuantity) {
        this.maximumQuantity = maximumQuantity;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }
}
