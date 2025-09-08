package common.entity;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Entity implements IMutableEntity {
	public Entity(final @NonNull EntityIdentifier id, final @NonNull Location location, final @NonNull String type) {
		this.location = location;
		this.id = id;
		if (type.isEmpty()) {
			throw new IllegalArgumentException("Entity type cannot be empty");
		}
		this.type = type;
	}

	private @NonNull Location location;
	private final @NonNull EntityIdentifier id;
	private final @NonNull String type;
	private final Map<String, EntityProperty<?>> properties = new HashMap<>();

	@Override
	public @NonNull Location getLocation() {
		return location;
	}

	public void setLocation(final @NonNull Location location) {
		this.location = location;
	}

	@Override
	public @NonNull EntityIdentifier getId() {
		return id;
	}

	@Override
	public @NonNull String getType() {
		return type;
	}

	@Override
	public boolean hasProperty(final @NonNull String propertyName) {
		return properties.containsKey(propertyName);
	}

	@Override
	public @Nullable EntityProperty<?> getProperty(final @NonNull String propertyName) {
		return properties.get(propertyName);
	}

	public void setProperty(final @NonNull EntityProperty<?> property) {
		properties.put(property.propertyName(), property);
	}

	public void removeProperty(final @NonNull String propertyName) {
		properties.remove(propertyName);
	}

	public void removeProperty(final @NonNull EntityProperty<?> property) {
		properties.remove(property.propertyName(), property);
	}

	@Override
	public @NonNull Entity copy() {
		final Entity retval = new Entity(id, location, type);
		properties.values().forEach(retval::setProperty);
		return retval;
	}

	@Override
	public @NonNull Collection<EntityProperty<?>> getAllProperties() {
		return Collections.unmodifiableCollection(properties.values());
	}

	@Override
	public String toString() {
		return "Entity{type='%s', id=%s, location=%s}".formatted(type, id, location);
	}
}
