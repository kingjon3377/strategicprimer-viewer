package common.entity;

import org.jetbrains.annotations.NotNull;

public final class PropertyImpl<T> implements EntityProperty<T> {
	private final @NotNull String propertyName;
	private final @NotNull T propertyValue;
	public PropertyImpl(final @NotNull String propertyName, final @NotNull T propertyValue) {
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	@Override
	public @NotNull String getPropertyName() {
		return propertyName;
	}

	@Override
	public @NotNull T getPropertyValue() {
		return propertyValue;
	}
}
