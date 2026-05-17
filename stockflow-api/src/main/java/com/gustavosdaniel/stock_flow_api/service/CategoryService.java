package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.stock_flow_api.domain.mapping.CategoryMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.NameExistException;
import com.gustavosdaniel.stock_flow_api.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.nio.file.LinkOption;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final Logger log = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryMapper categoryMapper, CategoryRepository categoryRepository) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Mono<CategoryResponse> createCategory(CategoryRequest request){

       return categoryRepository
               .existsByNameIgnoreCase(request.name())
               .flatMap(exists -> {

                   if (exists) return Mono.error(new NameExistException());

                   Category newCategory = categoryMapper.toCategory(request);

                   return categoryRepository.save(newCategory)
                           .doFirst(() -> log.info("Iniciando processo de criar categoria"))
                           .doOnNext(saveCategory -> log.info("Categoria: {} criada com sucesso",
                                   saveCategory.getName()));

               })

               .map(categoryMapper::toCategoryResponse);
    }

    public Mono<CategoryResponse> addSubCategories(){


    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllCategories(Pageable pageable){

        return categoryRepository.findAllBy(pageable)
                .map(categoryMapper::toCategoryResponse)
                .collectList()
                .zipWith(categoryRepository.count())
                .map(tuple -> (Page<CategoryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2())
                )
                .doFirst(() -> log.info("Buscando todas as categorias"))
                .doOnSuccess(page -> log.info("Total de categorias encontradas foram de {}",
                        page.getTotalElements()));

    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllActiveCategories(Pageable pageable){

        return categoryRepository.findByIsActiveTrue(pageable)
                .map(categoryMapper::toCategoryResponse)
                .collectList()
                .zipWith(categoryRepository.countByIsActiveTrue())
                .map(tuple -> (Page<CategoryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doFirst(() -> log.info("Buscando todas as categorias ativas"))
                .doOnSuccess(page ->
                        log.info("Total de categorias ativas encontradas nesta página: {}",
                        page.getNumberOfElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> searchCategories(String name, Pageable pageable){

        return categoryRepository.searchByName(name, pageable)
                .map(categoryMapper::toCategoryResponse)
                .collectList()
                .zipWith(categoryRepository.countByName(name))
                .map(tuple -> (Page<CategoryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doFirst(() -> log.info("Buscando categorias que contenham o nome: '{}'", name))
                .doOnNext(page -> log.info("Total de categorias encontradas para '{}': {}",
                        name, page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> searchActiveCategories(Pageable pageable, String name){

        return categoryRepository.searchActiveByName(name, pageable)
                .map(categoryMapper::toCategoryResponse)
                .collectList()
                .zipWith(categoryRepository.countActiveByName(name))
                .map(tuple -> (Page<CategoryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doFirst(() -> log.info("Buscando categorias ativas pelo nome {}",
                        name))
                .doOnNext(page -> log.info("Total de categorias ativas encontradas para '{}': {}",
                        name, page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllSubCategories(UUID parentId, Pageable pageable){

        return categoryRepository.findByParentId(parentId, pageable)
                .map(categoryMapper::toCategoryResponse)
                .collectList()
                .zipWith(categoryRepository.countByParentId(parentId))
                .map(duple -> (Page<CategoryResponse>)
                        new PageImpl<>(duple.getT1(), pageable, duple.getT2()))
                .doFirst(() -> log.info("Buscando todas as subcategorias da categoria: {}", parentId))
                .doOnNext(page -> log.info("Total de {} subcategorias encontradas para a categoria: {}",
                page.getTotalElements(), parentId));
    }

    public Mono<Page<CategoryResponse>> findAllActiveSubCategories(Pageable pageable){


    }

    public Mono<Page<CategoryResponse>> findAllDisabledSubCategories(Pageable pageable){


    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllDisabledCategories(Pageable pageable){

        return categoryRepository.findByIsActiveFalse(pageable)
                .map(categoryMapper::toCategoryResponse)
                .collectList()
                .zipWith(categoryRepository.countByIsActiveFalse())
                .map(tuple -> (Page<CategoryResponse>)
                        new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()))
                .doFirst(() -> log.info("Buscando todas as categorias que se encontra desativadas"))
                .doOnNext(page -> log.info("Todas as categorias encontradas com sucesso: {}",
                        page.getTotalElements()));
    }

    @Transactional
    public Mono<CategoryResponse> updateCategory(UUID categoryId ,CategoryUpdateRequest request){


        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .flatMap(category -> validateNameDuplication(category, request.name()))
                .flatMap(validateCategory -> {
                    categoryMapper.updateCategory(validateCategory, request);
                    return categoryRepository.save(validateCategory);
                })
                .doFirst(() -> log.info("Iniciando atualização da categoria: {}", categoryId))
                .doOnNext(save -> log.info("Categoria {} atualizada com sucesso", save.getName()))
                .map(categoryMapper::toCategoryResponse);
    }

    @Transactional
    public Mono<Void> activeCategory(UUID categoryId){

        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .flatMap(category -> {
                    if (category.isActive()) return Mono.empty();
                    category.setActive(true);
                    return categoryRepository.save(category);
                })
                .doFirst(() -> log.info("Ativando categoria {} que se encontra desativada",
                        categoryId))
                .doOnNext(v -> log.info("Categoria ativada com sucesso"))
                .then();
    }

    public Mono<CategoryResponse> removeSubCategories(){


    }

    @Transactional
    public Mono<Void> disableCategory(UUID categoryId){

        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .filter(Category::isActive)
                .flatMap(category -> {

                    category.setActive(false);

                    return categoryRepository.save(category);
                })
                .doFirst(() -> log.info("Iniciando processo para desativa a categoria {}", categoryId))
                .doOnNext(savedCategory -> log.info("Categoria {} desativada com sucesso",
                        savedCategory.getName()))
                .then();
    }

    @Transactional
    public Mono<Void> deleteCategory(UUID categoryId){

        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .flatMap(categoryRepository::delete)
                .doFirst(() -> log.warn("Iniciando processo para deletar a categoria {}",
                        categoryId))
                .doOnSuccess(v -> log.info("Categoria deletada com sucesso"))
                .then();
    }

    private Mono<Category> validateNameDuplication(Category existingCategory ,String requestName){

        if (requestName == null || requestName.isBlank() ||
                requestName.equalsIgnoreCase(existingCategory.getName()))

            return Mono.just(existingCategory);

        return categoryRepository.existsByNameIgnoreCase(requestName)
                .flatMap(existName -> {
                    if (existName) return Mono.error(new NameExistException());

                    return Mono.just(existingCategory);

                });
    }

}
