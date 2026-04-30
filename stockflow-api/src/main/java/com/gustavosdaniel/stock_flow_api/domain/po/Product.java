package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Table("products")
public class Product extends BaseEntity {

    public Product(){}

    public Product(String name, String description, String sku, UUID categoryId, UUID supplierId, BigDecimal costPrice, BigDecimal salePrice, UnitMeasure unitMeasure, String barcode) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.unitMeasure = unitMeasure;
        this.barcode = barcode;
    }

    private String name;

    private String description;

    private String sku;

    @Column("category_id")
    private UUID categoryId;

    @Column("supplier_id")
    private UUID supplierId;

    @Column("cost_price")
    private BigDecimal costPrice;

    @Column("sale_price")
    private BigDecimal salePrice;

    @Column("unit_measure")
    private UnitMeasure unitMeasure;

    private String barcode;

    private ProductStatus status = ProductStatus.ACTIVE;

    public boolean isActive(){

        return ProductStatus.ACTIVE.equals(this.status);

    }

    public BigDecimal calculateMargin(){
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        return salePrice.subtract(costPrice)
                .divide(salePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public void activateProduct(){
        this.status = ProductStatus.ACTIVE;
    }

    public void inactiveProduct(){
        this.status = ProductStatus.INACTIVE;
    }

    public void discontinuedProduct(){
        this.status = ProductStatus.DISCONTINUED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public UnitMeasure getUnitMeasure() {
        return unitMeasure;
    }

    public void setUnitMeasure(UnitMeasure unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public ProductStatus getStatus() {
        return status;
    }

}
