package changesets.operations.entity;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.entity.Entity;
import common.entity.EntityIdentifier;
import common.entity.EntityProperty;
import common.entity.IEntity;
import common.entity.IMutableEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.Predicate;

public final class ChangeEntityPropertyChangeset<FromType, ToType> implements Changeset {
	private final EntityIdentifier id;
	private final String propertyName;
	private final FromType oldValue;
	private final ToType newValue;

	public ChangeEntityPropertyChangeset(final EntityIdentifier id, final String propertyName, final FromType oldValue,
										 final ToType newValue) {
		this.id = id;
		this.propertyName = propertyName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	public @NonNull Changeset invert() {
		return new ChangeEntityPropertyChangeset<>(id, propertyName, newValue, oldValue);
	}

	private void checkPreconditions(final @NonNull IMap map) throws PreconditionFailureException {
		final IEntity entity = map.getEntity(id);
		if (Objects.isNull(entity)) {
			throw new PreconditionFailureException("Cannot change property of non-existent entity");
		} else if (!Objects.equals(entity.getProperty(propertyName), oldValue)) {
			throw new PreconditionFailureException("Cannot change property from different value than expected");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final EntityProperty<ToType> property = new EntityProperty<>(propertyName, newValue);
		if (matching instanceof final IMutableEntity entity) {
			entity.setProperty(property);
		} else {
			final EntityProperty<FromType> oldProperty = new EntityProperty<>(propertyName, oldValue);
			final IMutableEntity entity = new Entity(matching.getId(), matching.getLocation(), matching.getType(),
					matching.getAllProperties().stream().filter(Predicate.not(oldProperty::equals)).toList());
			entity.setProperty(property);
			map.replaceEntity(matching, entity);
		}
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final EntityProperty<FromType> oldProperty = new EntityProperty<>(propertyName, oldValue);
		final EntityProperty<ToType> newProperty = new EntityProperty<>(propertyName, newValue);
		final IMutableEntity entity = new Entity(matching.getId(), matching.getLocation(), matching.getType(),
				matching.getAllProperties().stream().filter(Predicate.not(oldProperty::equals)).toList());
		entity.setProperty(newProperty);
		retval.replaceEntity(matching, entity);
		return retval;
	}

	@Override
	public String toString() {
		return "ChangeEntityPropertyChangeset{id=%s, propertyName='%s', oldValue=%s, newValue=%s}".formatted(id,
				propertyName, oldValue, newValue);
	}
}
