<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','lastName','email','username','password','password-confirm'); section>
    <#if section = "header">
    <#elseif section = "form">
        <div class="stockflow-brand">
            <div class="stockflow-logo">
                <span class="material-icons-round">inventory_2</span>
            </div>
            <h1 class="stockflow-title">StockFlow</h1>
            <p class="stockflow-subtitle">Gestão de inventário inteligente</p>
            <p class="stockflow-tagline">Crie sua conta para começar</p>
        </div>
        <form id="kc-register-form" action="${url.registrationAction}" method="post" novalidate="novalidate">
            <div class="form-group ${messagesPerField.printIfExists('firstName','has-error')}">
                <label for="firstName">Nome</label>
                <input tabindex="1" id="firstName" class="stockflow-input" name="firstName" value="${(register.formData.firstName!'')}" type="text" autofocus autocomplete="off" placeholder="Seu nome" />
                <#if messagesPerField.existsError('firstName')>
                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('firstName'))?no_esc}</span>
                </#if>
            </div>

            <div class="form-group ${messagesPerField.printIfExists('lastName','has-error')}">
                <label for="lastName">Sobrenome</label>
                <input tabindex="2" id="lastName" class="stockflow-input" name="lastName" value="${(register.formData.lastName!'')}" type="text" autocomplete="off" placeholder="Seu sobrenome" />
                <#if messagesPerField.existsError('lastName')>
                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('lastName'))?no_esc}</span>
                </#if>
            </div>

            <#if !realm.registrationEmailAsUsername>
            <div class="form-group ${messagesPerField.printIfExists('username','has-error')}">
                <label for="username">Usuário</label>
                <input tabindex="3" id="username" class="stockflow-input" name="username" value="${(register.formData.username!'')}" type="text" autocomplete="off" placeholder="Nome de usuário" />
                <#if messagesPerField.existsError('username')>
                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('username'))?no_esc}</span>
                </#if>
            </div>
            </#if>

            <div class="form-group ${messagesPerField.printIfExists('email','has-error')}">
                <label for="email">E-mail</label>
                <input tabindex="4" id="email" class="stockflow-input" name="email" value="${(register.formData.email!'')}" type="text" autocomplete="off" placeholder="seu@email.com" />
                <#if messagesPerField.existsError('email')>
                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('email'))?no_esc}</span>
                </#if>
            </div>

            <#if passwordRequired??>
            <div class="form-group ${messagesPerField.printIfExists('password','has-error')}">
                <label for="password">Senha</label>
                <input tabindex="5" id="password" class="stockflow-input" name="password" type="password" autocomplete="off" placeholder="Mínimo 8 caracteres" />
                <#if messagesPerField.existsError('password')>
                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('password'))?no_esc}</span>
                </#if>
            </div>

            <div class="form-group ${messagesPerField.printIfExists('password-confirm','has-error')}">
                <label for="password-confirm">Confirmar senha</label>
                <input tabindex="6" id="password-confirm" class="stockflow-input" name="password-confirm" type="password" autocomplete="off" placeholder="Repita a senha" />
                <#if messagesPerField.existsError('password-confirm')>
                    <span class="kc-feedback-text">${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}</span>
                </#if>
            </div>
            </#if>

            <#if recaptchaRequired??>
            <div class="form-group">
                <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
            </div>
            </#if>

            <div id="kc-form-buttons">
                <input tabindex="7" type="submit" value="Criar conta" />
            </div>
        </form>
    <#elseif section = "info">
        <div id="kc-registration" style="text-align:center; margin-top:24px; padding-top:20px; border-top:1px solid #e5e7eb;">
            <span style="color:#6b7280; font-size:14px;">Já possui uma conta? </span>
            <a tabindex="8" href="${url.loginUrl}" style="color:#7c3aed; font-weight:600; text-decoration:none; font-size:14px;">Fazer login</a>
        </div>
    </#if>
</@layout.registrationLayout>
