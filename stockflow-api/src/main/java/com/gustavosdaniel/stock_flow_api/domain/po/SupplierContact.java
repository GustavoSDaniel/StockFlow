package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;


/**
 * Representa um contato associado a um fornecedor no sistema.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria padrão:
 * <ul>
 *   <li>{@code id} (UUID único)</li>
 *   <li>{@code version} (controle de concorrência otimista)</li>
 *   <li>{@code createdAt} / {@code updatedAt} (datas de criação e modificação)</li>
 *   <li>{@code createdBy} / {@code updatedBy} (usuários responsáveis)</li>
 * </ul>
 * </p>
 *
 * <p>Um contato de fornecedor contém informações como nome, e-mail, telefone e um indicador
 * de atividade. Cada contato pertence a um fornecedor específico, identificado pelo campo
 * {@code supplierId}.</p>
 *
 * <p>A anotação {@code @Table} define o nome da tabela no banco de dados como
 * <strong>supplier_contact</strong>. Os campos são mapeados com {@code @Column} quando o nome
 * da coluna difere do padrão (camelCase para snake_case).</p>
 *
 * @see BaseEntity
 * @see Supplier  (exemplo: entidade que representa o fornecedor)
 */
@Table("supplier_contact")
public class SupplierContact extends BaseEntity {

    /**
     * Construtor padrão (obrigatório para o JPA).
     */
    public SupplierContact() {
    }

    /**
     * Construtor para criação rápida de um contato sem a necessidade de setters.
     * <p>Os campos de auditoria (herdados) e o {@code supplierId} não são definidos
     * neste construtor – devem ser preenchidos posteriormente.</p>
     *
     * @param contactName nome do contato (ex.: "João Silva")
     * @param email       endereço de e-mail do contato
     * @param phoneNumber número de telefone do contato
     */
    public SupplierContact(String contactName, String email, String phoneNumber) {
        this.contactName = contactName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Identificador do fornecedor ao qual este contato pertence.
     * <p>Mapeado para a coluna {@code supplier_id} na tabela.</p>
     * <p>Este campo não é uma relação JPA (como {@code @ManyToOne}) por simplicidade;
     * a integridade referencial pode ser mantida em nível de banco de dados.</p>
     */
    @Column("supplier_id")
    private UUID supplierId;

    /**
     * Nome completo do contato.
     * <p>Mapeado para a coluna {@code contact_name}.</p>
     */
    @Column("contact_name")
    private String contactName;

    /**
     * Endereço de e-mail do contato. Mapeado para a coluna de mesmo nome.
     */
    private String email;

    /**
     * Número de telefone do contato. Mapeado para a coluna {@code phone_number}.
     */
    @Column("phone_number")
    private String phoneNumber;

    /**
     * Indica se o contato está ativo. Valores padrão: {@code true}.
     * <p>Contatos inativos podem ser ignorados em consultas de negócio,
     * mas permanecem no banco para fins de auditoria.</p>
     */
    private boolean active = true;


    /**
     * Retorna o identificador do fornecedor associado a este contato.
     *
     * @return UUID do fornecedor, ou {@code null} se ainda não definido
     */
    public UUID getSupplierId() {
        return supplierId;
    }

    /**
     * Define o identificador do fornecedor associado a este contato.
     *
     * @param supplierId UUID do fornecedor
     */
    public void setSupplierId(UUID supplierId) {
        this.supplierId = supplierId;
    }

    /**
     * Retorna o nome do contato.
     *
     * @return nome do contato (pode ser {@code null} se não informado)
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * Define o nome do contato.
     *
     * @param contactName nome do contato
     */
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    /**
     * Retorna o e-mail do contato.
     *
     * @return e-mail do contato
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o e-mail do contato.
     *
     * @param email endereço de e-mail
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retorna o número de telefone do contato.
     *
     * @return telefone do contato
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Define o número de telefone do contato.
     *
     * @param phoneNumber telefone do contato
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Verifica se o contato está ativo.
     *
     * @return {@code true} se ativo, {@code false} caso contrário
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Define o status de atividade do contato.
     *
     * @param active {@code true} para ativo, {@code false} para inativo
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
