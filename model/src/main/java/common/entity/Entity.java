package common.entity;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Entity implements IMutableEntity {
	public Entity(final EntityIdentifier id, final Location location, final String type,
	              final EntityProperty<?>... properties) {
		this.location = location;
		this.id = id;
		if (type.isEmpty()) {
			throw new IllegalArgumentException("Entity type cannot be empty");
		}
		this.type = type;
		for (final EntityProperty<?> property : properties) {
			setProperty(property);
		}
	}

	public Entity(final EntityIdentifier id, final Location location, final String type,
	              final Iterable<EntityProperty<?>> properties) {
		this.location = location;
		this.id = id;
		if (type.isEmpty()) {
			throw new IllegalArgumentException("Entity type cannot be empty");
		}
		this.type = type;
		properties.forEach(this::setProperty);
	}

	private Location location;
	private final EntityIdentifier id;
	private final String type;
	private final Map<String, EntityProperty<?>> properties = new HashMap<>();

	@Override
	public Location getLocation() {
		return location;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	public EntityIdentifier getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean hasProperty(final String propertyName) {
		return properties.containsKey(propertyName);
	}

	@Override
	public @Nullable EntityProperty<?> getProperty(final String propertyName) {
		return properties.get(propertyName);
	}

	public void setProperty(final EntityProperty<?> property) {
		properties.put(property.propertyName(), property);
	}

	public void removeProperty(final String propertyName) {
		properties.remove(propertyName);
	}

	public void removeProperty(final EntityProperty<?> property) {
		properties.remove(property.propertyName(), property);
	}

	@Override
	public Entity copy() {
		return new Entity(id, location, type, properties.values());
	}

	@Override
	public Collection<EntityProperty<?>> getAllProperties() {
		return Collections.unmodifiableCollection(properties.values());
	}

	@Override
	public String toString() {
		return "Entity{type='%s', id=%s, location=%s}".formatted(type, id, location);
	}
}
