package model.viewer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import model.listeners.GraphicalParamsListener;
import model.listeners.MapChangeListener;
import model.listeners.SelectionChangeListener;
import model.listeners.VersionChangeListener;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;

import org.junit.Test;

import util.IteratorWrapper;

/**
 * A test of the PointIterator.
 *
 * @author Jonathan Lovelace
 *
 */
public class PointIteratorTest {
	/**
	 * The expectation for each test.
	 */
	private static final String EXPECTATION = "Iterator produced points in expected order";

	/**
	 * An IViewerModel implementation for use in testing.
	 */
	private static final class MockViewerModel implements IViewerModel {
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * The error message to throw when an unexpected method is called.
		 */
		private static final String MOCK_FAILURE_MSG = "Tests should never call this";

		/**
		 * Constructor.
		 *
		 * @param dimen the dimensions to return when asked.
		 * @param select the "selected tile" location, to return when asked.
		 */
		MockViewerModel(final MapDimensions dimen, final Point select) {
			dimensions = dimen;
			selection = select;
		}

		/**
		 * The "selected tile" location passed in at creation.
		 */
		private final Point selection;

		/**
		 * @param newMap ignored
		 * @param name ignored
		 */
		@Override
		public void setMap(final MapView newMap, final String name) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @return nothing
		 */
		@Override
		public MapView getMap() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * The dimensions we were told to return.
		 */
		private final MapDimensions dimensions;

		/**
		 * @return the dimensions given at creation
		 */
		@Override
		public MapDimensions getMapDimensions() {
			return dimensions;
		}

		/**
		 * @param point ignored
		 */
		@Override
		public void setSelection(final Point point) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param point ignored
		 * @return nothing
		 */
		@Override
		public Tile getTile(final Point point) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param dim ignored
		 */
		@Override
		public void setDimensions(final VisibleDimensions dim) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @return nothing
		 */
		@Override
		public VisibleDimensions getDimensions() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @return nothing
		 */
		@Override
		public String getMapFilename() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @return nothing
		 */
		@Override
		public int getZoomLevel() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * Should never be called.
		 */
		@Override
		public void zoomIn() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * Should never be called.
		 */
		@Override
		public void zoomOut() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * Should never be called.
		 */
		@Override
		public void resetZoom() {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @return the selected point
		 */
		@Override
		public Point getSelectedPoint() {
			return selection;
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void addMapChangeListener(final MapChangeListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void removeMapChangeListener(final MapChangeListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void addSelectionChangeListener(
				final SelectionChangeListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void removeSelectionChangeListener(
				final SelectionChangeListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void addVersionChangeListener(final VersionChangeListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void removeVersionChangeListener(final VersionChangeListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void addGraphicalParamsListener(
				final GraphicalParamsListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}

		/**
		 * @param list ignored
		 */
		@Override
		public void removeGraphicalParamsListener(
				final GraphicalParamsListener list) {
			throw new IllegalStateException(MOCK_FAILURE_MSG);
		}
	}

	/**
	 * Test without startFromSel, horizontally, forwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testFromBeginning() {
		final IViewerModel model = new MockViewerModel(new MapDimensions(3, 3,
				1), PointFactory.point(-1, -1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(model, false, true, true));
		final List<Point> expected = new ArrayList<>();
		expected.add(PointFactory.point(0, 0));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(1, 1));
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(2, 2));
		final List<Point> actual = new ArrayList<>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}

	/**
	 * Test with startFromSel, horizontally, forwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testFromSelection() {
		final IViewerModel model = new MockViewerModel(new MapDimensions(3, 3,
				1), PointFactory.point(1, 1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(model, true, true, true));
		final List<Point> expected = new ArrayList<>();
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(2, 2));
		expected.add(PointFactory.point(0, 0));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(1, 1));
		final List<Point> actual = new ArrayList<>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}

	/**
	 * Test working from the "selection" that the viewer starts with. And
	 * vertically, to exercise that part too.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testInitialSelection() {
		final IViewerModel model = new MockViewerModel(new MapDimensions(3, 3,
				1), PointFactory.point(-1, -1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(model, true, true, false));
		final List<Point> expected = new ArrayList<>();
		expected.add(PointFactory.point(0, 0));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(1, 1));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(2, 2));
		final List<Point> actual = new ArrayList<>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}

	/**
	 * Test searching vertically, forwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertical() {
		final IViewerModel model = new MockViewerModel(new MapDimensions(3, 3,
				1), PointFactory.point(-1, -1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(model, false, true, false));
		final List<Point> expected = new ArrayList<>();
		expected.add(PointFactory.point(0, 0));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(1, 1));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(2, 2));
		final List<Point> actual = new ArrayList<>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}

	/**
	 * Test searching backwards, horizontally.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testReverse() {
		final IViewerModel model = new MockViewerModel(new MapDimensions(3, 3,
				1), PointFactory.point(-1, -1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(model, false, false, true));
		final List<Point> expected = new ArrayList<>();
		expected.add(PointFactory.point(2, 2));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(1, 1));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(0, 0));
		final List<Point> actual = new ArrayList<>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}

	/**
	 * Test searching vertically, backwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVerticalReverse() {
		final IViewerModel model = new MockViewerModel(new MapDimensions(3, 3,
				1), PointFactory.point(-1, -1));
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(model, false, false, false));
		final List<Point> expected = new ArrayList<>();
		expected.add(PointFactory.point(2, 2));
		expected.add(PointFactory.point(1, 2));
		expected.add(PointFactory.point(0, 2));
		expected.add(PointFactory.point(2, 1));
		expected.add(PointFactory.point(1, 1));
		expected.add(PointFactory.point(0, 1));
		expected.add(PointFactory.point(2, 0));
		expected.add(PointFactory.point(1, 0));
		expected.add(PointFactory.point(0, 0));
		final List<Point> actual = new ArrayList<>();
		for (Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}
}
