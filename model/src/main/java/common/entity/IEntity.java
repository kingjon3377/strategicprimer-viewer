package common.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for game-world entities.
 */
public interface IEntity {
	@NotNull Location getLocation();
	@NotNull EntityIdentifier getId();
	@NotNull String getType();
	boolean hasProperty(@NotNull String propertyName);
	// TODO: Instead of having getPropertyType() and getTypedProperty(), and making the Map use Object as the value type, have an EntityProperty<T> interface
	@Nullable Class<?> getPropertyType(@NotNull String propertyName);
	@Nullable Object getProperty(@NotNull String propertyName);
	@Nullable <T> T getTypedProperty(@NotNull String propertyName, @NotNull Class<T> type);
}
