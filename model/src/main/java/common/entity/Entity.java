package common.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Entity implements IEntity {
	public Entity(final @NotNull EntityIdentifier id, final @NotNull Location location, final @NotNull String type) {
		this.location = location;
		this.id = id;
		if (type.isEmpty()) {
			throw new IllegalArgumentException("Entity type cannot be empty");
		}
		this.type = type;
	}
	private @NotNull Location location;
	private final @NotNull EntityIdentifier id;
	private final @NotNull String type;
	private final Map<String, EntityProperty<?>> properties = new HashMap<>();
	@Override
	public final @NotNull Location getLocation() {
		return location;
	}

	public final void setLocation(final @NotNull Location location) {
		this.location = location;
	}
	@Override
	public final @NotNull EntityIdentifier getId() {
		return id;
	}
	@Override
	public final @NotNull String getType() {
		return type;
	}
	@Override
	public final boolean hasProperty(final @NotNull String propertyName) {
		return properties.containsKey(propertyName);
	}

	@Override
	public final @Nullable EntityProperty<?> getProperty(final @NotNull String propertyName) {
		return properties.get(propertyName);
	}

	public void setProperty(final @NotNull EntityProperty<?> property) {
		properties.put(property.getPropertyName(), property);
	}

	public void removeProperty(final @NotNull String propertyName) {
		properties.remove(propertyName);
	}

	public void removeProperty(final @NotNull EntityProperty<?> property) {
		properties.remove(property.getPropertyName(), property);
	}
}
