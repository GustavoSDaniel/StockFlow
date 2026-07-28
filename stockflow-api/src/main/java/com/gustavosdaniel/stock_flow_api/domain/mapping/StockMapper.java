package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.InventoryMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockUpdate;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.InventoryMovementResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles manual mapping between {@link Stock} and {@link InventoryMovement}
 * persistence objects and their corresponding request/response DTOs.
 * <p>
 * Includes build methods for creating {@link Stock} from a {@link StockRequest},
 * creating {@link InventoryMovement} from an {@link InventoryMovementRequest},
 * and assembling detailed response DTOs such as {@link StockResponse},
 * {@link StockSummaryResponse}, and {@link InventoryMovementResponse}.
 * </p>
 */
@Component
public class StockMapper {

    public Stock toStock(UUID productId, StockRequest request){

        if (request == null) return null;

        return new Stock(

                productId,
                request.minimumQuantity(),
                request.maximumQuantity(),
                request.reorderPoint(),
                request.reorderQuantity(),
                request.location(),
                request.warehouseId()
        );
    }

    public StockSummaryResponse toStockSummaryResponse(Stock stock, Product product){

        if (stock == null || product == null) return null;

        return new StockSummaryResponse(

                stock.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                stock.getCurrentQuantity(),
                stock.getStockStatus(),
                stock.getWarehouseId(),
                stock.getLocation()
        );
    }

    public StockResponse toStockResponse(Stock stock, Product product){

        if (stock == null || product == null) return null;

        return new StockResponse(

                stock.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                stock.getCurrentQuantity(),
                stock.getMinimumQuantity(),
                stock.getMaximumQuantity(),
                stock.getReorderPoint(),
                stock.getReorderQuantity(),
                stock.getStockStatus(),
                stock.getLocation(),
                stock.getWarehouseId()
        );
    }

    public void applyUpdate(Stock stock, StockUpdate request){

        if (request.minimumQuantity() != null) stock.setMinimumQuantity(request.minimumQuantity());
        if (request.maximumQuantity() != null) stock.setMaximumQuantity(request.maximumQuantity());
        if (request.reorderPoint() != null) stock.setReorderPoint(request.reorderPoint());
        if (request.reorderQuantity() != null) stock.setReorderQuantity(request.reorderQuantity());
        if (request.location() != null && !request.location().isBlank()) stock.setLocation(request.location());
        if (request.warehouseId() != null && !request.warehouseId().isBlank())
            stock.setWarehouseId(request.warehouseId());
    }


    public InventoryMovement toInventoryMovement(
            InventoryMovementRequest request,
            Stock stock,
            int quantityBefore,
            int quantityAfter) {

        if (request == null || stock == null) return null;

        return new InventoryMovement(

                stock.getProductId(),
                stock.getId(),
                request.movementType(),
                request.quantity(),
                quantityBefore,
                quantityAfter,
                request.movementReason(),
                request.referenceNumber(),
                request.supplierId(),
                request.customerId(),
                request.note(),
                request.unitCost()
        );
    }

    public InventoryMovementResponse toInventoryMovementResponse(InventoryMovement inventoryMovement){

        if (inventoryMovement == null) return null;

        return new InventoryMovementResponse(

                inventoryMovement.getId(),
                inventoryMovement.getCreatedAt(),
                inventoryMovement.getMovementType(),
                inventoryMovement.getReason(),
                inventoryMovement.getQuantity(),
                inventoryMovement.getQuantityBefore(),
                inventoryMovement.getQuantityAfter(),
                inventoryMovement.getReferenceNumber(),
                inventoryMovement.getNote(),
                inventoryMovement.getSupplierId(),
                inventoryMovement.getCustomerId(),
                inventoryMovement.getUnitCost()
        );
    }

}
