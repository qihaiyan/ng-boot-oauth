import { Storage } from './Storage'

export class SessionStorage extends Storage {
    /**
     * Get a value from the session storage.
     * @param key - The storage key.
     * @param defaultValue - The default value to return if the key is not found.
     * @returns the value from the session storage.
     * @throws if the session storage is not supported.
     * @example
     * ```typescript
     * const storage = new SessionStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * ```
     */
    public get(key: string, defaultValue?: string) {
        this._checkSupport()
        return sessionStorage.getItem(this.key(key)) ?? defaultValue
    }

    /**
     * Set a value in the session storage.
     * @param key - The storage key.
     * @param value - The value to store.
     * @throws if the session storage is not supported.
     * @example
     * ```typescript
     * const storage = new SessionStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * ```
     */
    public set(key: string, value?: string | null) {
        this._checkSupport()
        if (value === null || value === undefined) {
            this.delete(key)
            return
        }
        sessionStorage.setItem(this.key(key), value)
    }

    /**
     * Delete a value from the session storage.
     * @param key - The storage key.
     * @throws if the session storage is not supported.
     * @example
     * ```typescript
     * const storage = new SessionStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * storage.delete('test')
     * expect(storage.get('test')).toBeNull()
     * ```
     */
    public delete(key: string) {
        this._checkSupport()
        sessionStorage.removeItem(this.key(key))
    }

    /**
     * Clear all values from the session storage.
     * @throws if the session storage is not supported.
     * @example
     * ```typescript
     * const storage = new SessionStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * storage.clear()
     * expect(storage.get('test')).toBeNull()
     * ```
     */
    public clear() {
        this._checkSupport()
        const base = this.key()
        for (const key in sessionStorage) {
            if (key.startsWith(base)) {
                this.delete(key)
            }
        }
    }

    /**
     * Determines if the current browser supports session storage.
     * @returns true if the session storage is supported.
     * @example
     * ```typescript
     * expect(SessionStorage.supported()).toBe(true)
     * ```
     */
    public static supported() {
        return (
            typeof window !== 'undefined'
            && typeof sessionStorage !== 'undefined'
        )
    }

    private _checkSupport() {
        if (!SessionStorage.supported()) {
            throw new Error('Session storage is not supported')
        }
    }
}
