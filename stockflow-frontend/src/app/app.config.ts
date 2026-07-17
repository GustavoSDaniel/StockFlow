import { ApplicationConfig, provideBrowserGlobalErrorListeners, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import Keycloak from 'keycloak-js';

import { routes } from './app.routes';
import { AuthService } from './core/auth/auth.service';
import { apiInterceptor } from './core/http/api.interceptor';
import { errorInterceptor } from './core/http/error.interceptor';
import { environment } from '../environments/environment';

function initializeKeycloak(auth: AuthService): () => Promise<boolean> {
  const keycloak = new Keycloak({
    url: environment.keycloak.url,
    realm: environment.keycloak.realm,
    clientId: environment.keycloak.clientId,
  });

  return () => auth.init(keycloak);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([apiInterceptor, errorInterceptor])),
    provideAnimations(),
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      deps: [AuthService],
      multi: true,
    },
  ],
};
