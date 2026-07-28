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

/**
 * Service responsible for category CRUD operations, subcategory management,
 * and activation/deactivation lifecycle.
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(CategoryMapper categoryMapper, CategoryRepository categoryRepository) {
        this.categoryMapper = categoryMapper;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates a new category if its name does not already exist.
     *
     * @param request the category creation payload
     * @return a Mono emitting the created category response
     * @throws NameExistException if a category with the same name (case-insensitive) already exists
     */
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

    /**
     * Links a child category as a subcategory of the specified parent.
     *
     * @param parentId the ID of the parent category
     * @param childId  the ID of the category to become a subcategory
     * @return a Mono emitting the updated child category response
     * @throws CategoryNotFoundException if either category does not exist
     * @throws BusinessRuleException if the child already belongs to a parent, or if parent and child are the same
     */
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
                        childId, parentId))
                .doOnNext(savedChild -> log.info("Categoria {} vinculada com sucesso como subcategoria."
                        , savedChild.getName()))
                .map(categoryMapper::toCategoryResponse);

    }

    /**
     * Retrieves a paginated list of all categories.
     *
     * @param pageable pagination information
     * @return a Mono emitting a page of category responses
     */
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

    /**
     * Retrieves a paginated list of only active categories.
     *
     * @param pageable pagination information
     * @return a Mono emitting a page of active category responses
     */
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

    /**
     * Searches categories whose name contains the given string (case-insensitive).
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return a Mono emitting a page of matching category responses
     */
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

    /**
     * Searches only active categories by name.
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return a Mono emitting a page of matching active category responses
     */
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

    /**
     * Retrieves a paginated list of subcategories for a given parent category.
     *
     * @param parentId the parent category ID
     * @param pageable pagination information
     * @return a Mono emitting a page of subcategory responses
     */
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

    /**
     * Retrieves a paginated list of only active subcategories for a given parent.
     *
     * @param parentId the parent category ID
     * @param pageable pagination information
     * @return a Mono emitting a page of active subcategory responses
     */
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

    /**
     * Retrieves a paginated list of only disabled/inactive subcategories for a given parent.
     *
     * @param parentId the parent category ID
     * @param pageable pagination information
     * @return a Mono emitting a page of disabled subcategory responses
     */
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

    /**
     * Retrieves a paginated list of all disabled/inactive categories.
     *
     * @param pageable pagination information
     * @return a Mono emitting a page of disabled category responses
     */
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

    /**
     * Updates an existing category's fields. Validates for duplicate names before saving.
     *
     * @param categoryId the ID of the category to update
     * @param request    the update payload
     * @return a Mono emitting the updated category response
     * @throws CategoryNotFoundException if the category does not exist
     * @throws NameExistException         if the new name already belongs to another category
     */
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

    /**
     * Activates a previously deactivated category.
     *
     * @param categoryId the ID of the category to activate
     * @return a Mono that completes when the operation is done
     * @throws CategoryNotFoundException if the category does not exist
     * @throws BusinessRuleException if the category is already active
     */
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

    /**
     * Unlinks a child category from its parent, making it a root category again.
     *
     * @param parentId the parent category ID
     * @param childId  the child category ID to unlink
     * @return a Mono that completes when the operation is done
     * @throws CategoryNotFoundException if either category does not exist
     * @throws BusinessRuleException if the child is already a root category or if parent/child IDs are invalid
     */
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

    /**
     * Soft-deactivates a category by setting its active flag to false.
     *
     * @param categoryId the ID of the category to disable
     * @return a Mono that completes when the operation is done
     * @throws CategoryNotFoundException if the category does not exist
     * @throws BusinessRuleException if the category is already disabled
     */
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

    /**
     * Permanently deletes a category. If it is a root category, its subcategories are unlinked
     * (ON DELETE SET NULL).
     *
     * @param categoryId the ID of the category to delete
     * @return a Mono that completes when the operation is done
     * @throws CategoryNotFoundException if the category does not exist
     */
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
