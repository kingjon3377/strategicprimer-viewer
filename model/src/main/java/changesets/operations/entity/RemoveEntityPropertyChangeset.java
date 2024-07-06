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

public final class RemoveEntityPropertyChangeset<PropertyType> implements Changeset {
	private final EntityIdentifier id;
	private final String propertyName;
	private final PropertyType propertyValue;

	public RemoveEntityPropertyChangeset(final @NotNull EntityIdentifier id, final @NotNull String propertyName,
	                                     final @NotNull PropertyType propertyValue) {
		this.id = id;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}

	@Override
	public @NotNull Changeset invert() {
		return new AddEntityPropertyChangeset<>(id, propertyName, propertyValue);
	}

	private void checkPreconditions(final @NotNull IMap map) throws PreconditionFailureException {
		final IEntity matching = map.getEntity(id);
		if (Objects.isNull(matching)) {
			throw new PreconditionFailureException("Cannot remove property from nonexistent entity");
		} else if (!Objects.equals(matching.getProperty(propertyName), propertyValue)) {
			throw new PreconditionFailureException("Cannot remove property from entity when value doesn't match");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final EntityProperty<PropertyType> property = new EntityProperty<>(propertyName, propertyValue);
		if (matching instanceof final IMutableEntity entity) {
			entity.removeProperty(property);
		} else {
			// TODO: Maybe verify that we actually exclude one-and-only-one property?
			final IMutableEntity entity = new Entity(matching.getId(), matching.getLocation(), matching.getType());
			matching.getAllProperties().stream().filter(p -> !Objects.equals(p, property)).forEach(entity::setProperty);
			map.replaceEntity(matching, entity);
		}
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final EntityProperty<PropertyType> property = new EntityProperty<>(propertyName, propertyValue);
		final IMutableEntity entity = new Entity(matching.getId(), matching.getLocation(), matching.getType());
		matching.getAllProperties().stream().filter(p -> !Objects.equals(p, property)).forEach(entity::setProperty);
		retval.replaceEntity(matching, entity);
		return retval;
	}

	@Override
	public String toString() {
		return "RemoveEntityPropertyChangeset{id=%s, propertyName='%s', propertyValue=%s}".formatted(id, propertyName,
				propertyValue);
	}
}
