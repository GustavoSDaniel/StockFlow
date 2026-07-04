package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.*;

/**
 * Representa um fornecedor no sistema.
 * <p>
 * Esta entidade estende {@link BaseEntity} e herda os campos de auditoria e controle de versão.
 * Um fornecedor possui informações cadastrais como nome, CNPJ, nome fantasia, site, valor mínimo de pedido,
 * anotações internas, além de listas de {@link SupplierContact} (contatos) e {@link Address} (endereços).
 * </p>
 *
 * <p><strong>Relacionamentos transientes:</strong>
 * As listas de contatos e endereços são anotadas com {@code @Transient}, indicando que não são persistidas
 * diretamente nesta tabela. A persistência dessas associações é responsabilidade de repositórios específicos
 * ou de camada de serviço, tipicamente utilizando chaves estrangeiras {@code supplierId} nas respectivas tabelas.
 * </p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>suppliers</strong>.</p>
 *
 * @see BaseEntity
 * @see SupplierContact
 * @see Address
 */
@Table("suppliers")
public class Supplier extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o Spring Data R2DBC.
     */
    public Supplier() {
    }

    /**
     * Construtor para criação de um fornecedor com os principais dados.
     * <p>As listas de contatos e endereços recebem valores padrão (listas vazias) se {@code null} for fornecido.
     * Os campos de auditoria e ID são herdados e gerados automaticamente.</p>
     *
     * @param name          razão social do fornecedor
     * @param cnpj          CNPJ do fornecedor (formato esperado: apenas números)
     * @param tradeName     nome fantasia (pode ser igual à razão social)
     * @param website       site do fornecedor
     * @param minOrderValue valor mínimo para fechamento de pedido com este fornecedor
     * @param notes         observações internas (não visíveis ao fornecedor)
     */
    public Supplier(String name, String cnpj, String tradeName,
                    String website, BigDecimal minOrderValue, String notes) {
        this.name = name;
        this.cnpj = cnpj;
        this.tradeName = tradeName;
        this.website = website;
        this.minOrderValue = minOrderValue;
        this.notes = notes;
    }

    /**
     * Razão social do fornecedor.
     */
    private String name;

    /**
     * CNPJ do fornecedor (14 dígitos numéricos).
     */
    private String cnpj;

    /**
     * Nome fantasia do fornecedor.
     */
    private String tradeName;

    /**
     * Website do fornecedor.
     */
    private String website;

    /**
     * Valor mínimo do pedido para que o fornecedor aceite a transação.
     * Pode ser {@code null} se não houver restrição.
     */
    private BigDecimal minOrderValue;

    /**
     * Observações internas sobre o fornecedor.
     */
    private String notes;

    /**
     * Retorna a razão social do fornecedor.
     *
     * @return nome do fornecedor
     */
    public String getName() {
        return name;
    }

    /**
     * Define a razão social do fornecedor.
     *
     * @param name razão social
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retorna o CNPJ do fornecedor.
     *
     * @return CNPJ (apenas números)
     */
    public String getCnpj() {
        return cnpj;
    }

    /**
     * Define o CNPJ do fornecedor.
     *
     * @param cnpj CNPJ com 14 dígitos
     */
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    /**
     * Retorna o nome fantasia do fornecedor.
     *
     * @return nome fantasia
     */
    public String getTradeName() {
        return tradeName;
    }

    /**
     * Define o nome fantasia do fornecedor.
     *
     * @param tradeName nome fantasia
     */
    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    /**
     * Retorna o website do fornecedor.
     *
     * @return URL do site
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Define o website do fornecedor.
     *
     * @param website URL do site
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * Retorna o valor mínimo de pedido.
     *
     * @return valor mínimo, ou {@code null} se não houver restrição
     */
    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    /**
     * Define o valor mínimo de pedido.
     *
     * @param minOrderValue valor mínimo
     */
    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    /**
     * Retorna as observações internas sobre o fornecedor.
     *
     * @return notas ou {@code null}
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Define observações internas sobre o fornecedor.
     *
     * @param notes notas
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

}
