package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Representa um produto no catálogo da aplicação.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria, identificador UUID e versão.
 * Um produto possui informações como nome, descrição, SKU (código de estoque), código de barras,
 * preços de custo e venda, unidade de medida, além de referências a uma categoria e a um fornecedor
 * (ambas representadas por seus identificadores, sem relacionamentos JPA diretos para simplificação).
 * </p>
 *
 * <p><strong>Status do produto:</strong> o campo {@code status} (enum {@link ProductStatus}) pode ser:
 * <ul>
 *   <li>{@code ACTIVE} – produto disponível para venda (padrão)</li>
 *   <li>{@code INACTIVE} – produto temporariamente indisponível</li>
 *   <li>{@code DISCONTINUED} – produto descontinuado, não deve mais ser vendido</li>
 * </ul>
 * Métodos auxiliares {@link #activateProduct()}, {@link #inactiveProduct()} e {@link #discontinuedProduct()}
 * permitem alterar o status de forma semântica.
 * </p>
 *
 * <p><strong>Margem de lucro:</strong> o método {@link #calculateMargin()} calcula a margem percentual
 * com base no preço de venda e custo, seguindo a fórmula:
 * <br> {@code (preçoVenda - preçoCusto) / preçoVenda * 100}
 * </p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>products</strong>.
 * Os mapeamentos de colunas são explicitamente definidos com {@code @Column} onde o nome difere do padrão.</p>
 *
 * @see BaseEntity
 * @see ProductStatus
 * @see UnitMeasure
 * @see Category
 * @see Supplier
 */
@Table("products")
public class Product extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o JPA.
     */
    public Product() {
    }

    /**
     * Construtor para criação de um produto com os principais atributos.
     * O status do produto é inicializado como {@code ACTIVE} por padrão.
     *
     * @param name        nome do produto
     * @param description descrição detalhada do produto
     * @param sku         SKU (Stock Keeping Unit) – código interno de estoque
     * @param categoryId  identificador da categoria à qual o produto pertence
     * @param supplierId  identificador do fornecedor do produto
     * @param costPrice   preço de custo (sem impostos, geralmente)
     * @param salePrice   preço de venda ao consumidor
     * @param unitMeasure unidade de medida (enum {@link UnitMeasure}, ex.: KG, UN, L)
     * @param barcode     código de barras (EAN-13, UPC, etc.)
     */
    public Product(String name, String description, String sku, UUID categoryId, UUID supplierId,
                   BigDecimal costPrice, BigDecimal salePrice, UnitMeasure unitMeasure, String barcode) {
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.costPrice = costPrice;
        this.salePrice = salePrice;
        this.unitMeasure = unitMeasure;
        this.barcode = barcode;
    }

    // ======================== ATRIBUTOS ========================

    /**
     * Nome do produto (ex.: "Smartphone XYZ").
     */
    private String name;

    /**
     * Descrição detalhada do produto (características, especificações).
     */
    private String description;

    /**
     * SKU (Stock Keeping Unit) – identificador único de estoque, geralmente alfanumérico.
     */
    private String sku;

    /**
     * Identificador da categoria do produto.
     * Mapeado para a coluna {@code category_id}.
     */
    @Column("category_id")
    private UUID categoryId;

    /**
     * Identificador do fornecedor do produto.
     * Mapeado para a coluna {@code supplier_id}.
     */
    @Column("supplier_id")
    private UUID supplierId;

    /**
     * Preço de custo do produto (valor pago ao fornecedor).
     * Mapeado para a coluna {@code cost_price}.
     */
    @Column("cost_price")
    private BigDecimal costPrice;

    /**
     * Preço de venda do produto para o cliente final.
     * Mapeado para a coluna {@code sale_price}.
     */
    @Column("sale_price")
    private BigDecimal salePrice;

    /**
     * Unidade de medida do produto (quilograma, unidade, litro, etc.).
     * Mapeado para a coluna {@code unit_measure} via enum {@link UnitMeasure}.
     */
    @Column("unit_measure")
    private UnitMeasure unitMeasure;

    /**
     * Código de barras do produto (formato EAN-13, UPC, etc.).
     * Pode ser nulo se o produto não possuir código de barras.
     */
    private String barcode;

    /**
     * Status atual do produto (ACTIVE, INACTIVE, DISCONTINUED).
     * Inicializado como ACTIVE.
     */
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * Verifica se o produto está ativo (status ACTIVE).
     *
     * @return {@code true} se {@code status == ProductStatus.ACTIVE}; {@code false} caso contrário
     */
    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(this.status);
    }

    /**
     * Calcula a margem de lucro percentual do produto com base no preço de venda e no preço de custo.
     * <p>
     * Fórmula: <br>
     * {@code ((salePrice - costPrice) / salePrice) * 100}
     * </p>
     * <p>
     * Regras de tratamento:
     * <ul>
     *   <li>Se {@code costPrice} for {@code null} ou igual a zero, retorna {@link BigDecimal#ZERO}.</li>
     *   <li>Se {@code salePrice} for {@code null} ou igual a zero, retorna {@link BigDecimal#ZERO}.</li>
     *   <li>A divisão utiliza escala 4 e arredondamento {@code HALF_UP}.</li>
     *   <li>O resultado final é multiplicado por 100 para obter o percentual.</li>
     * </ul>
     *
     * @return margem percentual (ex.: 25.50 para 25,5%), ou {@code BigDecimal.ZERO} se não for possível calcular
     */
    public BigDecimal calculateMargin() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return salePrice.subtract(costPrice)
                .divide(salePrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Altera o status do produto para {@code ACTIVE} (ativo).
     */
    public void activateProduct() {
        this.status = ProductStatus.ACTIVE;
    }

    /**
     * Altera o status do produto para {@code INACTIVE} (inativo).
     * Produtos inativos não devem aparecer em catálogos ativos, mas permanecem no histórico.
     */
    public void inactiveProduct() {
        this.status = ProductStatus.INACTIVE;
    }

    /**
     * Altera o status do produto para {@code DISCONTINUED} (descontinuado).
     * Produtos descontinuados não são mais fabricados ou fornecidos.
     */
    public void discontinuedProduct() {
        this.status = ProductStatus.DISCONTINUED;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public UnitMeasure getUnitMeasure() {
        return unitMeasure;
    }

    public void setUnitMeasure(UnitMeasure unitMeasure) {
        this.unitMeasure = unitMeasure;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Retorna o status atual do produto.
     *
     * @return {@link ProductStatus} atual (ACTIVE, INACTIVE ou DISCONTINUED)
     */
    public ProductStatus getStatus() {
        return status;
    }

    // Nota: não há setter público para status; use activateProduct(), inactiveProduct() ou discontinuedProduct()
}