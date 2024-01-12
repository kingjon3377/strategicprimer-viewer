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
 * A changeset operation to replace one entity with another.
 */
public final class ReplaceEntityChangeset implements Changeset {
	private final @NotNull IEntity toRemove;
	private final @NotNull IEntity toAdd;

	public ReplaceEntityChangeset(final @NotNull IEntity toRemove, final @NotNull IEntity toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public @NotNull Changeset invert() {
		return new ReplaceEntityChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		boolean met = false;
		for (final IEntity item : map.getAllEntities()) {
			if (item.equals(toRemove)) {
				met = true;
			} else if (Objects.equals(item.getId(), toAdd.getId())) {
				throw new PreconditionFailureException("Cannot add entity with non-unique ID");
			}
		}
		if (!met) {
			throw new PreconditionFailureException("Cannot remove entity if not present in the map");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.replaceEntity(toRemove, toAdd);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replaceEntity(toRemove, toAdd);
		return retval;
	}
}
