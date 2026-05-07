package com.gustavosdaniel.stock_flow_api.domain.po;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


/**
 * Representa um usuário do sistema, autenticado via Keycloak.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria,
 * identificador UUID e controle de versão. É utilizada para armazenar o vínculo
 * entre o usuário do Keycloak e o registro interno, além do nome de exibição.
 * </p>
 *
 * <p><strong>Integração e Sincronização (Keycloak):</strong><br>
 * O campo {@code keycloakId} armazena o identificador único (UUID) vindo do Keycloak.
 * O campo {@code role} atua como um "Mirror" (espelho) do nível de acesso do usuário.
 * Para evitar problemas de dessincronização (Dual Write), a role local é atualizada
 * através de uma estratégia <i>Just-in-Time (JIT)</i> no momento em que o usuário
 * acessa a API, tendo o token JWT como a fonte da verdade.</p>
 * * <p><strong>Status do Usuário:</strong><br>
 * O campo {@code isActive} permite a desativação lógica (soft delete) do usuário.
 * Novos usuários são criados como ativos por padrão.</p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>users</strong>.
 * Os mapeamentos de colunas são definidos com {@code @Column} para os campos
 * {@code keycloak_id}, {@code user_name}, {@code role} e {@code is_active}.</p>
 *
 * @see BaseEntity
 */
@Table("users")
public class User extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o framework Spring Data R2DBC.
     */
    public User() {
    }

    /**
     * Construtor para criação de um usuário com os dados essenciais.
     * <p>O status é inicializado automaticamente como ativo ({@code true}) e a
     * role padrão é definida como {@link UserRole#EMPLOYEE}.</p>
     *
     * @param keycloakId identificador do usuário no Keycloak (não nulo)
     * @param userName   nome de usuário (login ou nome exibido)
     */
    public User(String keycloakId, String userName) {
        this.keycloakId = keycloakId;
        this.userName = userName;
    }

    /**
     * Identificador único do usuário no Keycloak.
     * Mapeado para a coluna {@code keycloak_id}.
     * Este campo é imutável após a criação.
     */
    @Column("keycloak_id")
    private String keycloakId;

    /**
     * Nome de usuário para exibição (login ou nome amigável).
     * Mapeado para a coluna {@code user_name}.
     */
    @Column("user_name")
    private String userName;

    /**
     * Nível de acesso (Role) do usuário no sistema.
     * Mapeado para a coluna {@code role} (armazenado como VARCHAR no banco de dados).
     * Sincronizado dinamicamente via JWT. Padrão: {@link UserRole#EMPLOYEE}.
     */
    @Column("role")
    private UserRole role = UserRole.EMPLOYEE;

    /**
     * Indica se o usuário está ativo no sistema. Padrão: {@code true}.
     * Mapeado para a coluna {@code is_active}.
     */
    @Column("is_active")
    private boolean isActive = true;


    /**
     * Retorna o identificador do usuário no Keycloak.
     *
     * @return keycloakId (não deve ser {@code null} após persistência)
     */
    public String getKeycloakId() {
        return keycloakId;
    }

    // Nota: não há setter para keycloakId – o identificador Keycloak é imutável.

    /**
     * Retorna o nome de usuário.
     *
     * @return nome do usuário
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Define o nome de usuário.
     *
     * @param userName novo nome a ser exibido
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Retorna o nível de acesso (Role) atual do usuário.
     * * @return a role do usuário (ex: EMPLOYEE, MANAGER, ADMIN)
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Define o nível de acesso (Role) do usuário.
     * Geralmente invocado durante a Sincronização JIT com o token JWT.
     * * @param role nova role atribuída
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Verifica se o usuário está ativo.
     * * @return {@code true} se ativo, {@code false} se inativo
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Define o status de atividade do usuário.
     * * @param active {@code true} para ativar, {@code false} para desativar (soft delete)
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
}