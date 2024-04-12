package changesets.operations.entity;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.entity.Entity;
import common.entity.EntityIdentifier;
import common.entity.IEntity;
import common.entity.IMutableEntity;
import common.entity.Location;
import common.map.IMap;
import common.map.IMutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A changeset operation for moving an entity from one location to another. TODO: Should we check for no-ops in
 * constructor?
 */
public final class ChangeEntityLocationChangeset implements Changeset {
	private final EntityIdentifier id;
	private final Location fromLocation;
	private final Location toLocation;

	public ChangeEntityLocationChangeset(final @NotNull EntityIdentifier id, final @NotNull Location fromLocation,
										 final @NotNull Location toLocation) {
		this.id = id;
		this.fromLocation = fromLocation;
		this.toLocation = toLocation;
	}

	@Override
	public @NotNull Changeset invert() {
		return new ChangeEntityLocationChangeset(id, toLocation, fromLocation);
	}

	private void checkPreconditions(final @NotNull IMap map) throws PreconditionFailureException {
		final IEntity matching = map.getEntity(id);
		if (Objects.isNull(matching)) {
			throw new PreconditionFailureException("Entity to move must exist in the map");
		} else if (!matching.getLocation().equals(fromLocation)) {
			throw new PreconditionFailureException("Entity must be at 'from' location to move from there");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IEntity matching = map.getEntity(id);
		if (matching instanceof final IMutableEntity entity) {
			entity.setLocation(toLocation);
		} else {
			// TODO: Take properties in Entity constructor?
			final IMutableEntity replacement = new Entity(id, toLocation, matching.getType());
			matching.getAllProperties().forEach(replacement::setProperty);
			map.replaceEntity(matching, replacement);
		}
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws PreconditionFailureException {
		checkPreconditions(map);
		final IEntity matching = Objects.requireNonNull(map.getEntity(id));
		final IMutableMap retval = (IMutableMap) map.copy();
		// TODO: Take properties in Entity constructor?
		final IMutableEntity replacement = new Entity(id, toLocation, matching.getType());
		matching.getAllProperties().forEach(replacement::setProperty);
		retval.replaceEntity(matching, replacement);
		return retval;
	}

	@Override
	public String toString() {
		return "ChangeEntityLocationChangeset{id=%s, fromLocation=%s, toLocation=%s}".formatted(id, fromLocation,
				toLocation);
	}
}
