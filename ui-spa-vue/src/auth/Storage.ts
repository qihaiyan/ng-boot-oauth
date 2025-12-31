export abstract class Storage {
    protected baseKey: string

    abstract get(name: string, defaultValue?: string): string | null | undefined
    abstract set(name: string, value: unknown): void
    abstract delete(name: string): void
    abstract clear(): void

    /**
     * Creates a new storage instance.
     * @param baseKey - The base key for storage.
     */
    constructor(baseKey = '') {
        this.baseKey = baseKey
    }

    /**
     * Determines if the current browser supports this storage type.
     * @returns true if the storage type is supported.
     */
    public static supported() {
        return true
    }

    /**
     * Returns true if the storage has a value for the given key.
     * @param name - The storage key.
     * @returns true if the storage has a value for the given key.
     */
    public has(name: string): boolean {
        return this.get(name) !== null && this.get(name) !== undefined
    }

    /**
     * Returns a scoped key for storage.
     * @param key - The storage key.
     * @returns the scoped key for storage.
     */
    protected key(key = ''): string {
        if (this.baseKey && !key.includes(this.baseKey)) {
            return `${this.baseKey}.${key}`
        }

        return key
    }
}
