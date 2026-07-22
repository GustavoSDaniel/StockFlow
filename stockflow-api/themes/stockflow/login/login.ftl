<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationRequired??; section>
    <#if section = "header">
    <#elseif section = "form">
        <div class="stockflow-brand">
            <div class="stockflow-logo">
                <span class="material-icons-round">inventory_2</span>
            </div>
            <h1 class="stockflow-title">StockFlow</h1>
            <p class="stockflow-subtitle">Gestão de inventário inteligente</p>
            <p class="stockflow-tagline">Acesse sua conta para continuar</p>
        </div>
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <#if realm.password>
                    <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" novalidate="novalidate">
                        <#if !usernameHidden??>
                            <div class="form-group ${messagesPerField.printIfExists('username','has-error')}">
                                <label for="username"><#if !realm.loginWithEmailAllowed>Usuário<#elseif !realm.registrationEmailAsUsername>E-mail ou usuário<#else>E-mail</#if></label>
                                <input tabindex="2" id="username" class="stockflow-input" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="off" placeholder="Digite seu e-mail ou usuário" />
                                <#if messagesPerField.existsError('username')>
                                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
                                </#if>
                            </div>
                        </#if>

                        <div class="form-group ${messagesPerField.printIfExists('password','has-error')}">
                            <label for="password">Senha</label>
                            <div class="password-wrapper">
                                <input tabindex="3" id="password" class="stockflow-input" name="password" type="password" autocomplete="off" placeholder="Digite sua senha" />
                            </div>
                            <#if messagesPerField.existsError('password')>
                                <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                            </#if>
                        </div>

                        <div id="kc-form-buttons">
                            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                            <input tabindex="5" id="kc-login" name="login" type="submit" value="Entrar" />
                        </div>

                        <#if realm.rememberMe && !usernameHidden??>
                            <div class="form-group checkbox" style="margin-top:16px; text-align:center;">
                                <#if login.rememberMe??>
                                    <input tabindex="4" id="rememberMe" name="rememberMe" type="checkbox" checked>
                                <#else>
                                    <input tabindex="4" id="rememberMe" name="rememberMe" type="checkbox">
                                </#if>
                                <label for="rememberMe" style="display:inline; font-size:13px; font-weight:400; color:#6b7280;">Lembrar de mim</label>
                            </div>
                        </#if>

                        <#if realm.resetPasswordAllowed>
                            <div style="text-align:center; margin-top:16px;">
                                <a tabindex="6" href="${url.loginResetCredentialsUrl}" style="color:#7c3aed; font-weight:500; text-decoration:none; font-size:14px;">Esqueceu sua senha?</a>
                            </div>
                        </#if>
                    </form>
                </#if>
            </div>
        </div>
    <#elseif section = "info">
        <#if realm.password && realm.registrationAllowed && !registrationRequired??>
            <div id="kc-registration" style="text-align:center; margin-top:24px; padding-top:20px; border-top:1px solid #e5e7eb;">
                <span style="color:#6b7280; font-size:14px;">Não tem uma conta? </span>
                <a tabindex="7" href="${url.registrationUrl}" style="color:#7c3aed; font-weight:600; text-decoration:none; font-size:14px;">Criar conta</a>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>
