import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import {
  provideRouter,
  withEnabledBlockingInitialNavigation,
} from '@angular/router';
import {
  LogLevel,
  authInterceptor,
  autoLoginPartialRoutesGuard,
  provideAuth,
} from 'angular-auth-oidc-client';
import { routes } from './app.routes';

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
        authority: 'http://localhost:8080',
        authWellknownEndpointUrl: 'http://localhost:8080/.well-known/openid-configuration',
        redirectUrl: window.location.origin,
        postLogoutRedirectUri: window.location.origin,
        clientId: 'spa-client',
        scope: 'openid profile email',
        responseType: 'code',
        silentRenew: true,
        useRefreshToken: true,
        secureRoutes: ['http://localhost:8090/'],
      },
    }),
    provideRouter(
      routes,
      withEnabledBlockingInitialNavigation()
    ),
  ],
};
