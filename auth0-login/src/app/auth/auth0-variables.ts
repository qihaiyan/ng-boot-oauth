interface AuthConfig {
  clientID: string;
  domain: string;
  callbackURL: string;
}

export const AUTH_CONFIG: AuthConfig = {
  clientID: '1HgAlrpMiE5Rb7KypFyTTO41NCCMmYHa',
  domain: 'qihy.auth0.com',
  callbackURL: 'http://localhost:4200/callback'
};
