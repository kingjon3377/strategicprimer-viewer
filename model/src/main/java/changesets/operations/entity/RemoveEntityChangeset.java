package changesets.operations.entity;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.entity.IEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jetbrains.annotations.NotNull;

public class RemoveEntityChangeset implements Changeset {
	private final IEntity entity;
	public RemoveEntityChangeset(final @NotNull IEntity entity) {
		this.entity = entity;
	}
	public Changeset invert() {
		return new AddEntityChangeset(entity);
	}
	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		if (map.getAllEntities().stream().noneMatch(entity::equals)) {
			throw new PreconditionFailureException("Cannot remove entity if not present in the map");
		}
	}

	@Override
	public void applyInPlace(IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.removeEntity(entity);
	}

	@Override
	public IMap apply(IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.removeEntity(entity);
		return retval;
	}
}
