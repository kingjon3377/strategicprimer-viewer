package common.entity;

import org.jspecify.annotations.NonNull;

/**
 * An interface for mutable operations on entities. TODO: We probably want to make entities actually immutable, with
 * "copy-with-change" operations.
 */
public interface IMutableEntity extends IEntity {
	void setLocation(@NonNull Location location);

	void setProperty(final @NonNull EntityProperty<?> property);

	void removeProperty(final @NonNull String propertyName);

	void removeProperty(final @NonNull EntityProperty<?> property);

	@Override
	IMutableEntity copy();
}
