package common.entity;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * An interface for game-world entities.
 *
 * TODO: Add EntityListener support, that setters in implementations will notify when properties are changed.
 *
 * TODO: Maybe add a currentTick field, so the engine can find the entity with the earliest "current time" and process
 * its orders next.
 */
public interface IEntity {
	@NonNull
	Location getLocation();

	@NonNull
	EntityIdentifier getId();

	@NonNull
	String getType();

	boolean hasProperty(@NonNull String propertyName);

	@Nullable
	EntityProperty<?> getProperty(@NonNull String propertyName);

	@NonNull
	Collection<EntityProperty<?>> getAllProperties();

	IEntity copy();
}
