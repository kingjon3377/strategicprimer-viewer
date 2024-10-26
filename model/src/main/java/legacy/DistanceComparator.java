package legacy;

import legacy.map.Point;

import java.io.Serializable;
import java.util.Comparator;

/**
 * TODO: explain this class
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
