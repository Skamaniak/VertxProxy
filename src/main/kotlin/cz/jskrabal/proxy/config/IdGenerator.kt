package cz.jskrabal.proxy.config

internal interface IdGenerator<T> {
    fun generateId(): T
}