package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa uma notificação gerada pelo sistema, geralmente relacionada a eventos de estoque
 * (como estoque baixo, crítico, etc.) ou outros alertas de negócio.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria, versão e identificador.
 * As notificações podem ser lidas, resolvidas e atribuídas a um usuário específico.
 * </p>
 *
 * <p><strong>Status da notificação:</strong>
 * <ul>
 *   <li>{@code read} – indica se a notificação foi visualizada pelo usuário (com timestamp {@code readAt}).</li>
 *   <li>{@code resolved} – indica se a causa da notificação foi tratada (com timestamp {@code resolvedAt}).</li>
 * </ul>
 * Ao marcar como resolvida ({@link #markAsResolved()}), a notificação é automaticamente marcada como lida, caso ainda não esteja.
 * </p>
 *
 * <p><strong>Tipos e prioridades:</strong>
 * Os campos {@link NotificationType} e {@link NotificationPriority} permitem classificar a notificação
 * para exibição adequada na interface do usuário (ex.: prioridade alta com destaque vermelho).</p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>notifications</strong>.
 * Os mapeamentos de colunas são definidos explicitamente com {@code @Column}.</p>
 *
 * @see BaseEntity
 * @see NotificationType
 * @see NotificationPriority
 */
@Table("notifications")
public class Notification extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o JPA.
     */
    public Notification() {
    }

    /**
     * Construtor para criação de uma notificação com os principais dados.
     *
     * @param productId          identificador do produto relacionado (opcional)
     * @param productName        nome do produto no momento da notificação
     * @param productSku         SKU do produto
     * @param notificationType   tipo da notificação (baixo estoque, venda, etc.)
     * @param notificationPriority prioridade da notificação (baixa, média, alta)
     * @param title              título resumido da notificação
     * @param message            mensagem detalhada
     * @param currentQuantity    quantidade atual do produto (contexto)
     * @param minimumQuantity    quantidade mínima configurada (contexto)
     * @param assignedTo         UUID do usuário responsável (pode ser nulo)
     */
    public Notification(UUID productId, String productName, String productSku,
                        NotificationType notificationType, NotificationPriority notificationPriority,
                        String title, String message, Integer currentQuantity,
                        Integer minimumQuantity, UUID assignedTo) {
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.notificationType = notificationType;
        this.notificationPriority = notificationPriority;
        this.title = title;
        this.message = message;
        this.currentQuantity = currentQuantity;
        this.minimumQuantity = minimumQuantity;
        this.assignedTo = assignedTo;
    }


    /**
     * Identificador do produto associado à notificação.
     * Mapeado para a coluna {@code product_id}.
     */
    @Column("product_id")
    private UUID productId;

    /**
     * Nome do produto no momento da geração da notificação (desnormalizado para histórico).
     * Mapeado para a coluna {@code product_name}.
     */
    @Column("product_name")
    private String productName;

    /**
     * SKU do produto no momento da notificação.
     * Mapeado para a coluna {@code product_sku}.
     */
    @Column("product_sku")
    private String productSku;

    /**
     * Tipo da notificação (ex.: {@code LOW_STOCK}, {@code OUT_OF_STOCK}).
     * Mapeado para a coluna {@code notification_type}.
     */
    @Column("notification_type")
    private NotificationType notificationType;

    /**
     * Prioridade da notificação (ex.: BAIXA, MÉDIA, ALTA).
     * Mapeado para a coluna {@code notification_priority}.
     */
    @Column("notification_priority")
    private NotificationPriority notificationPriority;

    /**
     * Título resumido da notificação.
     */
    private String title;

    /**
     * Mensagem descritiva da notificação.
     */
    private String message;

    /**
     * Quantidade atual do produto no momento da notificação.
     * Mapeado para a coluna {@code current_quantity}.
     */
    @Column("current_quantity")
    private Integer currentQuantity;

    /**
     * Quantidade mínima configurada para o produto (ponto de gatilho).
     * Mapeado para a coluna {@code minimum_quantity}.
     */
    @Column("minimum_quantity")
    private Integer minimumQuantity;

    /**
     * Indica se a notificação já foi lida pelo usuário. Padrão: {@code false}.
     * Mapeado para a coluna {@code is_read}.
     */
    @Column("is_read")
    private boolean read = false;

    /**
     * Indica se a situação que gerou a notificação foi resolvida. Padrão: {@code false}.
     * Mapeado para a coluna {@code is_resolved}.
     */
    @Column("is_resolved")
    private boolean resolved = false;

    /**
     * UUID do usuário responsável por tratar a notificação.
     * Mapeado para a coluna {@code assigned_to}.
     */
    @Column("assigned_to")
    private UUID assignedTo;

    /**
     * Data/hora em que a notificação foi lida (preenchido por {@link #markAsRead()}).
     * Mapeado para a coluna {@code read_at}.
     */
    @Column("read_at")
    private LocalDateTime readAt;

    /**
     * Data/hora em que a notificação foi resolvida (preenchido por {@link #markAsResolved()}).
     * Mapeado para a coluna {@code resolved_at}.
     */
    @Column("resolved_at")
    private LocalDateTime resolvedAt;


    /**
     * Atribui a notificação a um usuário responsável.
     *
     * @param userId UUID do usuário (não pode ser nulo)
     * @throws IllegalArgumentException se {@code userId} for {@code null}
     */
    public void assingTo(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("Usuário não pode ser nulo");
        this.assignedTo = userId;
    }

    /**
     * Marca a notificação como lida.
     * <p>Se já estiver lida, o método não faz nada. Define {@code read = true}
     * e {@code readAt} como a data/hora atual.</p>
     */
    public void markAsRead() {
        if (this.read) return;
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Marca a notificação como resolvida.
     * <p>Se já estiver resolvida, o método não faz nada.
     * Define {@code resolved = true} e {@code resolvedAt} agora.
     * Além disso, se a notificação ainda não foi lida, chama automaticamente {@link #markAsRead()}.</p>
     */
    public void markAsResolved() {
        if (this.resolved) return;
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        if (!this.read) markAsRead();
    }


    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationPriority getNotificationPriority() {
        return notificationPriority;
    }

    public void setNotificationPriority(NotificationPriority notificationPriority) {
        this.notificationPriority = notificationPriority;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(Integer currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    /**
     * Verifica se a notificação foi lida.
     *
     * @return {@code true} se lida; {@code false} caso contrário
     */
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Verifica se a notificação foi resolvida.
     *
     * @return {@code true} se resolvida; {@code false} caso contrário
     */
    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }

    /**
     * Data/hora da leitura (preenchido automaticamente por {@link #markAsRead()}).
     *
     * @return {@link LocalDateTime} da leitura ou {@code null}
     */
    public LocalDateTime getReadAt() {
        return readAt;
    }

    /**
     * Data/hora da resolução (preenchido automaticamente por {@link #markAsResolved()}).
     *
     * @return {@link LocalDateTime} da resolução ou {@code null}
     */
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
