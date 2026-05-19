package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
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
                .assertNext(resultado -> {

                    assertEquals(name, resultado.name(), "O nome deve ser o mesmo");
                    assertEquals(description, resultado.description(), "A descrição deve ser a mesma");
                    assertEquals(categoryId, resultado.id(), "O ID deve ser o mesmo gerado");
                })
                .verifyComplete();

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should add subcategory with sucesso")
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
                .assertNext(resultado -> {

                    assertEquals(subCategoryId, resultado.id(), "O ID deve ser o mesmo");
                    assertEquals(parentId, resultado.parentId(), "A categoria pai deve ser a mesma");
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
        Category category2 = new Category(name1, description1, null, true);

        CategoryResponse response =
                new CategoryResponse(categoryId1, name, description, null, true );

        CategoryResponse response2 = new CategoryResponse(
                categoryId2, name1, description1, null, true);

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

}