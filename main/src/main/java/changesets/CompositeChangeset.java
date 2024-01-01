package changesets;

import common.map.IMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Multiple changesets treated as a single unit.
 */
public class CompositeChangeset implements Changeset {
	private final List<Changeset> changesets;

	public CompositeChangeset(final @NotNull List<Changeset> changesets) {
		if (changesets.isEmpty()) {
			throw new IllegalArgumentException("Cannot have an empty composite changeset");
		}
		this.changesets = Collections.unmodifiableList(new ArrayList<>(changesets));
	}

	public CompositeChangeset(final @NotNull Changeset... changesets) {
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
	public void applyInPlace(IMap map) throws ChangesetFailureException {
		Deque<Changeset> alreadyApplied = new LinkedList<>();
		try {
			for (Changeset changeset : changesets) {
				changeset.applyInPlace(map); // FIXME: What if this fails?
				alreadyApplied.push(changeset);
			}
		} catch (ChangesetFailureException except) {
			try {
				for (Changeset changeset : alreadyApplied) {
					changeset.applyInPlace(map);
				}
			} catch (ChangesetFailureException inner) {
				final IllegalStateException toThrow =
					new IllegalStateException("Failed to roll back already-applied changesets", inner);
				toThrow.addSuppressed(except);
				throw toThrow;
			}
			throw except;
		}
	}

	@Override
	public IMap apply(IMap map) throws ChangesetFailureException {
		IMap retval = map;
		for (Changeset changeset : changesets) {
			retval = changeset.apply(retval);
		}
		return retval;
	}
}
