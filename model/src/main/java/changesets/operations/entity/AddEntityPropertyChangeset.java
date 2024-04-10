package changesets.operations.entity;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.entity.Entity;
import common.entity.EntityIdentifier;
import common.entity.EntityProperty;
import common.entity.IEntity;
import common.entity.IMutableEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AddEntityPropertyChangeset<PropertyType> implements Changeset {
	private final EntityIdentifier id;
	private final String propertyName;
	private final PropertyType propertyValue;

	public AddEntityPropertyChangeset(final @NotNull EntityIdentifier id, final @NotNull String propertyName,
									  final @NotNull PropertyType propertyValue) {
		this.id = id;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}

	@Override
	public @NotNull Changeset invert() {
		return new RemoveEntityPropertyChangeset<>(id, propertyName, propertyValue);
	}

	private void checkPreconditions(final @NotNull IMap map) throws PreconditionFailureException {
		final IEntity entity = map.getEntity(id);
		if (Objects.isNull(entity)) {
			throw new PreconditionFailureException("Cannot add property to entity that does not exist in the map");
		} else if (entity.hasProperty(propertyName)) {
			throw new PreconditionFailureException("Cannot add already-existing property to entity");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPreconditions(map);
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final EntityProperty<PropertyType> property = new EntityProperty<>(propertyName, propertyValue);
		if (matching instanceof final IMutableEntity entity) {
			entity.setProperty(property);
		} else {
			final IMutableEntity entity = new Entity(matching.getId(), matching.getLocation(), matching.getType());
			matching.getAllProperties().forEach(entity::setProperty);
			entity.setProperty(property);
			map.replaceEntity(matching, entity);
		}
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPreconditions(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final EntityProperty<PropertyType> property = new EntityProperty<>(propertyName, propertyValue);
		final IMutableEntity entity = new Entity(matching.getId(), matching.getLocation(), matching.getType());
		matching.getAllProperties().forEach(entity::setProperty);
		entity.setProperty(property);
		retval.replaceEntity(matching, entity);
		return retval;
	}

	@Override
	public String toString() {
		return "AddEntityPropertyChangeset{id=%s, propertyName='%s', propertyValue=%s}".formatted(id, propertyName,
				propertyValue);
	}
}
