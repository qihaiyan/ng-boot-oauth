import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withEnabledBlockingInitialNavigation, withInMemoryScrolling } from '@angular/router';
import {
  LogLevel,
  authInterceptor,
  autoLoginPartialRoutesGuard,
  provideAuth,
} from 'angular-auth-oidc-client';
import Aura from '@primeuix/themes/aura';
import { providePrimeNG } from 'primeng/config';
import { appRoutes } from './app.routes';

export const appConfig: ApplicationConfig = {
    providers: [
        provideHttpClient(withInterceptors([authInterceptor()])),
            provideAuth({
              config: {
                triggerAuthorizationResultEvent: true,
                postLoginRoute: '/',
                forbiddenRoute: '/forbidden',
                unauthorizedRoute: '/unauthorized',
                logLevel: LogLevel.Debug,
                historyCleanupOff: true,
                authority: 'http://localhost:9000',
                authWellknownEndpointUrl: 'http://localhost:9000/.well-known/openid-configuration',
                redirectUrl: window.location.origin,
                postLogoutRedirectUri: window.location.origin,
                clientId: 'public-client',
                scope: 'openid profile',
                responseType: 'code',
                silentRenew: true,
                useRefreshToken: true,
                secureRoutes: ['http://localhost:8090/'],
              },
            }),
        provideRouter(appRoutes, withInMemoryScrolling({ anchorScrolling: 'enabled', scrollPositionRestoration: 'enabled' }), withEnabledBlockingInitialNavigation()),
//         provideHttpClient(withFetch()),
        provideAnimationsAsync(),
        providePrimeNG({ theme: { preset: Aura, options: { darkModeSelector: '.app-dark' } } })
    ]
};
