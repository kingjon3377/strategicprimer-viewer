package changesets.operations.regions;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.MapRegion;
import org.jspecify.annotations.NonNull;

/**
 * A changeset operation to remove a region from the map.
 */
public final class RemoveRegionChangeset implements Changeset {
	private final MapRegion region;

	public RemoveRegionChangeset(final @NonNull MapRegion region) {
		this.region = region;
	}

	@Override
	public @NonNull Changeset invert() {
		return new AddRegionChangeset(region);
	}

	private void checkPrecondition(final @NonNull IMap map) throws PreconditionFailureException {
		if (map.getRegions().stream().noneMatch(region::equals)) {
			throw new PreconditionFailureException("Cannot remove region that does not exist in the map");
		}
	}

	@Override
	public void applyInPlace(final @NonNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.removeMapRegion(region);
	}

	@Override
	public @NonNull IMap apply(final @NonNull IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.removeMapRegion(region);
		return retval;
	}

	@Override
	public String toString() {
		return "RemoveRegionChangeset{region=%d}".formatted(region.getRegionId());
	}
}
