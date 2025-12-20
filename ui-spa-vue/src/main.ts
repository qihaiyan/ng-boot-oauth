import { createApp } from 'vue'
import { createOAuthClient } from '@volverjs/auth-vue'

import './style.css'
import App from './App.vue'

const app = createApp(App);

const authClient = createOAuthClient({
    url: 'http://localhost:9000',
    clientId: 'public-client',
    scopes: 'openid profile',
})

app.use(authClient, { global: true })

app.mount('#app');
