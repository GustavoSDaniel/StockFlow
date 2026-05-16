package com.gustavosdaniel.stock_flow_api.domain.mapping;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toCategory(CategoryRequest request){

        if (request == null) return null;

        return new Category(

                request.name(),
                request.description(),
                request.parentId(),
                true
        );
    }

    public CategoryResponse toCategoryResponse(Category category){

        if (category == null) return null;

        return new CategoryResponse(

                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                category.isActive()
        );
    }

    public void updateCategory(Category category, CategoryUpdateRequest request){

        if (request.name() != null && !request.name().isBlank()) category.setName(request.name());


        if (request.description() != null && !request.description().isBlank())
            category.setDescription(request.description());
    }
}
