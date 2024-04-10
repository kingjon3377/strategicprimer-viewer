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

/**
 * A changeset to add a region to the map.
 */
public final class AddRegionChangeset implements Changeset {
	private final MapRegion region;

	public AddRegionChangeset(final @NotNull MapRegion region) {
		this.region = region;
	}

	@Override
	public @NotNull Changeset invert() {
		return new RemoveRegionChangeset(region);
	}

	private void checkPrecondition(final @NotNull IMap map) throws PreconditionFailureException {
		final Collection<MapRegion> regions = new ArrayList<>(map.getRegions());
		regions.add(region);
		if (!IMap.areRegionsValid(regions)) {
			throw new PreconditionFailureException("Regions must have unique IDs and cannot overlap");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		map.addMapRegion(region);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws ChangesetFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.addMapRegion(region);
		return retval;
	}

	@Override
	public String toString() {
		return "AddRegionChangeset{region=%d}".formatted(region.getRegionId());
	}
}
