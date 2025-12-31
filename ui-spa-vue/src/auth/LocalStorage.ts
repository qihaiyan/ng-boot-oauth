import { Storage } from './Storage'

export class LocalStorage extends Storage {
    /**
     * Get a value from the local storage.
     * @param key - The storage key.
     * @param defaultValue - The default value to return if the key is not found.
     * @returns the value from the local storage.
     * @throws if the local storage is not supported.
     * @example
     * ```typescript
     * const storage = new LocalStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * ```
     */
    public get(key: string, defaultValue?: string) {
        this._checkSupport()
        return localStorage.getItem(this.key(key)) ?? defaultValue
    }

    /**
     * Set a value in the local storage.
     * @param key - The storage key.
     * @param value - The value to store.
     * @throws if the local storage is not supported.
     * @example
     * ```typescript
     * const storage = new LocalStorage('oauth')
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
        localStorage.setItem(this.key(key), value)
    }

    /**
     * Delete a value from the local storage.
     * @param name - The storage key.
     * @throws if the local storage is not supported.
     * @example
     * ```typescript
     * const storage = new LocalStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * storage.delete('test')
     * expect(storage.get('test')).toBeNull()
     * ```
     */
    public delete(name: string) {
        this._checkSupport()
        localStorage.removeItem(this.key(name))
    }

    /**
     * Clear all values from the local storage.
     * @throws if the local storage is not supported.
     * @example
     * ```typescript
     * const storage = new LocalStorage('oauth')
     * storage.set('test', 'value')
     * expect(storage.get('test')).toBe('value')
     * storage.clear()
     * expect(storage.get('test')).toBeNull()
     * ```
     */
    public clear() {
        this._checkSupport()
        const base = this.key()
        for (const key in localStorage) {
            if (key.startsWith(base)) {
                this.delete(key)
            }
        }
    }

    /**
     * Check if the local storage is supported.
     * @returns true if the local storage is supported.
     * @example
     * ```typescript
     * expect(LocalStorage.supported()).toBe(true)
     * ```
     */
    public static supported() {
        return (
            typeof window !== 'undefined' && typeof localStorage !== 'undefined'
        )
    }

    private _checkSupport() {
        if (!LocalStorage.supported()) {
            throw new Error('Local storage is not supported')
        }
    }
}
