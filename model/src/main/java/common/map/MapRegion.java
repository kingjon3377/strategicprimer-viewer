package common.map;

import java.awt.Polygon;
import java.awt.geom.Area;

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
	 * @return the shape defining the region's perimeter
	 */
	Polygon getShape();

	/**
	 * This is expected to be used for drawing the region and for enforcing the no-overlap invariant.
	 * @return the shape defining the region's area
	 */
	Area getArea();
}
