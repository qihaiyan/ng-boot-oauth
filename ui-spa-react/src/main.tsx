import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { AuthContext, AuthProvider, type IAuthContext, type TAuthConfig } from 'react-oauth2-code-pkce'

const authConfig: TAuthConfig = {
  clientId: 'public-client',
  authorizationEndpoint: 'http://localhost:9000/oauth2/authorize',
  logoutEndpoint: 'http://localhost:9000/connect/logout',
  tokenEndpoint: 'http://localhost:9000/oauth2/token',
  redirectUri: 'http://localhost:5173',
  scope: 'openid profile',
  // Example to redirect back to original path after login has completed
  // preLogin: () => localStorage.setItem('preLoginPath', window.location.pathname),
  // postLogin: () => window.location.replace(localStorage.getItem('preLoginPath') || ''),
  decodeToken: true,
  autoLogin: false,
}


createRoot(document.getElementById('root')!).render(
  <StrictMode>
     <AuthProvider authConfig={authConfig}>
      {/* @ts-ignore*/}
    <App />
    </AuthProvider>
  </StrictMode>,
)
