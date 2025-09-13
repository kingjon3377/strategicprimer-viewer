package changesets.operations.entity;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.entity.Entity;
import common.entity.EntityIdentifier;
import common.entity.IEntity;
import common.entity.IMutableEntity;
import common.entity.Location;
import common.map.IMap;
import common.map.IMutableMap;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * A changeset operation for moving an entity from one location to another. TODO: Should we check for no-ops in
 * constructor?
 */
public final class ChangeEntityLocationChangeset implements Changeset {
	private final EntityIdentifier id;
	private final Location fromLocation;
	private final Location toLocation;

	public ChangeEntityLocationChangeset(final @NonNull EntityIdentifier id, final @NonNull Location fromLocation,
										 final @NonNull Location toLocation) {
		this.id = id;
		this.fromLocation = fromLocation;
		this.toLocation = toLocation;
	}

	@Override
	public @NonNull Changeset invert() {
		return new ChangeEntityLocationChangeset(id, toLocation, fromLocation);
	}

	private void checkPreconditions(final @NonNull IMap map) throws PreconditionFailureException {
		final IEntity matching = map.getEntity(id);
		if (Objects.isNull(matching)) {
			throw new PreconditionFailureException("Entity to move must exist in the map");
		} else if (!matching.getLocation().equals(fromLocation)) {
			throw new PreconditionFailureException("Entity must be at 'from' location to move from there");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IEntity matching = map.getEntity(id);
		if (matching instanceof final IMutableEntity entity) {
			entity.setLocation(toLocation);
		} else if (Objects.nonNull(matching)) {
			final IMutableEntity replacement = new Entity(id, toLocation, matching.getType(),
					matching.getAllProperties());
			map.replaceEntity(matching, replacement);
		}
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final IMutableMap retval = (IMutableMap) map.copy();
		final IMutableEntity replacement = new Entity(id, toLocation, matching.getType(), matching.getAllProperties());
		retval.replaceEntity(matching, replacement);
		return retval;
	}

	@Override
	public String toString() {
		return "ChangeEntityLocationChangeset{id=%s, fromLocation=%s, toLocation=%s}".formatted(id, fromLocation,
				toLocation);
	}
}
