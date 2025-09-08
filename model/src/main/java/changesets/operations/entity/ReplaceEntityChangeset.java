package changesets.operations.entity;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.entity.IEntity;
import common.map.IMap;
import common.map.IMutableMap;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * A changeset operation to replace one entity with another.
 */
public final class ReplaceEntityChangeset implements Changeset {
	private final @NonNull IEntity toRemove;
	private final @NonNull IEntity toAdd;

	public ReplaceEntityChangeset(final @NonNull IEntity toRemove, final @NonNull IEntity toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public @NonNull Changeset invert() {
		return new ReplaceEntityChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		boolean neverMet = true;
		for (final IEntity item : map.getAllEntities()) {
			if (item.equals(toRemove)) {
				neverMet = false;
			} else if (Objects.equals(item.getId(), toAdd.getId())) {
				throw new PreconditionFailureException("Cannot add entity with non-unique ID");
			}
		}
		if (neverMet) {
			throw new PreconditionFailureException("Cannot remove entity if not present in the map");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.replaceEntity(toRemove, toAdd);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replaceEntity(toRemove, toAdd);
		return retval;
	}

	@Override
	public String toString() {
		return "ReplaceEntityChangeset{toRemove=%s, toAdd=%s}".formatted(toRemove, toAdd);
	}
}
