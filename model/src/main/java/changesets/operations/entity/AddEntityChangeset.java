package changesets.operations.entity;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.entity.IEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A changeset to add an entity to the game-world. TODO: Should we really allow/ask callers to construct the Entities?
 */
public class AddEntityChangeset implements Changeset {
	private final IEntity entity;

	public AddEntityChangeset(final @NotNull IEntity entity) {
		this.entity = entity;
	}

	@Override
	public Changeset invert() {
		return new RemoveEntityChangeset(entity);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		for (IEntity item : map.getAllEntities()) {
			if (Objects.equals(item.getId(), entity.getId())) {
				throw new PreconditionFailureException("Cannot add entity if another exists with same ID");
			}
		}
	}

	@Override
	public void applyInPlace(IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.addEntity(entity);
	}

	@Override
	public IMap apply(IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.addEntity(entity);
		return retval;
	}
}
