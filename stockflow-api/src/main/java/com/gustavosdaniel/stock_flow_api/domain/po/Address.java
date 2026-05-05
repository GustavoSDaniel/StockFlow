package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representa um endereço associado a um fornecedor no sistema.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria e controle de versão.
 * Armazena informações detalhadas de localização, como logradouro, número, complemento, bairro, cidade,
 * estado (via enum {@link StateUF}), CEP, país, além de um rótulo para identificar o endereço
 * (ex.: "Matriz", "Filial Sul") e um indicador de endereço principal.
 * </p>
 *
 * <p><strong>Regras de negócio:</strong>
 * <ul>
 *   <li>Cada endereço pertence a um fornecedor (campo {@code supplierId}).</li>
 *   <li>O flag {@code isMain} indica se este é o endereço principal do fornecedor.
 *   Normalmente, apenas um endereço por fornecedor deve ser marcado como principal.</li>
 *   <li>O campo {@code active} permite desativação lógica (endereços inativos não são exibidos em interfaces padrão).</li>
 * </ul>
 * </p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>addresses</strong>.
 * O mapeamento das colunas utiliza a anotação {@code @Column} quando o nome da coluna difere do padrão.</p>
 *
 * @see BaseEntity
 * @see StateUF
 * @see Supplier
 */
@Table("addresses")
public class Address extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o JPA.
     */
    public Address() {
    }

    /**
     * Construtor para criação de um endereço com todos os atributos necessários.
     *
     * @param supplierId    identificador do fornecedor associado
     * @param label         rótulo descritivo (ex.: "Matriz", "Depósito")
     * @param street        nome da rua ou logradouro
     * @param streetNumber  número do imóvel (pode conter sufixos como "S/N", "Bloco A")
     * @param complement    complemento (apto, sala, fundos, etc.)
     * @param neighborhood  bairro
     * @param city          cidade
     * @param zipCode       CEP (código postal)
     * @param stateUF       estado (enum {@link StateUF})
     * @param country       país (ex.: "Brasil")
     * @param isMain        {@code true} se este for o endereço principal do fornecedor
     */
    public Address(UUID supplierId, String label, String street, String streetNumber,
                   String complement, String neighborhood, String city, String zipCode,
                   StateUF stateUF, String country, boolean isMain) {
        this.supplierId = supplierId;
        this.label = label;
        this.street = street;
        this.streetNumber = streetNumber;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.zipCode = zipCode;
        this.stateUF = stateUF;
        this.country = country;
        this.isMain = isMain;
    }

    // ======================== ATRIBUTOS ========================

    /**
     * Identificador do fornecedor ao qual este endereço pertence.
     * Mapeado para a coluna {@code supplier_id}.
     */
    @Column("supplier_id")
    private UUID supplierId;

    /**
     * Rótulo descritivo do endereço (ex.: "Casa", "Escritório Central", "Filial Sul").
     */
    private String label;

    /**
     * Nome do logradouro (rua, avenida, estrada, etc.).
     */
    private String street;

    /**
     * Número do imóvel. Pode conter texto (ex.: "S/N", "Km 10").
     * Mapeado para a coluna {@code street_number}.
     */
    @Column("street_number")
    private String streetNumber;

    /**
     * Complemento (apartamento, sala, bloco, lote, fundos, etc.).
     */
    private String complement;

    /**
     * Bairro ou região.
     */
    private String neighborhood;

    /**
     * Nome da cidade.
     */
    private String city;

    /**
     * Código postal (CEP). Mapeado para a coluna {@code zip_code}.
     */
    @Column("zip_code")
    private String zipCode;

    /**
     * Unidade federativa (estado) representada pelo enum {@link StateUF}.
     * Mapeado para a coluna {@code state}.
     */
    @Column("state")
    private StateUF stateUF;

    /**
     * País (ex.: "Brasil", "Estados Unidos").
     */
    private String country;

    /**
     * Indica se este é o endereço principal do fornecedor.
     * Mapeado para a coluna {@code is_main}.
     */
    @Column("is_main")
    private boolean isMain;

    /**
     * Indica se o endereço está ativo. Endereços inativos são ignorados na maioria das consultas.
     * Padrão: {@code true}.
     */
    private boolean active = true;

    // ======================== GETTERS E SETTERS ========================

    public UUID getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public StateUF getStateUF() {
        return stateUF;
    }

    public void setStateUF(StateUF stateUF) {
        this.stateUF = stateUF;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Verifica se este é o endereço principal do fornecedor.
     *
     * @return {@code true} se for o endereço principal; {@code false} caso contrário
     */
    public boolean isMain() {
        return isMain;
    }

    /**
     * Define se este endereço é o principal do fornecedor.
     *
     * @param isMain {@code true} para marcar como principal; {@code false} caso contrário
     */
    public void setMain(boolean isMain) {
        this.isMain = isMain;
    }

    /**
     * Verifica se o endereço está ativo.
     *
     * @return {@code true} se ativo; {@code false} se inativo
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Define o status de atividade do endereço.
     *
     * @param active {@code true} para ativo; {@code false} para inativo
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}