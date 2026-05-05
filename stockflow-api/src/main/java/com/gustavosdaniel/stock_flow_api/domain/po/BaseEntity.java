package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe base abstrata para todas as entidades JPA que necessitam de:
 * <ul>
 *   <li>Identificador único do tipo {@link UUID}</li>
 *   <li>Controle de versão para otimismo (lock otimista)</li>
 *   <li>Auditoria automática (datas de criação/atualização e usuários responsáveis)</li>
 *   <li>Implementação padrão de {@link Persistable} para uso com Spring Data</li>
 * </ul>
 *
 * <p>Esta classe utiliza anotações do JPA e do Spring Data Auditing.
 * Os campos de auditoria são preenchidos automaticamente por listeners como
 * {@code AuditingEntityListener} quando configurado na aplicação.</p>
 *
 * <p>Os métodos equals() e hashCode() são baseados exclusivamente no ID, o que
 * garante o comportamento correto em coleções e sessões Hibernate, desde que o
 * ID nunca seja alterado (é gerado no construtor).</p>
 *
 * @see Persistable
 *
 */
abstract class BaseEntity implements Persistable<UUID> {

    /**
     * Identificador único da entidade. Gerado automaticamente como UUID aleatório no momento da criação.
     * Este valor é imutável após a instanciação.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * Versão da entidade, usada para controle de concorrência otimista pelo JPA.
     * O Spring Data incrementa este campo automaticamente a cada atualização.
     * Se {@code null}, a entidade é considerada nova (ainda não persistida).
     */
    @Version
    private Long version;

    /**
     * Data/hora de criação da entidade. Preenchido automaticamente pelo Spring Data Auditing
     * no momento da primeira persistência.
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Data/hora da última modificação. Atualizado automaticamente pelo Spring Data Auditing
     * sempre que a entidade é alterada e salva.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Identificador do usuário que criou a entidade. Preenchido automaticamente pelo
     * Spring Data Auditing com o valor fornecido pelo {@code AuditorAware} configurado.
     */
    @CreatedBy
    private UUID createdBy;

    /**
     * Identificador do último usuário que modificou a entidade. Atualizado automaticamente
     * pelo Spring Data Auditing.
     */
    @LastModifiedBy
    private UUID updatedBy;

    /**
     * Indica se a entidade é nova (ainda não foi persistida no banco de dados).
     * O Spring Data JPA usa este método para decidir se deve executar {@code persist()}
     * ou {@code merge()}.
     *
     * @return {@code true} se a versão é {@code null} (entidade recém-criada),
     *         {@code false} caso contrário (entidade já persistida)
     */
    @Override
    @Transient
    public boolean isNew() {
        return this.version == null;
    }

    /**
     * Retorna o identificador único da entidade.
     *
     * @return UUID da entidade, nunca {@code null}
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Retorna a data/hora de criação.
     *
     * @return data e hora da criação, ou {@code null} se a entidade ainda não foi persistida
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data/hora de criação. Normalmente preenchido automaticamente pelo auditing,
     * mas disponível para uso em testes ou cenários específicos.
     * <p>Visibilidade de pacote – use apenas dentro da mesma camada de persistência.
     *
     * @param createdAt data/hora a ser definida
     */
    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retorna a data/hora da última atualização.
     *
     * @return última data/hora de modificação, ou {@code null} se nunca foi atualizada
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Define a data/hora da última atualização. Normalmente preenchido automaticamente.
     * <p>Visibilidade de pacote.
     *
     * @param updatedAt data/hora a ser definida
     */
    void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Retorna o identificador do usuário que criou a entidade.
     *
     * @return UUID do criador, ou {@code null} se o sistema de auditoria não estiver configurado
     *         ou a entidade ainda não foi persistida
     */
    public UUID getCreatedBy() {
        return createdBy;
    }

    /**
     * Define o identificador do usuário criador. Normalmente preenchido automaticamente.
     * <p>Visibilidade de pacote.
     *
     * @param createdBy UUID do criador
     */
    void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Retorna o identificador do último usuário que modificou a entidade.
     *
     * @return UUID do último modificador, ou {@code null} se nunca foi modificada
     */
    public UUID getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Define o identificador do último modificador.
     * <p>Visibilidade de pacote.
     *
     * @param updatedBy UUID do modificador
     */
    void setUpdatedBy(UUID updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Retorna a versão atual da entidade para controle de concorrência otimista.
     *
     * @return versão atual, ou {@code null} se a entidade ainda é nova
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Compara esta entidade com outro objeto. A comparação é baseada exclusivamente no ID,
     * pois entidades equivalentes representam a mesma linha no banco de dados.
     * Duas entidades de classes diferentes nunca serão consideradas iguais.
     *
     * @param o objeto a ser comparado
     * @return {@code true} se o outro objeto for do mesmo tipo e tiver o mesmo ID;
     *         {@code false} caso contrário
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcula o hash code baseado apenas no ID, de acordo com o contrato de equals().
     *
     * @return hash code do ID (ou 0 se o ID for {@code null}, mas na prática nunca é)
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
