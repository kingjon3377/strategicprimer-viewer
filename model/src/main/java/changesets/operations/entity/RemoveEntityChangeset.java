package changesets.operations.entity;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.entity.IEntity;
import common.map.IMap;
import common.map.IMutableMap;

public final class RemoveEntityChangeset implements Changeset {
	private final IEntity entity;

	public RemoveEntityChangeset(final IEntity entity) {
		this.entity = entity;
	}

	public Changeset invert() {
		return new AddEntityChangeset(entity);
	}

	private void checkPrecondition(final IMap map) throws PreconditionFailureException {
		if (map.getAllEntities().stream().noneMatch(entity::equals)) {
			throw new PreconditionFailureException("Cannot remove entity if not present in the map");
		}
	}

	@Override
	public void applyInPlace(final IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.removeEntity(entity);
	}

	@Override
	public IMap apply(final IMap map) throws PreconditionFailureException {
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
