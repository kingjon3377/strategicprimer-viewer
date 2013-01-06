package model.viewer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import model.map.MapDimensions;
import model.map.MapView;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;

import org.junit.Test;

import util.IteratorWrapper;
/**
 * A test of the PointIterator.
 * @author Jonathan Lovelace
 *
 */
public class PointIteratorTest {
	/**
	 * Test without startFromSel.
	 */
	@Test
	public void testFromBeginning() {
		final MapView map = new MapView(new SPMap(new MapDimensions(3, 3, 1)), 0, 0);
		final MapModel model = new MapModel(map);
		final IteratorWrapper<Point> iter = new IteratorWrapper<Point>(new PointIterator(model, false));
		final List<Point> expected = new ArrayList<Point>();
		expected.add(PointFactory.point(0, 0));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(1, 1));
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(2, 2));
		final List<Point> actual = new ArrayList<Point>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals("Iterator produced points in expected order", expected, actual);
	}
	/**
	 * Test with startFromSel.
	 */
	@Test
	public void testFromSelection() {
		final MapView map = new MapView(new SPMap(new MapDimensions(3, 3, 1)), 0, 0);
		final MapModel model = new MapModel(map);
		model.setSelection(PointFactory.point(1, 1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<Point>(new PointIterator(model, true));
		final List<Point> expected = new ArrayList<Point>();
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(2, 2));
		expected.add(PointFactory.point(0, 0));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(1, 1));
		final List<Point> actual = new ArrayList<Point>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals("Iterator produced points in expected order", expected, actual);
	}
}
