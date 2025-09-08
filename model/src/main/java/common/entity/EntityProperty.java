package common.entity;

import org.jspecify.annotations.NonNull;

/**
 * A class for strongly-typed entity properties. Basically a strongly-typed object, for use in serialization, but
 * for ease of use also including the property name.
 */
public record EntityProperty<T>(@NonNull String propertyName, @NonNull T propertyValue) {
}
