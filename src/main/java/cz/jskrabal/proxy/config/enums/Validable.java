package cz.jskrabal.proxy.config.enums;

/**
 * Created by janskrabal on 04/06/16.
 */
public interface Validable<T> {
    default boolean validate(T value){
        return getType().isInstance(value);
    }

    Class getType();
}
