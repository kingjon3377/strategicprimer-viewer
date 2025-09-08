package changesets.operations.entity;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.entity.IEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jspecify.annotations.NonNull;

public final class RemoveEntityChangeset implements Changeset {
	private final IEntity entity;

	public RemoveEntityChangeset(final @NonNull IEntity entity) {
		this.entity = entity;
	}

	public @NonNull Changeset invert() {
		return new AddEntityChangeset(entity);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		if (map.getAllEntities().stream().noneMatch(entity::equals)) {
			throw new PreconditionFailureException("Cannot remove entity if not present in the map");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.removeEntity(entity);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.removeEntity(entity);
		return retval;
	}

	@Override
	public String toString() {
		return "RemoveEntityChangeset{entity=%s}".formatted(entity.getId());
	}
}
