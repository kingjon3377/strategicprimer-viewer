package common.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for game-world entities.
 */
public interface IEntity {
	@NotNull Location getLocation();
	@NotNull EntityIdentifier getId();
	boolean hasProperty(@NotNull String propertyName);
	@Nullable Class<?> getPropertyType(@NotNull String propertyName);
	@Nullable Object getProperty(@NotNull String propertyName);
	@Nullable <T> T getTypedProperty(@NotNull String propertyName, @NotNull Class<T> type);
}
