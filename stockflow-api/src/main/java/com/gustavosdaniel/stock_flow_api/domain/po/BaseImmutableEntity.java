package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Classe base abstrata para entidades <strong>imutáveis</strong> (read-only após persistência).
 * Diferente de {@link BaseEntity}, esta classe não possui controle de versão nem campos de última
 * modificação (updatedAt / updatedBy), pois seus registros nunca devem ser alterados depois de criados.
 *
 * <p>Utilidades principais:
 * <ul>
 *   <li>Identificador único {@link UUID} gerado automaticamente</li>
 *   <li>Auditoria de criação (usuário e data/hora)</li>
 *   <li>Implementação padrão de {@link Persistable} para uso com Spring Data JPA</li>
 *   <li>Métodos {@code equals()} e {@code hashCode()} baseados apenas no ID</li>
 * </ul>
 *
 * <p><strong>Comportamento de novidade ({@link #isNew()})</strong>:
 * A entidade é considerada nova quando {@code createdAt} é {@code null}. Isso ocorre porque entidades
 * imutáveis, uma vez persistidas, sempre terão a data de criação preenchida. Esta lógica dispensa o campo
 * {@code version} típico de entidades mutáveis.
 *
 * <p><strong>Restrição de modificação</strong>:
 * Esta classe NÃO fornece setters públicos para seus campos. Os únicos setters existentes têm visibilidade
 * de pacote e destinam-se exclusivamente ao preenchimento automático pelo mecanismo de auditoria do Spring
 * Data (via {@code AuditingEntityListener}). Em código de aplicação, os campos devem ser considerados
 * <em>read-only</em> após a construção.
 *
 * <p><strong>Uso típico</strong>:
 * <pre>
 * &#64;Entity
 * public class AuditLog extends BaseImmutableEntity {
 *     private String acao;
 *     private String detalhes;
 *     // getters públicos, sem setters
 * }
 * </pre>
 *
 * @see Persistable
 * @see BaseEntity  para entidades mutáveis (com version e updatedAt/updatedBy)
 */
abstract class BaseImmutableEntity implements Persistable<UUID> {

    /**
     * Identificador único da entidade. Gerado como UUID aleatório no momento da instanciação.
     * Este valor é imutável e nunca deve ser alterado após a criação do objeto.
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * Identificador do usuário que criou a entidade.
     * Preenchido automaticamente pelo Spring Data Auditing com base no {@code AuditorAware} configurado.
     * Após a persistência, este campo não pode ser modificado.
     */
    @CreatedBy
    private UUID createdBy;

    /**
     * Data e hora da criação da entidade.
     * Preenchido automaticamente pelo Spring Data Auditing no momento da primeira persistência.
     * Serve também como indicador de novidade: se {@code null}, a entidade ainda não foi salva.
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Indica se a entidade é nova (ainda não foi persistida no banco de dados).
     * O Spring Data JPA utiliza este método para decidir entre {@code persist()} e {@code merge()}.
     * <p>
     * Para entidades imutáveis, uma entidade é considerada nova enquanto {@code createdAt} estiver
     * {@code null} – ou seja, antes de a auditoria preencher a data de criação.
     *
     * @return {@code true} se {@code createdAt == null} (entidade recém-criada em memória),
     *         {@code false} caso contrário (já possui data de criação, portanto já persistida)
     */
    @Override
    @Transient
    public boolean isNew() {
        return this.createdAt == null;
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
     * Retorna o identificador do usuário que criou a entidade.
     *
     * @return UUID do criador, ou {@code null} se a entidade ainda não foi persistida
     *         ou o sistema de auditoria não estiver configurado
     */
    public UUID getCreatedBy() {
        return createdBy;
    }

    /**
     * Define o identificador do usuário criador.
     * <p>
     * <strong>Visibilidade de pacote</strong>: este método destina-se exclusivamente ao mecanismo de
     * auditoria do Spring Data (ex.: {@code AuditingEntityListener}). Código da aplicação não deve
     * chamá-lo diretamente, pois violaria a imutabilidade da entidade.
     *
     * @param createdBy UUID do criador
     */
    void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Retorna a data e hora de criação da entidade.
     *
     * @return {@link LocalDateTime} da criação, ou {@code null} se a entidade ainda não foi persistida
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Define a data/hora de criação.
     * <p>
     * <strong>Visibilidade de pacote</strong>: restrito ao framework de auditoria.
     * Aplicações devem tratar este campo como <em>read-only</em>.
     *
     * @param createdAt data/hora a ser definida
     */
    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Compara esta entidade com outro objeto. A comparação é baseada exclusivamente no ID,
     * pois duas entidades com o mesmo ID representam o mesmo registro no banco de dados,
     * independentemente do estado dos demais campos (que são imutáveis).
     *
     * @param o objeto a ser comparado
     * @return {@code true} se o outro objeto for da mesma classe e tiver o mesmo ID;
     *         {@code false} caso contrário
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseImmutableEntity that = (BaseImmutableEntity) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Calcula o hash code baseado apenas no ID, consistente com o método {@link #equals(Object)}.
     *
     * @return hash code do ID (ou 0 se ID for {@code null}, o que não ocorre na prática)
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
