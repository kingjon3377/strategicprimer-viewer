package common.entity;

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
	Location getLocation();

	EntityIdentifier getId();

	String getType();

	boolean hasProperty(String propertyName);

	@Nullable
	EntityProperty<?> getProperty(String propertyName);

	Collection<EntityProperty<?>> getAllProperties();

	IEntity copy();
}
