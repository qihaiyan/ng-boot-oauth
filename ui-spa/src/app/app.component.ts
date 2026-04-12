import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, of, take } from 'rxjs';
import { environment } from './environment';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent {
  private readonly oidcSecurityService = inject(OidcSecurityService);
  private readonly http = inject(HttpClient);

  readonly isAuthenticated = signal(false);
  readonly userName = signal('');
  readonly messages = signal<string[]>([]);

  constructor() {
    this.checkAuth();
  }

  private checkAuth(): void {
    this.oidcSecurityService.checkAuth()
      .pipe(take(1))
      .subscribe(({ isAuthenticated, accessToken, userData }) => {
        console.log('app authenticated', isAuthenticated);
        console.log(`Current access token is '${accessToken}'`);
        this.isAuthenticated.set(isAuthenticated);
        this.userName.set(userData?.name ?? userData?.preferred_username ?? '');
      });
  }

  login(): void {
    this.oidcSecurityService.authorize();
  }

  logout(): void {
    this.oidcSecurityService.logoff()
      .pipe(take(1))
      .subscribe(() => {
        this.isAuthenticated.set(false);
        this.userName.set('');
        this.messages.set([]);
      });
  }

  getMessages(): void {
    this.http.get<string[]>('http://localhost:8090/messages')
      .pipe(
        catchError((error) => {
          console.error(error);
          return of([]);
        }),
        take(1)
      )
      .subscribe((messages) => {
        this.messages.set(messages);
      });
  }
}