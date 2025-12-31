import type { App, InjectionKey } from 'vue'
import type { OAuthClientOptions } from './OAuthClient'
import { getCurrentInstance, inject } from 'vue'
import { OAuthClient } from './OAuthClient'

export { OAuthClient }
export { LocalStorage } from './LocalStorage'
export { SessionStorage } from './SessionStorage'
export { Storage } from './Storage'
export {
    ClientSecretBasic,
    ClientSecretPost,
    PrivateKeyJwt,
    TlsClientAuth,
} from 'oauth4webapi'

export const authClientInjectionKey = Symbol('') as InjectionKey<OAuthClient>

export class OAuthClientPlugin extends OAuthClient {
    public install(app: App, { global = false } = {}) {
        if (global) {
            app.config.globalProperties.$vvAuth = this
        }
        app.provide(authClientInjectionKey, this)
    }
}

/**
 * Create a new OAuthClient instance.
 * @param options - The OAuthClient options, see {@link OAuthClientOptions}.
 * @returns {@link OAuthClient} instance.
 * @example
 * ```typescript
 * import { createOAuthClient } from '@volverjs/auth-vue'
 * import { createApp } from 'vue'
 * import App from './App.vue'
 *
 * const app = createApp(App)
 * const authClient = createOAuthClient({
 * 	url: 'https://example.com',
 * 	clientId: 'my-client-id',
 * })
 *
 * app.use(authClient, { global: true })
 * ```
 */
export function createOAuthClient(options: OAuthClientOptions) {
    return new OAuthClientPlugin(options)
}

/**
 * Use the composition API to get the OAuthClient instance.
 * @param options - The OAuthClient options, see {@link OAuthClientOptions}.
 * @returns {@link OAuthClient} instance.
 * @remarks This function must be called in the `setup` function of a component.
 * @throws if the OAuthClient is not installed.
 * @throws if the function is not called in the `setup` function.
 *
 * @example
 * ```html
 * <template>
 *  <button @click="authorize">Login</button>
 * </template>
 *
 * <script setup>
 * 	import { useOAuthClient } from '@volverjs/auth-vue'
 * 	const authClient = useOAuthClient()
 * 	const authorize = () => authClient.authorize()
 * </script>
 * ```
 */
export function useOAuthClient(options?: OAuthClientOptions) {
    const client = inject(authClientInjectionKey)
    const instance = getCurrentInstance()
    if (!instance) {
        throw new Error('useOAuthClient must be called in the setup function')
    }
    if (!client) {
        throw new Error('OAuthClient is not installed')
    }
    if (options) {
        client.extend(options)
    }
    return client
}
