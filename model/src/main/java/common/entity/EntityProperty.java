package common.entity;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for strongly-typed entity properties. Basically an object and its type, for use in serialization, but
 * for ease of use also including the property name. TODO: Convert to record, combining with PropertyImpl?
 */
public interface EntityProperty<T> {
	@NotNull String getPropertyName();
	@NotNull T getPropertyValue();
}
