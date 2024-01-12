package common.entity;

import org.jetbrains.annotations.NotNull;

/**
 * An interface for mutable opeerations on entities. TODO: We probably want to make entities actually immutable, with
 * "copy-with-change" operations.
 */
public interface IMutableEntity extends IEntity {
	void setLocation(@NotNull Location location);
	void setProperty(final @NotNull EntityProperty<?> property);
	void removeProperty(final @NotNull String propertyName);
	void removeProperty(final @NotNull EntityProperty<?> property);
	@Override
	IMutableEntity copy();
}
