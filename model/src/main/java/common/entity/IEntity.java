package common.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for game-world entities.
 *
 * TODO: Add EntityListener support, that setters in implementations will notify when properties are changed.
 *
 * TODO: Maybe add a currentTick field, so the engine can find the entity with the earliest "current time" and process
 * its orders next.
 */
public interface IEntity {
	@NotNull Location getLocation();
	@NotNull EntityIdentifier getId();
	@NotNull String getType();
	boolean hasProperty(@NotNull String propertyName);
	@Nullable EntityProperty<?> getProperty(@NotNull String propertyName);
}
