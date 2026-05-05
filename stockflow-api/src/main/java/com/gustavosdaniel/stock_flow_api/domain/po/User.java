package com.gustavosdaniel.stock_flow_api.domain.po;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


/**
 * Representa um usuário do sistema, normalmente autenticado via Keycloak.
 * <p>
 * Esta entidade estende {@link BaseEntity}, herdando os campos de auditoria,
 * identificador UUID e controle de versão. É utilizada para armazenar o vínculo
 * entre o usuário do Keycloak e o registro interno, além do nome de exibição.
 * </p>
 *
 * <p><strong>Integração com Keycloak:</strong>
 * O campo {@code keycloakId} armazena o identificador único do usuário no Keycloak
 * (geralmente um UUID gerado pelo Keycloak). Isso permite referenciar o usuário
 * sem expor dados sensíveis ou depender de APIs externas a cada consulta.</p>
 *
 * <p>A tabela correspondente no banco de dados chama-se <strong>users</strong>.
 * Os mapeamentos de colunas são definidos com {@code @Column} para os campos
 * {@code keycloak_id} e {@code user_name}.</p>
 *
 * @see BaseEntity
 */
@Table("users")
public class User extends BaseEntity {

    /**
     * Construtor padrão obrigatório para o JPA.
     */
    public User() {
    }

    /**
     * Construtor para criação de um usuário com os dados essenciais.
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
     * Este campo não deve ser alterado após a criação.
     */
    @Column("keycloak_id")
    private String keycloakId;

    /**
     * Nome de usuário para exibição (login ou nome amigável).
     * Mapeado para a coluna {@code user_name}.
     * Pode ser alterado posteriormente via {@link #setUserName(String)}.
     */
    @Column("user_name")
    private String userName;


    /**
     * Retorna o identificador do usuário no Keycloak.
     *
     * @return keycloakId (não deve ser {@code null} após persistência)
     */
    public String getKeycloakId() {
        return keycloakId;
    }

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

    // Nota: não há setter para keycloakId – o identificador Keycloak é imutável após a criação.
}