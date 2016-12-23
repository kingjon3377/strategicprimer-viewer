package model.viewer;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import org.junit.Test;
import util.IteratorWrapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * A test of the PointIterator.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
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
		final Collection<Point> expected =
				Arrays.asList(PointFactory.point(0, 0), PointFactory.point(0, 1),
						PointFactory.point(0, 2), PointFactory.point(1, 0),
						PointFactory.point(1, 1), PointFactory.point(1, 2),
						PointFactory.point(2, 0), PointFactory.point(2, 1),
						PointFactory.point(2, 2));
		final Iterable<Point> iter =
				new IteratorWrapper<>(new PointIterator(new MapDimensions(3, 3, 1), null,
															   true, true));
		final Collection<Point> actual = StreamSupport.stream(iter.spliterator(), false)
												 .collect(Collectors.toList());
		assertThat(EXPECTATION, actual, equalTo(expected));
	}

	/**
	 * Test with startFromSel, horizontally, forwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testFromSelection() {
		final Iterable<Point> iter =
				new IteratorWrapper<>(new PointIterator(new MapDimensions(3, 3, 1),
															   PointFactory.point(1, 1),
															   true, true));
		final Collection<Point> expected =
				Arrays.asList(PointFactory.point(1, 2), PointFactory.point(2, 0),
						PointFactory.point(2, 1), PointFactory.point(2, 2),
						PointFactory.point(0, 0), PointFactory.point(0, 1),
						PointFactory.point(0, 2), PointFactory.point(1, 0),
						PointFactory.point(1, 1));
		final Collection<Point> actual = StreamSupport.stream(iter.spliterator(), false)
												 .collect(Collectors.toList());
		assertThat(EXPECTATION, actual, equalTo(expected));
	}

	/**
	 * Test working from the "selection" that the viewer starts with. And vertically, to
	 * exercise that part too.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testInitialSelection() {
		final Iterable<Point> iter =
				new IteratorWrapper<>(new PointIterator(new MapDimensions(3, 3, 1),
															   PointFactory.INVALID_POINT,
															   true, false));
		final Collection<Point> expected =
				Arrays.asList(PointFactory.point(0, 0), PointFactory.point(1, 0),
						PointFactory.point(2, 0), PointFactory.point(0, 1),
						PointFactory.point(1, 1), PointFactory.point(2, 1),
						PointFactory.point(0, 2), PointFactory.point(1, 2),
						PointFactory.point(2, 2));
		final Collection<Point> actual = StreamSupport.stream(iter.spliterator(), false)
												 .collect(Collectors.toList());
		assertThat(EXPECTATION, actual, equalTo(expected));
	}

	/**
	 * Test searching vertically, forwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertical() {
		final Collection<Point> expected =
				Arrays.asList(PointFactory.point(0, 0), PointFactory.point(1, 0),
						PointFactory.point(2, 0), PointFactory.point(0, 1),
						PointFactory.point(1, 1), PointFactory.point(2, 1),
						PointFactory.point(0, 2), PointFactory.point(1, 2),
						PointFactory.point(2, 2));
		final Iterable<Point> iter =
				new IteratorWrapper<>(new PointIterator(new MapDimensions(3, 3, 1), null,
															   true, false));
		final Collection<Point> actual = StreamSupport.stream(iter.spliterator(), false)
												 .collect(Collectors.toList());
		assertThat(EXPECTATION, actual, equalTo(expected));
	}

	/**
	 * Test searching backwards, horizontally.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testReverse() {
		final Collection<Point> expected =
				Arrays.asList(PointFactory.point(2, 2), PointFactory.point(2, 1),
						PointFactory.point(2, 0), PointFactory.point(1, 2),
						PointFactory.point(1, 1), PointFactory.point(1, 0),
						PointFactory.point(0, 2), PointFactory.point(0, 1),
						PointFactory.point(0, 0));
		final Iterable<Point> iter =
				new IteratorWrapper<>(new PointIterator(new MapDimensions(3, 3, 1), null,
															   false, true));
		final Collection<Point> actual = StreamSupport.stream(iter.spliterator(), false)
												 .collect(Collectors.toList());
		assertThat(EXPECTATION, actual, equalTo(expected));
	}

	/**
	 * Test searching vertically, backwards.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVerticalReverse() {
		final Collection<Point> expected =
				Arrays.asList(PointFactory.point(2, 2), PointFactory.point(1, 2),
						PointFactory.point(0, 2), PointFactory.point(2, 1),
						PointFactory.point(1, 1), PointFactory.point(0, 1),
						PointFactory.point(2, 0), PointFactory.point(1, 0),
						PointFactory.point(0, 0));
		final Iterable<Point> iter =
				new IteratorWrapper<>(new PointIterator(new MapDimensions(3, 3, 1), null,
															   false, false));
		final Collection<Point> actual = StreamSupport.stream(iter.spliterator(), false)
												 .collect(Collectors.toList());
		assertThat(EXPECTATION, actual, equalTo(expected));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "PointIteratorTest";
	}
}
