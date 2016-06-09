package cz.jskrabal.proxy.config.enums;

interface IdGenerator<T> {
	T generateId();
}