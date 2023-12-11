package common.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Entity implements IEntity {
	public Entity(final @NotNull EntityIdentifier id, final @NotNull Location location) {
		this.location = location;
		this.id = id;
	}
	private @NotNull Location location;
	private final @NotNull EntityIdentifier id;
	private final Map<String, Object> properties = new HashMap<>();
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
	public final boolean hasProperty(final @NotNull String propertyName) {
		return properties.containsKey(propertyName);
	}

	@Override
	public final @Nullable Class<?> getPropertyType(final @NotNull String propertyName) {
		final Object property = properties.get(propertyName);
		if (null == property) {
			return null;
		} else {
			return property.getClass();
		}
	}

	@Override
	public final @Nullable Object getProperty(final @NotNull String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public final <T> @Nullable T getTypedProperty(final @NotNull String propertyName, final @NotNull Class<T> type) {
		final Object retval = properties.get(propertyName);
		if (type.isInstance(retval)) {
			return type.cast(retval);
		} else {
			return null;
		}
	}

	public void setProperty(final @NotNull String propertyName, final @Nullable Object value) {
		if (value == null) {
			properties.remove(propertyName);
		} else {
			properties.put(propertyName, value);
		}
	}
}
