package changesets.operations.regions;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.MapRegion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public final class ReplaceRegionChangeset implements Changeset {
	private final @NotNull MapRegion toRemove;
	private final @NotNull MapRegion toAdd;

	public ReplaceRegionChangeset(final @NotNull MapRegion toRemove, final @NotNull MapRegion toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public @NotNull Changeset invert() {
		return new ReplaceRegionChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final @NotNull IMap map) throws ChangesetFailureException {
		final Collection<MapRegion> regions = new ArrayList<>(map.getRegions());
		if (!regions.remove(toRemove)) {
			throw new PreconditionFailureException("Region to remove must exist in the map");
		}
		regions.add(toAdd);
		if (!IMap.areRegionsValid(regions)) {
			throw new PreconditionFailureException(
				"Region to add must have unique ID and not overlap any existing region");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.replaceMapRegion(toRemove, toAdd);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replaceMapRegion(toRemove, toAdd);
		return retval;
	}
}
