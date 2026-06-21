package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
}
