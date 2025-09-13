package common.entity;

/**
 * An interface for mutable operations on entities. TODO: We probably want to make entities actually immutable, with
 * "copy-with-change" operations.
 */
public interface IMutableEntity extends IEntity {
	void setLocation(Location location);

	void setProperty(final EntityProperty<?> property);

	void removeProperty(final String propertyName);

	void removeProperty(final EntityProperty<?> property);

	@Override
	IMutableEntity copy();
}
