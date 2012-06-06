package controller.map.converter;

import java.util.Random;

import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;

/**
 * A helper class to create submaps---for the converter, or for
 * procedural-content purposes.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SubmapCreator {
	/**
	 * @param fix a fixture
	 * @param size the size of the submap it's being placed in
	 * @return the point in a submap it would be placed
	 */
	public Point generatePosition(final TileFixture fix, final int size) {
		final Random random = new Random(fix.getID());
		return PointFactory.point(random.nextInt(size), random.nextInt(size));
	}
}
