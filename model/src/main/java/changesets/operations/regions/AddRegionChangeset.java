package changesets.operations.regions;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.MapRegion;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A changeset to add a region to the map.
 */
public final class AddRegionChangeset implements Changeset {
	private final MapRegion region;

	public AddRegionChangeset(final @NonNull MapRegion region) {
		this.region = region;
	}

	@Override
	public @NonNull Changeset invert() {
		return new RemoveRegionChangeset(region);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		final Collection<MapRegion> regions = new ArrayList<>(map.getRegions());
		regions.add(region);
		if (!MapRegion.areRegionsValid(regions)) {
			throw new PreconditionFailureException("Regions must have unique IDs and cannot overlap");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.addMapRegion(region);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
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
