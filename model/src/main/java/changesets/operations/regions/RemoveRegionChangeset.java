package changesets.operations.regions;

import changesets.Changeset;
import changesets.ChangesetFailureException;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.MapRegion;
import org.jetbrains.annotations.NotNull;

/**
 * A changeset operation to remove a region from the map.
 */
public final class RemoveRegionChangeset implements Changeset {
	private final MapRegion region;

	public RemoveRegionChangeset(final @NotNull MapRegion region) {
		this.region = region;
	}

	@Override
	public @NotNull Changeset invert() {
		return new AddRegionChangeset(region);
	}

	private void checkPrecondition(final @NotNull IMap map) throws PreconditionFailureException {
		if (map.getRegions().stream().noneMatch(region::equals)) {
			throw new PreconditionFailureException("Cannot remove region that does not exist in the map");
		}
	}

	@Override
	public void applyInPlace(final @NotNull IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.removeMapRegion(region);
	}

	@Override
	public @NotNull IMap apply(final @NotNull IMap map) throws PreconditionFailureException {
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
