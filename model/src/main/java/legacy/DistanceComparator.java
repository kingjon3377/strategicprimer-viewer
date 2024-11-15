package legacy;

import legacy.map.Point;

import java.io.Serializable;
import java.util.Comparator;

/**
 * An interface for the class that compares points based on distance from some fixed starting point, which also
 * provides helper methods to create a String representation of that distance.
 *
 * @author Jonathan Lovelace
 */
public interface DistanceComparator extends Comparator<Point>, Serializable {
	/**
	 * Returns a String describing how far a point is from "HQ", which the
	 * base point is presumed to be.
	 */
	String distanceString(Point point);

	/**
	 * Returns a String describing how far a point is from the base point,
	 * described with the given name.
	 */
	String distanceString(Point point, String name);
}
