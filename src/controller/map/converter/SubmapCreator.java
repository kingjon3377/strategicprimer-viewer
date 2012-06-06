package controller.map.converter;

import java.util.Random;

import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import controller.map.misc.IDFactory;

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
	 * @param size the size of each dimension of the submap it's being placed in
	 * @return the point in a submap it would be placed
	 */
	public Point generatePosition(final TileFixture fix, final int size) {
		final Random random = new Random(fix.getID());
		return PointFactory.point(random.nextInt(size), random.nextInt(size));
	}
	/**
	 * An enumerated type for quadrants.
	 */
	public enum Quadrant {
		/**
		 * Upper left.
		 */
		UpperLeft,
		/**
		 * Upper right.
		 */
		UpperRight,
		/**
		 * Lower left.
		 */
		LowerLeft,
		/**
		 * Lower right.
		 */
		LowerRight;
	}
	/**
	 * @param fix a fixture
	 * @param quadrant the quadrant of the submap we want it to end up in
	 * @param size the size of each dimension of the submap
	 * @return whether it'll end up in that quadrant
	 */
	public boolean checkPlacement(final TileFixture fix, final Quadrant quadrant, final int size) {
		final Point point = generatePosition(fix, size);
		return getQuadrant(point, size).equals(quadrant);
	}
	/**
	 * A Fixture subclass to test IDs with.
	 */
	private static class MockFixture implements TileFixture {
		/**
		 * The exception to throw when the mock escaped.
		 */
		private static final IllegalStateException MOCK_ESCAPED = new IllegalStateException("Mock escaped.");
		/**
		 * Never called.
		 * @return never
		 */
		@Override
		public String toXML() {
			throw MOCK_ESCAPED;
		}
		/**
		 * Never called.
		 * @return never
		 */
		@Override
		public String getFile() {
			throw MOCK_ESCAPED;
		}
		/**
		 * Never called.
		 * @param file ignored
		 */
		@Override
		public void setFile(final String file) {
			throw MOCK_ESCAPED;
		}
		/**
		 * Never called.
		 * @param obj ignored
		 * @return never
		 */
		@Override
		public int compareTo(final TileFixture obj) {
			throw MOCK_ESCAPED;
		}
		/**
		 * Never called.
		 * @return never
		 */
		@Override
		public TileFixture deepCopy() {
			throw MOCK_ESCAPED;
		}
		/**
		 * Never called.
		 * @return never
		 */
		@Override
		public int getZValue() {
			throw MOCK_ESCAPED;
		}
		/**
		 * @return the current ID
		 */
		@Override
		public int getID() {
			return id;
		}
		/**
		 * Never called.
		 * @param fix ignored
		 * @return never
		 */
		@Override
		public boolean equalsIgnoringID(final TileFixture fix) {
			throw MOCK_ESCAPED;
		}
		/**
		 * @param ident the new ID
		 */
		public void setID(final int ident) {
			id = ident;
		}
		/**
		 * The ID.
		 */
		private int id; // NOPMD
		/**
		 * Constructor.
		 * @param fix the fixture whose ID we start with
		 */
		MockFixture(final TileFixture fix) {
			id = fix.getID();
		}
		/**
		 * Never called.
		 * @param obj another object
		 * @return true iff this it's another MockFixture.
		 */
		@Override
		public boolean equals(final Object obj) {
			return obj instanceof MockFixture;
		}
		/**
		 * Never called.
		 * @return a hash value for the object
		 */
		@Override
		public int hashCode() {
			return -1;
		}
	}
	/**
	 * @param fix a fixture
	 * @param quadrant the quadrant of the submap we want it in
	 * @param size the size of each dimension of the submap
	 * @param factory an ID factory to ask for new IDs until we get a suitable one
	 * @return either its ID or a new one that'll put it in the quadrant we want
	 */
	public int getIdForQuadrant(final TileFixture fix, final Quadrant quadrant,
			final int size, final IDFactory factory) {
		final MockFixture fixture = new MockFixture(fix);
		while (!checkPlacement(fixture, quadrant, size)) {
			fixture.setID(factory.createID()); // NOPMD
		}
		return fixture.getID();
	}
	/**
	 * @param point a coordinate pair, within the bounds of the submap
	 * @param size the size of each dimension of the submap
	 * @return which quadrant of the submap it's in
	 */
	private static Quadrant getQuadrant(final Point point, final int size) {
		if (point.row() < 0 || point.row() > size || point.col() < 0 || point.col() > size) {
			throw new IllegalArgumentException("Position outside submap");
		} else if (point.row() < (size / 2)) {
			if (point.col() < (size / 2)) {
				return Quadrant.UpperLeft; // NOPMD
			} else {
				return Quadrant.UpperRight; // NOPMD
			}
		} else if (point.col() < (size / 2)) {
			return Quadrant.LowerLeft; // NOPMD
		} else {
			return Quadrant.LowerRight;
		}
	}
}
