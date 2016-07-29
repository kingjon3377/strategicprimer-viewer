package controller.exploration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import model.exploration.old.EncounterTable;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.TileType;
import org.junit.Test;
import util.NullCleaner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestTableLoader {
	/**
	 * The empty list.
	 */
	private static final List<TileFixture> EMPTY =
			NullCleaner.assertNotNull(Collections.emptyList());
	/**
	 * "one".
	 */
	private static final String ONE_STRING = "one";
	/**
	 * The string "static-method", used in an annotation on each method.
	 */
	private static final String ST_MET = "static-method";

	/**
	 * Test method for loading quadrant tables.
	 *
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadQuadrantTable() throws IOException {
		try (final BufferedReader reader = new BufferedReader(new StringReader
																	(String.format("quadrant%n2%none%ntwo%nthree%nfour%nfive%nsix")))) {
			final EncounterTable result = TableLoader.loadTableFromStream(reader);
			final Point point = PointFactory.point(0, 0);
			assertThat("loading quadrant table",
					result.generateEvent(point, TileType.Tundra, EMPTY),
					equalTo(ONE_STRING));
		}
		try (final BufferedReader readerTwo = new BufferedReader(new StringReader
																		("quadrant"))) {
			TableLoader.loadTableFromStream(readerTwo);
			fail("Didn't object to quadrant table without number of rows");
		} catch (final IOException except) {
			assertThat("Objecting to quadrant table without number of rows",
					except.getMessage(),
					equalTo("File doesn't start with the number of rows of quadrants"));
		}
	}

	/**
	 * Test method for loading random tables.
	 *
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadRandomTable() throws IOException {
		try (final BufferedReader reader = new BufferedReader(new StringReader(
				String.format("random%n0 one%n99 two")))) {
			final EncounterTable result = TableLoader.loadTableFromStream(reader);
			final Point point = PointFactory.point(30, 30);
			assertThat("loading random table",
					result.generateEvent(point, TileType.Tundra, EMPTY),
					equalTo(ONE_STRING));
		}
	}

	/**
	 * Test method for loading terrain tables. .
	 *
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadTerrainTable() throws IOException {
		try (final BufferedReader reader = new BufferedReader(new StringReader(
																	String.format("terrain%ntundra " +
																			"one%nplains two%nocean three")))) {
			final EncounterTable result = TableLoader.loadTableFromStream(reader);
			final Point firstPoint = PointFactory.point(30, 30);
			assertThat("loading terrain table: tundra",
					result.generateEvent(firstPoint, TileType.Tundra, EMPTY),
					equalTo(ONE_STRING));
			final Point secondPoint = PointFactory.point(15, 15);
			assertThat("loading terrain table: plains",
					result.generateEvent(secondPoint, TileType.Plains, EMPTY),
					equalTo("two"));
			assertThat("loading terrain table: ocean",
					result.generateEvent(secondPoint, TileType.Ocean, EMPTY),
					equalTo("three"));
		}
	}

	/**
	 * Test method for loading constant tables. .
	 *
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testLoadConstantTable() throws IOException {
		try (BufferedReader reader = new BufferedReader(new StringReader
																(String.format("constant%none")))) {
			final EncounterTable result = TableLoader.loadTableFromStream(reader);
			final Point point = PointFactory.point(10, 5);
			assertThat("loading constant table: first test",
					result.generateEvent(point, TileType.Plains, EMPTY),
					equalTo(ONE_STRING));
		}
	}

	/**
	 * Test the bad-input logic in loading tables from streams.
	 *
	 * @throws IOException on I/O error in the test or in cleaning up after it.
	 */
	@SuppressWarnings(ST_MET)
	@Test
	public void testInvalidInput() throws IOException {
		try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
			TableLoader.loadTableFromStream(reader);
			fail("Accepted empty input");
		} catch (final IOException except) {
			assertThat("Objects to empty input",
					except.getMessage(),
					equalTo("File doesn't start by specifying which kind of table."));
		}
		try (BufferedReader reader = new BufferedReader(new StringReader
																(String.format("2%ninvalidData%ninvalidData")))) {
			TableLoader.loadTableFromStream(reader);
			fail("Accepted table without header");
		} catch (final IllegalArgumentException except) {
			assertThat("Table without header", except.getMessage(),
					equalTo("unknown table type"));
		}
	}

	/**
	 * @return a String representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestTableLoader";
	}
}
