package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.stock_flow_api.domain.mapping.CategoryMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    CategoryService categoryService;


    @Test
    @DisplayName("Deve criar uma categoria com sucesso")
    void createCategory(){

        UUID categoryId = UUID.randomUUID();
        String name = "Eletronicos";
        String description = "Produtos eletronicos";

        CategoryRequest request = new CategoryRequest(name, description, null);
        Category newCategory = new Category(name, description, null, true);
        CategoryResponse response =
                new CategoryResponse(categoryId, name, description, null, true);

        when(categoryRepository.existsByNameIgnoreCase(name)).thenReturn(Mono.just(false));
        when(categoryMapper.toCategory(request)).thenReturn(newCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(Mono.just(newCategory));
        when(categoryMapper.toCategoryResponse(newCategory)).thenReturn(response);

        Mono<CategoryResponse> output = categoryService.createCategory(request);

        StepVerifier.create(output)
                .assertNext(result -> {

                    assertEquals(name, result.name(), "O nome deve ser o mesmo");
                    assertEquals(description, result.description(), "A descrição deve ser a mesma");
                    assertEquals(categoryId, result.id(), "O ID deve ser o mesmo gerado");
                })
                .verifyComplete();

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should add subcategory successfully")
    void addSubCategory(){

        UUID parentId = UUID.randomUUID();
        String name = "Eletronicos";
        String description = "Produtos eletronicos";

        UUID subCategoryId = UUID.randomUUID();
        String nameSubCategory = "Celulares";
        String descriptionSubCategory = "Celulares de todas as marcas";

        Category parent = new Category(name, description, null, true);
        Category child = new Category(nameSubCategory, descriptionSubCategory, null, true);

        CategoryResponse response = new CategoryResponse(
                subCategoryId, nameSubCategory, descriptionSubCategory, parentId, true);

        when(categoryRepository.findById(parentId)).thenReturn(Mono.just(parent));
        when(categoryRepository.findById(subCategoryId)).thenReturn(Mono.just(child));
        when(categoryRepository.save(any(Category.class))).thenReturn(Mono.just(child));
        when(categoryMapper.toCategoryResponse(child)).thenReturn(response);

        Mono<CategoryResponse> output = categoryService.addSubCategories(parentId, subCategoryId);

        StepVerifier.create(output)
                .assertNext(result -> {

                    assertEquals(subCategoryId, result.id(), "O ID deve ser o mesmo");
                    assertEquals(parentId, result.parentId(), "A categoria pai deve ser a mesma");
                })
                .verifyComplete();

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should all categories")
    void allCategories(){

        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();


        String name = "Eletronicos";
        String name1 = "Moveis";

        String description = "Eletronicos em geral";
        String description1 = "Moveis de casa";

        Pageable pageable = Pageable.unpaged();

        Category category = new Category(name, description, null, true);
        Category category2 = new Category(name1, description1, null, false);

        CategoryResponse response =
                new CategoryResponse(categoryId1, name, description, null, true );

        CategoryResponse response2 = new CategoryResponse(
                categoryId2, name1, description1, null, false);

        when(categoryRepository.findAllBy(pageable)).thenReturn(Flux.just(category, category2));
        when(categoryRepository.count()).thenReturn(Mono.just(2L));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category2)).thenReturn(response2);

        Mono<Page<CategoryResponse>> output = categoryService.findAllCategories(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {

                    assertEquals(2, page.getNumberOfElements(), "Deve conter 2 alementos");;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should all categories active")
    void allActiveCategories(){

        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        String name = "Eletronicos";
        String name1 = "Moveis";

        String description = "Eletronicos em geral";
        String description1 = "Moveis de casa";

        Pageable pageable = Pageable.unpaged();

        Category category = new Category(name, description, null, true);
        Category category2 = new Category(name1, description1, null, true);

        CategoryResponse response =
                new CategoryResponse(categoryId1, name, description, null, true );

        CategoryResponse response2 = new CategoryResponse(
                categoryId2, name1, description1, null, true);

        when(categoryRepository.findByActiveTrue(pageable)).thenReturn(Flux.just(category, category2));
        when(categoryRepository.countByActiveTrue()).thenReturn(Mono.just(2L));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category2)).thenReturn(response2);

        Mono<Page<CategoryResponse>> output = categoryService.findAllActiveCategories(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {

                    assertEquals(2, page.getNumberOfElements(), "Deve conter 2 alementos");;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should search categories by name successfully")
    void shouldSearchName(){

        Pageable pageable = Pageable.unpaged();

        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId3 = UUID.randomUUID();

        String name = "Eletronicos";
        String name2 = "Eletrica";
        String searchName = "elet";

        String description = "Eletronicos em geral";
        String description2 = "Materiais eletricos";

        Category category = new Category(name, description, null, false);
        Category category3 = new Category(name2, description2, null, true);

        CategoryResponse response =
                new CategoryResponse(categoryId1, name, description, null, false );


        CategoryResponse response3 =
                new CategoryResponse(categoryId3, name2, description2, null, true);

        when(categoryRepository.searchByName(searchName, pageable))
                .thenReturn(Flux.just(category, category3));
        when(categoryRepository.countByName(searchName)).thenReturn(Mono.just(2L));

        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category3)).thenReturn(response3);

        Mono<Page<CategoryResponse>> output = categoryService.searchCategories(searchName, pageable);

        StepVerifier.create(output)
                .assertNext(result -> {

                    assertEquals(2, result.getTotalElements(), "Deve conter 2 elementos");
                })
                .verifyComplete();

    }

    @Test
    @DisplayName("Should search active categories by name successfully")
    void shouldSearchNameActive(){

        Pageable pageable = Pageable.unpaged();

        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId3 = UUID.randomUUID();

        String name = "Eletronicos";
        String name2 = "Eletrica";
        String searchName = "elet";

        String description = "Eletronicos em geral";
        String description2 = "Materiais eletricos";

        Category category = new Category(name, description, null, true);
        Category category3 = new Category(name2, description2, null, true);

        CategoryResponse response =
                new CategoryResponse(categoryId1, name, description, null, true );


        CategoryResponse response3 =
                new CategoryResponse(categoryId3, name2, description2, null, true);

        when(categoryRepository.searchActiveByName(searchName, pageable))
                .thenReturn(Flux.just(category, category3));
        when(categoryRepository.countActiveByName(searchName)).thenReturn(Mono.just(2L));

        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category3)).thenReturn(response3);

        Mono<Page<CategoryResponse>> output = categoryService.searchActiveCategories(searchName, pageable);

        StepVerifier.create(output)
                .assertNext(result -> {

                    assertEquals(2, result.getTotalElements(), "Deve conter 2 elementos");
                })
                .verifyComplete();

    }

    @Test
    @DisplayName("Should get all subcategories successfully")
    void shouldAllSubcategories(){

        Pageable pageable = Pageable.unpaged();

        UUID parentId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();
        UUID subcategoryId2 = UUID.randomUUID();

        String name = "Eletronicos";
        String name2 = "Eletrica";

        String description = "Eletronicos em geral";
        String description2 = "Materiais eletricos";

        Category category = new Category(name, description, parentId, true);
        Category category3 = new Category(name2, description2, parentId, false);

        CategoryResponse response =
                new CategoryResponse(subcategoryId, name, description, parentId, true );


        CategoryResponse response2 =
                new CategoryResponse(subcategoryId2, name2, description2, parentId, false);

        when(categoryRepository.findByParentId(parentId, pageable))
                .thenReturn(Flux.just(category, category3));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category3)).thenReturn(response2);
        when(categoryRepository.countByParentId(parentId)).thenReturn(Mono.just(2L));

        Mono<Page<CategoryResponse>> output = categoryService.findAllSubCategories(parentId, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(2, page.getNumberOfElements());
                })
                .verifyComplete();

    }

    @Test
    @DisplayName("Should get all active subcategories successfully")
    void shouldAllActiveSubcategories(){

        Pageable pageable = Pageable.unpaged();

        UUID parentId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();
        UUID subcategoryId2 = UUID.randomUUID();

        String name = "Eletronicos";
        String name2 = "Eletrica";

        String description = "Eletronicos em geral";
        String description2 = "Materiais eletricos";

        Category category = new Category(name, description, parentId, true);
        Category category3 = new Category(name2, description2, parentId, true);

        CategoryResponse response =
                new CategoryResponse(subcategoryId, name, description, parentId, true );


        CategoryResponse response2 =
                new CategoryResponse(subcategoryId2, name2, description2, parentId, true);

        when(categoryRepository.findByParentIdAndActiveTrue(parentId, pageable))
                .thenReturn(Flux.just(category, category3));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category3)).thenReturn(response2);
        when(categoryRepository.countByParentIdAndActiveTrue(parentId)).thenReturn(Mono.just(2L));

        Mono<Page<CategoryResponse>> output = categoryService.findAllActiveSubCategories(parentId, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(2, page.getNumberOfElements());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get all disabled subcategories successfully")
    void shouldAllDisabledSubcategories(){

        Pageable pageable = Pageable.unpaged();

        UUID parentId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();
        UUID subcategoryId2 = UUID.randomUUID();

        String name = "Eletronicos";
        String name2 = "Eletrica";

        String description = "Eletronicos em geral";
        String description2 = "Materiais eletricos";

        Category category = new Category(name, description, parentId, false);
        Category category3 = new Category(name2, description2, parentId, false);

        CategoryResponse response =
                new CategoryResponse(subcategoryId, name, description, parentId, false );


        CategoryResponse response2 =
                new CategoryResponse(subcategoryId2, name2, description2, parentId, false);

        when(categoryRepository.findByParentIdAndActiveFalse(parentId, pageable))
                .thenReturn(Flux.just(category, category3));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category3)).thenReturn(response2);
        when(categoryRepository.countByParentIdAndActiveFalse(parentId)).thenReturn(Mono.just(2L));

        Mono<Page<CategoryResponse>> output = categoryService
                .findAllDisabledSubCategories(parentId, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(2, page.getNumberOfElements());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should all categories disabled")
    void allDisabledCategories(){

        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        String name = "Eletronicos";
        String name1 = "Moveis";

        String description = "Eletronicos em geral";
        String description1 = "Moveis de casa";

        Pageable pageable = Pageable.unpaged();

        Category category = new Category(name, description, null, false);
        Category category2 = new Category(name1, description1, null, false);

        CategoryResponse response =
                new CategoryResponse(categoryId1, name, description, null, false );

        CategoryResponse response2 = new CategoryResponse(
                categoryId2, name1, description1, null, false);

        when(categoryRepository.findByActiveFalse(pageable)).thenReturn(Flux.just(category, category2));
        when(categoryRepository.countByActiveFalse()).thenReturn(Mono.just(2L));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);
        when(categoryMapper.toCategoryResponse(category2)).thenReturn(response2);

        Mono<Page<CategoryResponse>> output = categoryService.findAllDisabledCategories(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {

                    assertEquals(2, page.getNumberOfElements(), "Deve conter 2 alementos");;
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategory(){

        UUID categoryId = UUID.randomUUID();

        String name = "Eletronicos";
        String nameUpdate = "Diversos eletronicos";

        String description = "Produtos eletronicos";
        String descriptionUpdate = "Diversos eletronicos atualizados";

        Category category = new Category(name, description, null, true);
        ReflectionTestUtils.setField(category,"id", categoryId);

        CategoryUpdateRequest request = new CategoryUpdateRequest(nameUpdate, descriptionUpdate);

        CategoryResponse response = new CategoryResponse(categoryId, name, description, null, true);

        when(categoryRepository.existsByNameIgnoreCase(nameUpdate)).thenReturn(Mono.just(false));
        when(categoryRepository.findById(categoryId)).thenReturn(Mono.just(category));
        doNothing().when(categoryMapper).updateCategory(any(Category.class), any(CategoryUpdateRequest.class));
        when(categoryRepository.save(category)).thenReturn(Mono.just(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(response);

        Mono<CategoryResponse> output = categoryService.updateCategory(categoryId, request);

        StepVerifier.create(output)
                .assertNext(result -> {

                    assertEquals(categoryId, result.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should activate category successfully")
    void shouldActiveCategory(){

        UUID categoryId = UUID.randomUUID();

        String name = "Eletronicos";
        String description = "Produtos eletronicos";

        Category category = new Category(name, description, null, false);
        ReflectionTestUtils.setField(category, "id", categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Mono.just(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(Mono.just(category));

        Mono<Void> output = categoryService.activeCategory(categoryId);

        StepVerifier.create(output)
                        .verifyComplete();

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(category);

        assertEquals(true, category.isActive(), "A categoria tem que está ativo");

    }

    @Test
    @DisplayName("Should remove subcategory successfully")
    void shouldRemoveSubcategory(){

        UUID categoryRootId = UUID.randomUUID();
        UUID subCategoryId = UUID.randomUUID();

        String nameRoot = "Eletronicos";
        String descriptionRoot = "Produtoos eletronicos";

        String name = "Celulares";
        String description = "Todos os celulares";

        Category categoryRoot = new Category(nameRoot, descriptionRoot, null, true);
        ReflectionTestUtils.setField(categoryRoot, "id", categoryRootId);

        Category subCategory = new Category(name, description, categoryRootId, true);

        when(categoryRepository.findById(subCategoryId)).thenReturn(Mono.just(subCategory));
        when(categoryRepository.findById(categoryRootId)).thenReturn(Mono.just(categoryRoot));
        when(categoryRepository.save(any(Category.class))).thenReturn(Mono.just(subCategory));

        Mono<Void> output = categoryService.removeSubCategories(categoryRootId, subCategoryId);

        StepVerifier.create(output).verifyComplete();

        verify(categoryRepository, times(1)).findById(subCategoryId);
        verify(categoryRepository, times(1)).findById(categoryRootId);
        verify(categoryRepository, times(1)).save(subCategory);

    }

    @Test
    @DisplayName("Should disable category successfully")
    void shouldDisableCategory(){

        UUID categoryId = UUID.randomUUID();

        String name = "Eletronicos";
        String description = "Produtos eletronicos";

        Category category = new Category(name, description, null, true);

        when(categoryRepository.findById(categoryId)).thenReturn(Mono.just(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(Mono.just(category));

        Mono<Void> output = categoryService.disableCategory(categoryId);

        StepVerifier.create(output).verifyComplete();

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(category);
    }


    @Test
    @DisplayName("Should delete category successfully")
    void shouldDeleteCategory(){

        UUID categoryId = UUID.randomUUID();

        String name = "Eletronicos";
        String description = "Produtos eletronicos";

        Category category = new Category(name, description, null, true);

        when(categoryRepository.findById(categoryId)).thenReturn(Mono.just(category));
        when(categoryRepository.delete(any(Category.class))).thenReturn(Mono.empty());

        Mono<Void> output = categoryService.deleteCategory(categoryId);

        StepVerifier.create(output).verifyComplete();

        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).delete(category);
    }
}