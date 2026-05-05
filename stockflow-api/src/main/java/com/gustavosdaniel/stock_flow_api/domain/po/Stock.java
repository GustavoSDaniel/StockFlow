package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;
import com.gustavosdaniel.stock_flow_api.exception.InsuficientStockException;
import com.gustavosdaniel.stock_flow_api.exception.InvalidQuantityException;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representa o estoque de um produto em um determinado armazém.
 * <p>
 * Esta entidade estende {@link BaseEntity} e herda os campos de auditoria e controle de versão.
 * Gerencia a quantidade atual em estoque, limites mínimo e máximo, ponto de reposição (reorder point)
 * e quantidade de reposição, além da localização física e identificador do armazém.
 * </p>
 *
 * <p><strong>Regras de negócio principais:</strong>
 * <ul>
 *   <li>A quantidade atual nunca pode ser negativa.</li>
 *   <li>A adição e remoção de estoque exigem quantidades positivas.</li>
 *   <li>A remoção não pode ultrapassar a quantidade atual ({@code currentQuantity}).</li>
 *   <li>Os limites mínimo e máximo são usados para classificar o status do estoque.</li>
 * </ul>
 * </p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>stocks</strong>.</p>
 *
 * @see BaseEntity
 * @see StockStatus
 * @see InvalidQuantityException   (exceção lançada para quantidade inválida)
 * @see InsuficientStockException  (exceção lançada quando a remoção excede o estoque atual)
 */
@Table("stocks")
public class Stock extends BaseEntity {

    /**
     * Valor padrão para o limite mínimo de estoque (0 unidades).
     * Usado quando nenhum valor é informado no construtor.
     */
    public static final int DEFAULT_MIN_QUANTITY = 0;

    /**
     * Valor padrão para o limite máximo de estoque (999.999.999 unidades).
     * Representa essencialmente "sem limite máximo".
     */
    public static final int DEFAULT_MAX_QUANTITY = 999999999;

    /**
     * Valor padrão para o ponto de reposição (0 unidades).
     * Indica que nenhum alerta de reposição será gerado por padrão.
     */
    public static final int DEFAULT_REORDER_POINT = 0;

    /**
     * Valor padrão para a quantidade a ser reposta (0 unidades).
     */
    public static final int DEFAULT_REORDER_QUANTITY = 0;

    /**
     * Construtor padrão obrigatório para o JPA.
     */
    public Stock() {
    }

    /**
     * Construtor para criar um registro de estoque com parâmetros personalizados.
     * <p>A quantidade atual é sempre inicializada com {@code 0}.
     * Caso algum parâmetro de limite/ponto de reposição seja {@code null}, o respectivo valor padrão
     * (constante {@code DEFAULT_*}) será utilizado.</p>
     *
     * @param productId        identificador do produto (não pode ser {@code null})
     * @param minimumQuantity  limite mínimo de estoque; se {@code null} usa {@link #DEFAULT_MIN_QUANTITY}
     * @param maximumQuantity  limite máximo de estoque; se {@code null} usa {@link #DEFAULT_MAX_QUANTITY}
     * @param reorderPoint     ponto de reposição; se {@code null} usa {@link #DEFAULT_REORDER_POINT}
     * @param reorderQuantity  quantidade sugerida para reposição; se {@code null} usa {@link #DEFAULT_REORDER_QUANTITY}
     * @param location         localização física dentro do armazém (ex.: "A-12", "Prateleira 3")
     * @param warehouseId      identificador do armazém ou depósito
     */
    public Stock(UUID productId, Integer minimumQuantity, Integer maximumQuantity,
                 Integer reorderPoint, Integer reorderQuantity, String location, String warehouseId) {
        this.productId = productId;
        this.currentQuantity = 0;
        this.minimumQuantity = minimumQuantity != null ? minimumQuantity : DEFAULT_MIN_QUANTITY;
        this.maximumQuantity = maximumQuantity != null ? maximumQuantity : DEFAULT_MAX_QUANTITY;
        this.reorderPoint = reorderPoint != null ? reorderPoint : DEFAULT_REORDER_POINT;
        this.reorderQuantity = reorderQuantity != null ? reorderQuantity : DEFAULT_REORDER_QUANTITY;
        this.location = location;
        this.warehouseId = warehouseId;
    }


    /**
     * Identificador do produto associado a este estoque.
     * Mapeado para a coluna {@code product_id}.
     */
    @Column("product_id")
    private UUID productId;

    /**
     * Quantidade atual disponível em estoque. Inicia em 0.
     * Mapeado para a coluna {@code current_quantity}.
     */
    @Column("current_quantity")
    private Integer currentQuantity = 0;

    /**
     * Quantidade mínima permitida em estoque. Abaixo deste valor o estoque é considerado baixo.
     * Mapeado para a coluna {@code minimum_quantity}.
     */
    @Column("minimum_quantity")
    private Integer minimumQuantity = DEFAULT_MIN_QUANTITY;

    /**
     * Quantidade máxima desejada em estoque. Acima deste valor o estoque é considerado excessivo.
     * Mapeado para a coluna {@code maximum_quantity}.
     */
    @Column("maximum_quantity")
    private Integer maximumQuantity = DEFAULT_MAX_QUANTITY;

    /**
     * Ponto de reposição: quando a quantidade atual atinge ou fica abaixo deste valor,
     * um pedido de compra pode ser recomendado.
     * Mapeado para a coluna {@code reorder_point}.
     */
    @Column("reorder_point")
    private Integer reorderPoint = DEFAULT_REORDER_POINT;

    /**
     * Quantidade de itens a serem comprados quando o ponto de reposição é atingido.
     * Mapeado para a coluna {@code reorder_quantity}.
     */
    @Column("reorder_quantity")
    private Integer reorderQuantity = DEFAULT_REORDER_QUANTITY;

    /**
     * Localização física do produto no armazém (corredor, prateleira, etc.).
     */
    private String location;

    /**
     * Identificador do armazém/depósito onde este estoque está localizado.
     */
    private String warehouseId;


    /**
     * Verifica se o estoque está baixo, ou seja, se a quantidade atual é menor ou igual
     * ao limite mínimo.
     *
     * @return {@code true} se {@code currentQuantity <= minimumQuantity}; {@code false} caso contrário
     */
    public boolean isLowStock() {
        return currentQuantity <= minimumQuantity;
    }

    /**
     * Verifica se o estoque está crítico (zerado).
     *
     * @return {@code true} se {@code currentQuantity == 0}; {@code false} caso contrário
     */
    public boolean isCriticalStock() {
        return currentQuantity == 0;
    }

    /**
     * Verifica se o estoque excede o limite máximo permitido.
     *
     * @return {@code true} se {@code currentQuantity > maximumQuantity}; {@code false} caso contrário
     */
    public boolean isOverStock() {
        return currentQuantity > maximumQuantity;
    }

    /**
     * Adiciona uma quantidade positiva ao estoque.
     *
     * @param quantity quantidade a ser adicionada (deve ser maior que zero)
     * @throws InvalidQuantityException se {@code quantity <= 0}
     */
    public void addStock(int quantity) {
        if (quantity <= 0) throw new InvalidQuantityException();
        this.currentQuantity += quantity;
    }

    /**
     * Remove uma quantidade positiva do estoque, desde que haja saldo suficiente.
     *
     * @param quantity quantidade a ser removida (deve ser maior que zero)
     * @throws InvalidQuantityException     se {@code quantity <= 0}
     * @throws InsuficientStockException    se {@code currentQuantity < quantity}
     */
    public void removeStock(int quantity) {
        if (quantity <= 0) throw new InvalidQuantityException();
        if (currentQuantity < quantity) throw new InsuficientStockException();
        this.currentQuantity -= quantity;
    }

    /**
     * Ajusta diretamente a quantidade atual do estoque para um novo valor.
     * <p>Este método não valida os limites mínimo/máximo – é uma operação bruta
     * útil para correções ou movimentações excepcionais.</p>
     *
     * @param newQuantity novo valor da quantidade atual (não pode ser negativo)
     * @throws InvalidQuantityException se {@code newQuantity < 0}
     */
    public void adjustStock(int newQuantity) {
        if (newQuantity < 0) throw new InvalidQuantityException();
        this.currentQuantity = newQuantity;
    }

    /**
     * Retorna o status atual do estoque baseado na quantidade atual e nos limites configurados.
     *
     * @return {@link StockStatus}:
     *         <ul>
     *           <li>{@code OUT_OF_STOCK} se {@code currentQuantity == 0}</li>
     *           <li>{@code LOW} se estoque baixo (menor ou igual ao mínimo)</li>
     *           <li>{@code OVER_STOCKED} se exceder o máximo</li>
     *           <li>{@code NORMAL} caso contrário</li>
     *         </ul>
     */
    public StockStatus getStockStatus() {
        if (currentQuantity == 0) return StockStatus.OUT_OF_STOCK;
        if (isLowStock()) return StockStatus.LOW;
        if (isOverStock()) return StockStatus.OVER_STOCKED;
        return StockStatus.NORMAL;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public Integer getMaximumQuantity() {
        return maximumQuantity;
    }

    public void setMaximumQuantity(Integer maximumQuantity) {
        this.maximumQuantity = maximumQuantity;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }
}