package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.CategoryResponse;
import com.gustavosdaniel.stock_flow_api.domain.mapping.CategoryMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.NameExistException;
import com.gustavosdaniel.stock_flow_api.repository.CategoryRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
    public Mono<CategoryResponse> createCategory(CategoryRequest request) {

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

    @Transactional
    public Mono<CategoryResponse> addSubCategories(UUID parentId, UUID childId) {

        validateSubCategory(parentId, childId);

        return Mono.zip(
                        categoryRepository.findById(parentId)
                                .switchIfEmpty(Mono.error(new CategoryNotFoundException())),

                        categoryRepository.findById(childId)
                                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                )
                .flatMap(tuple -> {

                    Category parent = tuple.getT1();
                    Category child = tuple.getT2();

                    if (!child.isRootCategory())
                        return Mono.error(new BusinessRuleException(
                                "Categoria já possui uma categoria pai"
                        ));

                    parent.addSubCategory(child);

                    return categoryRepository.save(child);
                })
                .doFirst(() -> log.info("Adicionando subCategoria: {} na categoria {}",
                        parentId, childId))
                .doOnNext(savedChild -> log.info("Categoria {} vinculada com sucesso como subcategoria."
                        , savedChild.getName()))
                .map(categoryMapper::toCategoryResponse);

    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllCategories(Pageable pageable) {

        return PageUtils.toPage(
                        categoryRepository.findAllBy(pageable),
                        categoryRepository.count(),
                        categoryMapper::toCategoryResponse,
                        pageable
                ).doFirst(() -> log.info("Buscando todas as categorias"))
                .doOnSuccess(page ->
                        log.info("Total de categorias encontradas foram de {}", page.getTotalElements()));

    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllActiveCategories(Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.findByActiveTrue(pageable),
                        categoryRepository.countByActiveTrue(),
                        categoryMapper::toCategoryResponse,
                        pageable
                )
                .doFirst(() -> log.info("Buscando todas as categorias ativas"))
                .doOnSuccess(page ->
                        log.info("Total de categorias ativas encontradas nesta página: {}",
                                page.getNumberOfElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> searchCategories(String name, Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.searchByName(name, pageable),
                        categoryRepository.countByName(name),
                        categoryMapper::toCategoryResponse,
                        pageable

                )
                .doFirst(() -> log.info("Buscando categorias que contenham o nome: '{}'", name))
                .doOnNext(page ->
                        log.info("Total de categorias encontradas para '{}': {}",
                                name, page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> searchActiveCategories(String name, Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.searchActiveByName(name, pageable),
                        categoryRepository.countActiveByName(name),
                        categoryMapper::toCategoryResponse,
                        pageable

                )
                .doFirst(() -> log.info("Buscando categorias ativas pelo nome {}",
                        name))
                .doOnNext(page ->
                        log.info("Total de categorias ativas encontradas para '{}': {}",
                                name, page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllSubCategories(UUID parentId, Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.findByParentId(parentId, pageable),
                        categoryRepository.countByParentId(parentId),
                        categoryMapper::toCategoryResponse,
                        pageable

                )
                .doFirst(() -> log.info("Buscando todas as subcategorias da categoria: {}", parentId))
                .doOnNext(page ->
                        log.info("Total de {} subcategorias encontradas para a categoria: {}",
                                page.getTotalElements(), parentId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllActiveSubCategories(UUID parentId, Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.findByParentIdAndActiveTrue(parentId, pageable),
                        categoryRepository.countByParentIdAndActiveTrue(parentId),
                        categoryMapper::toCategoryResponse,
                        pageable
                )
                .doFirst(() -> log.info("Buscando todas as subcategorias da categoria: {}",
                        parentId))
                .doOnNext(page ->
                        log.info("Total de {} subcategorias ativas encontradas para a categoria: {}",
                                page.getTotalElements(), parentId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllDisabledSubCategories(UUID parentId, Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.findByParentIdAndActiveFalse(parentId, pageable),
                        categoryRepository.countByParentIdAndActiveFalse(parentId),
                        categoryMapper::toCategoryResponse,
                        pageable
                )
                .doFirst(() -> log.info("Buscando todas as subcategorias desativadas da categoria: {}",
                        parentId))
                .doOnNext(page ->
                        log.info("Total de {} subcategorias desativadas encontradas para a categoria: {}",
                                page.getTotalElements(), parentId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<CategoryResponse>> findAllDisabledCategories(Pageable pageable) {

        return PageUtils.toPage(

                        categoryRepository.findByActiveFalse(pageable),
                        categoryRepository.countByActiveFalse(),
                        categoryMapper::toCategoryResponse,
                        pageable
                )
                .doFirst(() -> log.info("Buscando todas as categorias que se encontra desativadas"))
                .doOnNext(page ->
                        log.info("Todas as categorias desativadas encontradas: {}",
                                page.getTotalElements()));
    }

    @Transactional
    public Mono<CategoryResponse> updateCategory(UUID categoryId, CategoryUpdateRequest request) {

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
    public Mono<Void> activeCategory(UUID categoryId) {

        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .flatMap(category -> {
                    if (category.isActive()) return Mono.error(
                            new BusinessRuleException("Categoria já está ativa"));
                    category.setActive(true);
                    return categoryRepository.save(category);
                })
                .doFirst(() -> log.info("Ativando categoria {} que se encontra desativada",
                        categoryId))
                .doOnSuccess(v -> log.info("Categoria ativada com sucesso"))
                .then();
    }

    @Transactional
    public Mono<Void> removeSubCategories(UUID parentId, UUID childId) {

        validateSubCategory(parentId, childId);

        return Mono.zip(

                        categoryRepository.findById(parentId)
                                .switchIfEmpty(Mono.error(new CategoryNotFoundException())),

                        categoryRepository.findById(childId)
                                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))

                )
                .flatMap(tuple -> {

                    Category parent = tuple.getT1();

                    Category child = tuple.getT2();

                    if (child.isRootCategory())
                        return Mono.error(new BusinessRuleException(
                                "Categoria não possui categoria pai para ser removida"
                        ));

                    if (!parent.getId().equals(child.getParentId()))
                        return Mono.error(new BusinessRuleException(
                                "A categoria informada não é pai desta subcategoria"
                        ));

                    parent.removeSubCategory(child);

                    return categoryRepository.save(child);
                })
                .doFirst(() -> log.info("Removendo subcategoria {}, da categoria {}",
                        childId, parentId))
                .doOnSuccess(categoryRemoved -> log.info("Categoria, removida com sucesso"))
                .then();

    }

    @Transactional
    public Mono<Void> disableCategory(UUID categoryId) {

        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .filter(Category::isActive)
                .switchIfEmpty(Mono.error(new BusinessRuleException("Categoria já está desativada")))
                .flatMap(category -> {

                    category.setActive(false);

                    return categoryRepository.save(category);
                })
                .doFirst(() -> log.info("Iniciando processo para desativa a categoria {}", categoryId))
                .doOnSuccess(savedCategory -> log.info("Categoria {} desativada com sucesso",
                        savedCategory.getName()))
                .then();
    }

    @Transactional
    public Mono<Void> deleteCategory(UUID categoryId) {

        return categoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()))
                .flatMap(category -> {

                    if (category.isRootCategory()) {
                        log.warn("Deletando categoria raiz: {} subcategorias " +
                                        "serão desvinculadas (ON DELETE SET NULL)",
                                category.getName());
                    }

                    return categoryRepository.delete(category);
                })
                .doFirst(() -> log.warn("Iniciando processo para deletar a categoria {}",
                        categoryId))
                .doOnSuccess(v -> log.info("Categoria deletada com sucesso"))
                .then();
    }

    private Mono<Category> validateNameDuplication(Category existingCategory, String requestName) {

        if (requestName == null || requestName.isBlank() ||
                requestName.equalsIgnoreCase(existingCategory.getName()))

            return Mono.just(existingCategory);

        return categoryRepository.existsByNameIgnoreCase(requestName)
                .flatMap(existName -> {
                    if (existName) return Mono.error(new NameExistException());

                    return Mono.just(existingCategory);

                });
    }

    private void validateSubCategory(UUID parentId, UUID childId) {


        if (parentId == null || childId == null)
            throw new BusinessRuleException(
                    "ParentId e ChildId não podem ser nulos"
            );

        if (parentId.equals(childId)) throw new BusinessRuleException(
                "Uma categoria não pode ser subcategoria de si mesma");
    }

}
