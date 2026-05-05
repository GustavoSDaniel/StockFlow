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
     * Construtor padrão obrigatório para o JPA.
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
     * @param contacts      lista inicial de contatos (pode ser {@code null})
     * @param website       site do fornecedor
     * @param minOrderValue valor mínimo para fechamento de pedido com este fornecedor
     * @param notes         observações internas (não visíveis ao fornecedor)
     * @param addresses     lista inicial de endereços (pode ser {@code null})
     */
    public Supplier(String name, String cnpj, String tradeName, List<SupplierContact> contacts,
                    String website, BigDecimal minOrderValue, String notes, List<Address> addresses) {
        this.name = name;
        this.cnpj = cnpj;
        this.tradeName = tradeName;
        this.contacts = contacts != null ? contacts : new ArrayList<>();
        this.website = website;
        this.minOrderValue = minOrderValue;
        this.notes = notes;
        this.addresses = addresses != null ? addresses : new ArrayList<>();
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
     * Lista de contatos associados a este fornecedor.
     * <p>Anotada com {@code @Transient} – não é persistida na tabela {@code suppliers}.
     * A associação é mantida pela chave {@code supplierId} na entidade {@link SupplierContact}.</p>
     */
    @Transient
    private List<SupplierContact> contacts = new ArrayList<>();

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
     * Lista de endereços associados a este fornecedor.
     * <p>Anotada com {@code @Transient} – a persistência é feita via chave estrangeira {@code supplierId}
     * na entidade {@link Address}.</p>
     */
    @Transient
    private List<Address> addresses = new ArrayList<>();


    /**
     * Retorna o endereço principal do fornecedor, se existir.
     * <p>Um endereço é considerado principal quando o método {@link Address#isMain()} retorna {@code true}.
     * Na ausência de um endereço principal, o {@code Optional} será vazio.</p>
     *
     * @return {@code Optional} contendo o endereço principal, ou vazio se não houver
     */
    @Transient
    public Optional<Address> getMainAddress() {
        return addresses.stream()
                .filter(Address::isMain)
                .findFirst();
    }

    /**
     * Adiciona um endereço à lista de endereços do fornecedor e define automaticamente o ID do fornecedor
     * no endereço.
     *
     * @param address endereço a ser adicionado (não pode ser {@code null})
     */
    public void addAddress(Address address) {
        address.setSupplierId(this.getId());
        this.addresses.add(address);
    }

    /**
     * Adiciona um contato à lista de contatos do fornecedor e define automaticamente o ID do fornecedor
     * no contato.
     *
     * @param contact contato a ser adicionado (não pode ser {@code null})
     */
    public void addContact(SupplierContact contact) {
        contact.setSupplierId(this.getId());
        this.contacts.add(contact);
    }

    /**
     * Remove um endereço da lista interna e desassocia o fornecedor (define {@code supplierId} como {@code null}).
     *
     * @param address endereço a ser removido; se {@code null}, o método não faz nada
     */
    public void removeAddress(Address address) {
        if (address == null) return;
        this.addresses.remove(address);
        address.setSupplierId(null);
    }

    /**
     * Remove um contato da lista interna e desassocia o fornecedor (define {@code supplierId} como {@code null}).
     *
     * @param contact contato a ser removido; se {@code null}, o método remove sem exceção
     */
    public void removeContact(SupplierContact contact) {
        this.contacts.remove(contact);
        if (contact != null) {
            contact.setSupplierId(null);
        }
    }


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
     * Retorna uma visualização não modificável da lista de contatos.
     * <p>Isso impede que a lista interna seja alterada externamente sem o uso dos métodos
     * {@link #addContact(SupplierContact)} e {@link #removeContact(SupplierContact)}.</p>
     *
     * @return lista imutável de contatos
     */
    public List<SupplierContact> getContacts() {
        return Collections.unmodifiableList(contacts);
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

    /**
     * Retorna uma visualização não modificável da lista de endereços.
     *
     * @return lista imutável de endereços
     */
    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }
}
