import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, firstValueFrom } from 'rxjs';
import Keycloak from 'keycloak-js';
import { UserRole } from '../models/enums';
import { environment } from '../../../environments/environment';

export interface UserProfile {
  id: string;
  userName: string;
  email: string;
  roles: UserRole[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private keycloakInstance: Keycloak | null = null;
  private isAuthenticated = new BehaviorSubject<boolean>(false);
  private userProfile = new BehaviorSubject<UserProfile | null>(null);
  private token = new BehaviorSubject<string>('');

  isAuthenticated$ = this.isAuthenticated.asObservable();
  userProfile$ = this.userProfile.asObservable();
  token$ = this.token.asObservable();

  get keycloak(): Keycloak | null {
    return this.keycloakInstance;
  }

  async init(keycloakInstance: Keycloak): Promise<boolean> {
    this.keycloakInstance = keycloakInstance;

    try {
      const authenticated = await keycloakInstance.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        checkLoginIframe: false,
      });

      this.isAuthenticated.next(authenticated);

      if (authenticated) {
        this.token.next(keycloakInstance.token ?? '');

        // Aguarda o perfil carregar antes de liberar a navegação
        // Isso garante que a sidebar tenha os itens de navegação prontos
        await this.loadUserProfileAsync(keycloakInstance);

        // Sincroniza o usuário com o backend (JIT upsert via /me)
        // Fire-and-forget: não bloqueia a navegação se falhar
        this.syncUserWithBackend();

        keycloakInstance.onTokenExpired = () => {
          keycloakInstance.updateToken(30).then(() => {
            this.token.next(keycloakInstance.token ?? '');
          });
        };
      }

      return authenticated;
    } catch {
      this.isAuthenticated.next(false);
      return false;
    }
  }

  /**
   * Chama o endpoint /api/v1/users/me para garantir que o usuário
   * autenticado pelo Keycloak seja sincronizado (criado/atualizado) no banco de dados.
   * O backend faz o upsert automático via JWT (Just-in-Time).
   */
  private syncUserWithBackend(): void {
    firstValueFrom(this.http.get(`${environment.apiUrl}/api/v1/users/me`))
      .then(() => console.log('[AuthService] Usuário sincronizado com o backend.'))
      .catch(err => console.warn('[AuthService] Sync com backend falhou (será tentado novamente nas próximas requisições):', err.message));
  }

  /**
   * Carrega o perfil do Keycloak e popula o userProfile$ de forma assíncrona.
   * Deve ser aguardado no init() para garantir que a sidebar tenha os dados prontos.
   */
  private async loadUserProfileAsync(keycloak: Keycloak): Promise<void> {
    try {
      const profile = await keycloak.loadUserProfile();
      const roles = this.extractRoles(keycloak);
      this.userProfile.next({
        id: profile.id ?? '',
        userName: profile.username ?? '',
        email: profile.email ?? '',
        roles,
      });
    } catch (err) {
      console.warn('[AuthService] Falha ao carregar perfil do Keycloak:', err);
      // Mesmo sem perfil, o usuário está autenticado — navegação prossegue
    }
  }

  private extractRoles(keycloak: Keycloak): UserRole[] {
    const roles: UserRole[] = [];
    const realmRoles = keycloak.realmAccess?.roles ?? [];
    if (realmRoles.includes('ADMIN')) roles.push(UserRole.ADMIN);
    if (realmRoles.includes('MANAGER')) roles.push(UserRole.MANAGER);
    if (realmRoles.includes('EMPLOYEE')) roles.push(UserRole.EMPLOYEE);
    return roles;
  }

  login(): void {
    this.keycloakInstance?.login({ redirectUri: window.location.origin });
  }

  register(): void {
    this.keycloakInstance?.register({ redirectUri: window.location.origin });
  }

  logout(): void {
    this.keycloakInstance?.logout({ redirectUri: window.location.origin });
    this.isAuthenticated.next(false);
    this.userProfile.next(null);
    this.token.next('');
  }

  hasRole(role: UserRole): boolean {
    const profile = this.userProfile.value;
    if (!profile) return false;

    const roleLevels: Record<UserRole, number> = {
      [UserRole.EMPLOYEE]: 1,
      [UserRole.MANAGER]: 2,
      [UserRole.ADMIN]: 3,
    };

    return profile.roles.some(r => roleLevels[r] >= roleLevels[role]);
  }

  getToken(): string {
    return this.token.value;
  }
}