package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.CategoryOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.stock_flow_api.service.CategoryService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController implements  CategoryOpenApi{

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping()
    public Mono<ResponseEntity<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request){

        return categoryService.createCategory(request)
                .map(response -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(response));
    }

    @PutMapping("/{parentId}/subcategory/{childId}")
    public Mono<ResponseEntity<CategoryResponse>> addSubcategory(
            @PathVariable UUID parentId, @PathVariable UUID childId){

        return categoryService.addSubCategories(parentId, childId).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<Page<CategoryResponse>>> allCategories(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return categoryService.findAllCategories(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Page<CategoryResponse>>> searchCategories(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return categoryService.searchCategories(name, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/search-active")
    public Mono<ResponseEntity<Page<CategoryResponse>>> searchActiveCategories(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return categoryService.searchActiveCategories(name, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/{parentId}/all-subcategories")
    public Mono<ResponseEntity<Page<CategoryResponse>>> allSubcategoriesCategories(

            @PathVariable UUID parentId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return categoryService.findAllSubCategories(parentId, pageable).map(ResponseEntity::ok);

    }

    @GetMapping("/{parentId}/all-active-subcategories")
    public Mono<ResponseEntity<Page<CategoryResponse>>> allActiveSubcategories(

            @PathVariable UUID parentId,
            @ParameterObject
            @PageableDefault(size = 20,sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return categoryService.findAllActiveSubCategories(parentId, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/{parentId}/all-disable-subcategories")
    public Mono<ResponseEntity<Page<CategoryResponse>>> allDisableSubcategories(
            @PathVariable UUID parentId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return categoryService.findAllDisabledSubCategories(parentId, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/all-disable-categories")
    public Mono<ResponseEntity<Page<CategoryResponse>>> allDisableCategories(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return categoryService.findAllDisabledCategories(pageable).map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<CategoryResponse>> updateCategory(

            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request){

        return categoryService.updateCategory(id, request).map(ResponseEntity::ok);

    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseEntity<Void>> activeCategory(@PathVariable UUID id){

        return categoryService.activeCategory(id).thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{parentId}/remove-subcategory/{childId}")
    public Mono<ResponseEntity<Void>> removeCategory(
            @PathVariable UUID parentId, @PathVariable UUID childId
    ){
        return categoryService.removeSubCategories(parentId, childId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/disable")
    public Mono<ResponseEntity<Void>> disableCategory(@PathVariable UUID id){

        return categoryService.disableCategory(id).thenReturn(ResponseEntity.noContent().build())
;    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteCategory(@PathVariable UUID id){

        return categoryService.deleteCategory(id).thenReturn(ResponseEntity.noContent().build());
    }

}
