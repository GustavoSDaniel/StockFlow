package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toProduct(ProductRequest request, String sku){

        if (request == null) return null;

        return new Product(
                request.name(),
                request.description(),
                sku,
                request.categoryId(),
                request.supplierId(),
                request.costPrice(),
                request.salePrice(),
                request.unitMeasure(),
                request.barcode()
        );
    }

    public ProductResponse toProductResponse(Product product){

        if (product == null) return null;

        return new ProductResponse(

                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSku(),
                product.getCategoryId(),
                product.getSupplierId(),
                product.getCostPrice(),
                product.getSalePrice(),
                product.getUnitMeasure(),
                product.getBarcode(),
                product.getStatus()

        );
    }

    public void toUpdateProduct(Product product, ProductUpdateRequest request){

        if (request.name() != null && request.name().isBlank()) product.setName(request.name());

        if(request.description() != null && request.description().isBlank())
            product.setDescription(request.description());

        if (request.costPrice() != null) product.setCostPrice(request.costPrice());

        if (request.salePrice() != null) product.setSalePrice(request.salePrice());

        if (request.unitMeasure() != null) product.setUnitMeasure(request.unitMeasure());

        if (request.barcode() != null && request.barcode().isBlank())
            product.setBarcode(request.barcode());
    }
}
