import { Component, OnInit, inject } from '@angular/core';
import { NgIf, NgForOf } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { catchError, of } from 'rxjs';
import { environment } from "./environment";
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [NgIf, NgForOf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  private readonly oidcSecurityService = inject(OidcSecurityService);
  isAuthenticated: boolean = false;
  userName: string = '';
  messages: string[] = [];

  constructor(private http: HttpClient) {
  }

  ngOnInit(): void {
    this.oidcSecurityService
      .checkAuth()
      .subscribe(({ isAuthenticated, accessToken }) => {
        console.log('app authenticated', isAuthenticated);
        console.log(`Current access token is '${accessToken}'`);
        this.isAuthenticated = isAuthenticated;
      });
    //     this.getUserInfo();
    //     this.getMessages();
  }

  login(): void {
    // The Backend is configured to trigger login when unauthenticated
    //     window.location.href = environment.backendBaseUrl;
    this.oidcSecurityService.authorize();
  }

  logout(): void {
    this.oidcSecurityService.logoff().subscribe((result) => {
      console.log(result);
      this.isAuthenticated = false;
    });
  }

  getUserInfo(): void {
    this.http.get<any>('/userinfo')
      .pipe(catchError((error) => {
        console.error(error);
        return of(null);
      }))
      .subscribe((userInfo) => {
        if (userInfo) {
          this.isAuthenticated = true;
          this.userName = userInfo.sub;
        }
      });
  }

  authorizeMessages(): void {
    // Trigger the Backend to perform the authorization_code grant flow.
    // After authorization is complete, the Backend will redirect back to this app.
    window.location.href = environment.backendBaseUrl + "/oauth2/authorization/messaging-client";
  }

  getMessages(): void {
    this.http.get<string[]>('/messages')
      .pipe(catchError((error) => {
        console.error(error);
        return of([]);
      }))
      .subscribe((messages) => {
        this.messages = messages;
      });
  }
}
