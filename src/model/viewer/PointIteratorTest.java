package model.viewer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import util.IteratorWrapper;

/**
 * A test of the PointIterator.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PointIteratorTest {
	/**
	 * The expectation for each test.
	 */
	private static final String EXPECTATION =
			"Iterator produced points in expected order";

	/**
	 * Test without startFromSel, horizontally, forwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testFromBeginning() {
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(new MapDimensions(3, 3, 1), null, true, true));
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
		for (final Point point : iter) {
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
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(new MapDimensions(3, 3, 1),
						PointFactory.point(1, 1), true, true));
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
		for (final Point point : iter) {
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
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(new MapDimensions(3, 3,
						1), PointFactory.point(-1, -1), true, false));
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
		for (final Point point : iter) {
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
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(new MapDimensions(3, 3, 1), null, true, false));
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
		for (final Point point : iter) {
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
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(new MapDimensions(3, 3, 1), null, false, true));
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
		for (final Point point : iter) {
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
		final IteratorWrapper<Point> iter = new IteratorWrapper<>(
				new PointIterator(new MapDimensions(3, 3, 1), null, false,
						false));
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
		for (final Point point : iter) {
			actual.add(point);
		}
		assertEquals(EXPECTATION, expected, actual);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PointIteratorTest";
	}
}
