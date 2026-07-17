import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, from } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import Keycloak from 'keycloak-js';
import { UserRole } from '../models/enums';

export interface UserProfile {
  id: string;
  userName: string;
  email: string;
  roles: UserRole[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
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
        this.loadUserProfile(keycloakInstance);

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

  private loadUserProfile(keycloak: Keycloak): void {
    keycloak.loadUserProfile().then(profile => {
      const roles = this.extractRoles(keycloak);
      this.userProfile.next({
        id: profile.id ?? '',
        userName: profile.username ?? '',
        email: profile.email ?? '',
        roles,
      });
    });
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