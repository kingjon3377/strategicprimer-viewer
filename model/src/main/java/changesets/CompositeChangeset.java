package changesets;

import common.map.IMap;
import common.map.IMutableMap;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Multiple changesets treated as a single unit.
 */
public final class CompositeChangeset implements Changeset {
	private final List<Changeset> changesets;

	@SuppressWarnings("TypeMayBeWeakened") // Order matters.
	public CompositeChangeset(final List<Changeset> changesets) {
		if (changesets.isEmpty()) {
			throw new IllegalArgumentException("Cannot have an empty composite changeset");
		}
		this.changesets = List.copyOf(changesets);
	}

	public CompositeChangeset(final Changeset... changesets) {
		if (changesets.length == 0) {
			throw new IllegalArgumentException("Cannot have an empty composite changeset");
		}
		this.changesets = List.of(changesets);
	}

	@Override
	public Changeset invert() {
		return new CompositeChangeset(changesets.reversed().stream().map(Changeset::invert).toList());
	}

	@Override
	public void applyInPlace(final IMutableMap map) throws ChangesetFailureException {
		final Deque<Changeset> alreadyApplied = new LinkedList<>();
		try {
			for (final Changeset changeset : changesets) {
				changeset.applyInPlace(map); // FIXME: What if this fails?
				alreadyApplied.push(changeset);
			}
		} catch (final ChangesetFailureException except) {
			try {
				for (final Changeset changeset : alreadyApplied) {
					changeset.applyInPlace(map);
				}
			} catch (final ChangesetFailureException inner) {
				final IllegalStateException toThrow =
						new IllegalStateException("Failed to roll back already-applied changesets", inner);
				toThrow.addSuppressed(except);
				throw toThrow;
			}
			throw except;
		}
	}

	@Override
	public IMap apply(final IMap map) throws ChangesetFailureException {
		IMap retval = map;
		for (final Changeset changeset : changesets) {
			retval = changeset.apply(retval);
		}
		return retval;
	}

	@Override
	public String toString() {
		return "CompositeChangeset with " + changesets.size() + " changesets";
	}
}
