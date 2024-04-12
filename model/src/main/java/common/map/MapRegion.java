package common.map;

import org.jetbrains.annotations.NotNull;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.Collection;

// TODO: Should this be a record instead? Would probably silence SPMap "feature envy" warnings about repeated access to
//  this class.
public interface MapRegion {
	/**
	 * TODO: Should regions really only be identified by a number? Should they have a name?
	 *
	 * @return the ID number for the region
	 */
	int getRegionId();

	/**
	 * This is primarily expected to be used in serialization.
	 *
	 * @return the shape defining the region's perimeter
	 */
	Polygon getShape();

	/**
	 * This is expected to be used for drawing the region and for enforcing the no-overlap invariant.
	 *
	 * @return the shape defining the region's area
	 */
	Area getArea();

	/**
	 * A map can only contain non-overlapping regions with mutually-unique IDs.
	 * @param regions a collection of regions
	 * @return whether they have mutually unique IDs, and none overlaps another.
	 */
	static boolean areRegionsValid(final @NotNull Collection<? extends MapRegion> regions) {
		for (final MapRegion first : regions) {
			for (final MapRegion second : regions) {
				if (first == second) {
					continue;
				} else if (first.getRegionId() == second.getRegionId()) {
					return false;
				}
				final Area area = new Area(first.getArea());
				area.intersect(second.getArea());
				if (!area.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}
}
