package common.entity;

import org.jetbrains.annotations.NotNull;

/**
 * A class for strongly-typed entity properties. Basically a strongly-typed object, for use in serialization, but
 * for ease of use also including the property name.
 */
public record EntityProperty<T>(@NotNull String propertyName, @NotNull T propertyValue) {
}
