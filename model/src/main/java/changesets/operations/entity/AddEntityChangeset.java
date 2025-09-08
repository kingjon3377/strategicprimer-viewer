package changesets.operations.entity;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.entity.IEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * A changeset to add an entity to the game-world. TODO: Should we really allow/ask callers to construct the Entities?
 */
public final class AddEntityChangeset implements Changeset {
	private final IEntity entity;

	public AddEntityChangeset(final @NonNull IEntity entity) {
		this.entity = entity;
	}

	@Override
	public @NonNull Changeset invert() {
		return new RemoveEntityChangeset(entity);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		for (final IEntity item : map.getAllEntities()) {
			if (Objects.equals(item.getId(), entity.getId())) {
				throw new PreconditionFailureException("Cannot add entity if another exists with same ID");
			}
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.addEntity(entity);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.addEntity(entity);
		return retval;
	}

	@Override
	public String toString() {
		return "AddEntityChangeset adding " + entity;
	}
}
