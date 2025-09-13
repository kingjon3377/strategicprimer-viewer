package changesets.operations.regions;

import changesets.Changeset;
import changesets.PreconditionFailureException;
import common.map.IMap;
import common.map.IMutableMap;
import common.map.MapRegion;

import java.util.ArrayList;
import java.util.Collection;

public final class ReplaceRegionChangeset implements Changeset {
	private final MapRegion toRemove;
	private final MapRegion toAdd;

	public ReplaceRegionChangeset(final MapRegion toRemove, final MapRegion toAdd) {
		this.toRemove = toRemove;
		this.toAdd = toAdd;
	}

	@Override
	public Changeset invert() {
		return new ReplaceRegionChangeset(toAdd, toRemove);
	}

	private void checkPrecondition(final IMap map) throws PreconditionFailureException {
		final Collection<MapRegion> regions = new ArrayList<>(map.getRegions());
		if (!regions.remove(toRemove)) {
			throw new PreconditionFailureException("Region to remove must exist in the map");
		}
		regions.add(toAdd);
		if (!MapRegion.areRegionsValid(regions)) {
			throw new PreconditionFailureException(
					"Region to add must have unique ID and not overlap any existing region");
		}
	}

	@Override
	public void applyInPlace(final IMutableMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		map.replaceMapRegion(toRemove, toAdd);
	}

	@Override
	public IMap apply(final IMap map) throws PreconditionFailureException {
		checkPrecondition(map);
		final IMutableMap retval = (IMutableMap) map.copy();
		retval.replaceMapRegion(toRemove, toAdd);
		return retval;
	}

	@Override
	public String toString() {
		return "ReplaceRegionChangeset{toRemove=%d, toAdd=%d}".formatted(toRemove.getRegionId(), toAdd.getRegionId());
	}
}
