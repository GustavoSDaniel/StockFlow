package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Representa uma categoria de produtos (ou serviços) no sistema.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria, identificador UUID e versão.
 * As categorias podem organizar produtos de forma hierárquica, onde uma categoria pode ter uma categoria pai
 * (raiz) e várias subcategorias.
 * </p>
 *
 * <p><strong>Hierarquia:</strong>
 * <ul>
 *   <li>Categorias sem {@code parentId} são consideradas categorias raiz ({@link #isRootCategory()}).</li>
 *   <li>A lista de subcategorias é mantida apenas em memória (anotada com {@code @Transient}) e não é persistida diretamente.
 *       A associação hierárquica é representada unicamente pelo campo {@code parentId}.</li>
 *   <li>Métodos utilitários {@link #addSubCategory(Category)} e {@link #removeSubCategory(Category)} ajudam a manter a
 *       consistência da estrutura em memória.</li>
 * </ul>
 * </p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>categories</strong>.</p>
 *
 * @see BaseEntity
 */
@Table("categories")
public class Category extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o JPA.
     */
    public Category() {
    }

    /**
     * Construtor para criação de uma categoria com os principais atributos.
     *
     * @param name        nome da categoria (ex.: "Eletrônicos")
     * @param description descrição detalhada da categoria
     * @param parentId    identificador da categoria pai ({@code null} se for raiz)
     * @param active      indica se a categoria está ativa (categorias inativas podem ser ocultadas em listagens)
     */
    public Category(String name, String description, UUID parentId, boolean active) {
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.active = active;
    }

    // ======================== ATRIBUTOS ========================

    /**
     * Nome da categoria (ex.: "Smartphones", "Livros").
     */
    private String name;

    /**
     * Descrição textual detalhada da categoria.
     */
    private String description;

    /**
     * Identificador da categoria pai.
     * <p>Mapeado para a coluna {@code parent_id}. Se for {@code null}, a categoria é raiz.</p>
     */
    @Column("parent_id")
    private UUID parentId;

    /**
     * Indica se a categoria está ativa. Categorias inativas não devem ser exibidas
     * na interface do usuário, mas podem permanecer no banco para histórico.
     */
    private boolean active = true;

    /**
     * Lista de subcategorias (filhas) carregadas em memória.
     * <p>Anotada com {@code @Transient} – não é persistida na tabela {@code categories}.
     * Em cenários reais, esta lista seria carregada por uma consulta específica (ex.: {@code WHERE parent_id = :id}).
     * Os métodos {@link #addSubCategory(Category)} e {@link #removeSubCategory(Category)} devem ser usados para
     * manter a consistência.</p>
     */
    @Transient
    private List<Category> subCategories = new ArrayList<>();

    // ======================== MÉTODOS DE NEGÓCIO ========================

    /**
     * Verifica se esta categoria é uma categoria raiz (não possui categoria pai).
     *
     * @return {@code true} se {@code parentId == null}; {@code false} caso contrário
     */
    public boolean isRootCategory() {
        return parentId == null;
    }

    /**
     * Adiciona uma subcategoria à lista hierárquica em memória.
     * <p>Este método define automaticamente o {@code parentId} da subcategoria para o ID desta categoria
     * e a adiciona à lista interna {@code subCategories}. Nenhuma persistência é realizada – o código
     * da aplicação deve salvar a entidade filha separadamente.</p>
     *
     * @param category subcategoria a ser adicionada; se {@code null}, o método não faz nada
     */
    public void addSubCategory(Category category) {
        if (category == null) return;
        category.setParentId(this.getId());
        this.subCategories.add(category);
    }

    /**
     * Remove uma subcategoria da lista hierárquica em memória e desassocia seu {@code parentId}.
     *
     * @param category subcategoria a ser removida; se {@code null}, o método não faz nada
     */
    public void removeSubCategory(Category category) {
        if (category == null) return;
        this.subCategories.remove(category);
        category.setParentId(null);
    }

    // ======================== GETTERS E SETTERS ========================

    /**
     * Retorna o nome da categoria.
     *
     * @return nome da categoria
     */
    public String getName() {
        return name;
    }

    /**
     * Define o nome da categoria.
     *
     * @param name novo nome
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna a descrição da categoria.
     *
     * @return descrição
     */
    public String getDescription() {
        return description;
    }

    /**
     * Define a descrição da categoria.
     *
     * @param description nova descrição
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Retorna o identificador da categoria pai.
     *
     * @return UUID do pai, ou {@code null} se for raiz
     */
    public UUID getParentId() {
        return parentId;
    }

    /**
     * Define o identificador da categoria pai.
     *
     * @param parentId UUID do pai (pode ser {@code null})
     */
    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    /**
     * Verifica se a categoria está ativa.
     *
     * @return {@code true} se ativa, {@code false} caso contrário
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Define o status de atividade da categoria.
     *
     * @param active {@code true} para ativa, {@code false} para inativa
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Retorna uma visão não modificável da lista de subcategorias carregadas em memória.
     *
     * @return lista imutável de subcategorias
     */
    public List<Category> getSubCategories() {
        return Collections.unmodifiableList(subCategories);
    }
}