package model.exploration.old;

import java.util.Arrays;
import java.util.stream.Stream;
import model.map.MapDimensions;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.TileType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static util.NullCleaner.assertNotNull;

/**
 * A test case for TestExplorationRunner.
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
public final class TestExplorationRunner {
	/**
	 * The empty list.
	 */
	private static final Stream<TileFixture> EMPTY = assertNotNull(Stream.empty());
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_THREE = "test_three";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_TABLE_THREE = "test_table_three";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_TABLE_TWO = "test_table_two";
	/**
	 * Extracted constant, to fix a warning because it occurred three or more times.
	 */
	private static final String TEST_TABLE_ONE = "test_table_one";
	/**
	 * The object we're testing.
	 */
	private ExplorationRunner runner = new ExplorationRunner();

	/**
	 * Set up for the tests.
	 */
	@Before
	public void setUp() {
		runner = new ExplorationRunner();
	}

	/**
	 * Test the getPrimaryRock method.
	 *
	 * TODO: Use a mock object rather than a real object for the Table.
	 *
	 * @throws MissingTableException if the table is missing
	 */
	@Test
	public void testGetPrimaryRock() throws MissingTableException {
		runner.loadTable("major_rock", new ConstantTable("primary_rock_test"));
		assertThat("primary rock test",
				runner.getPrimaryRock(PointFactory.point(0, 0), TileType.Tundra, EMPTY,
						new MapDimensions(69, 88, 2)), equalTo("primary_rock_test"));
	}

	/**
	 * Test the getPrimaryTree method.
	 *
	 * TODO Use mock objects rather than real Tables.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetPrimaryTree() throws MissingTableException {
		runner.loadTable("boreal_major_tree", new ConstantTable("boreal_major_test"));
		runner.loadTable("temperate_major_tree",
				new ConstantTable("temperate_major_test"));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensions(69, 88, 2);
		assertThat("primary tree test for boreal forest",
				runner.getPrimaryTree(point, TileType.BorealForest, EMPTY, dimensions),
				equalTo("boreal_major_test"));
		assertThat("primary tree test for temperate forest",
				runner.getPrimaryTree(point, TileType.TemperateForest, EMPTY,
						dimensions),
				equalTo("temperate_major_test"));
	}

	/**
	 * Test that getPrimaryTree objects to non-forest tiles.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalGetPrimaryTree() throws MissingTableException {
		final Point point = PointFactory.point(0, 0);
		runner.getPrimaryTree(point, TileType.Tundra, EMPTY,
				new MapDimensions(69, 88, 2));
		fail("gave a primary tree for non-forest");
	}

	/**
	 * Test the consultTable method.
	 *
	 * TODO: Use a mock object rather than a real object for the Tables.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable("test_one"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable("test_two"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensions(69, 88, 2);
		assertThat("first table", runner.consultTable(TEST_TABLE_ONE,
				point, TileType.Tundra, EMPTY, dimensions), equalTo("test_one"));
		assertThat("second table", runner.consultTable(TEST_TABLE_TWO, point,
				TileType.Tundra, EMPTY, dimensions), equalTo("test_two"));
		assertThat("third table", runner.consultTable(TEST_TABLE_THREE,
				point, TileType.Tundra, EMPTY, dimensions), equalTo(TEST_THREE));
	}

	/**
	 * Test the recursiveConsultTable method: the one method under test whose correctness
	 * is non-obvious.
	 *
	 * TODO: Use a mock object rather than a real object for the Tables.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@Test
	public void testRecursiveConsultTable() throws MissingTableException {
		runner.loadTable(TEST_TABLE_ONE, new ConstantTable("( #test_table_two# )"));
		runner.loadTable(TEST_TABLE_TWO, new ConstantTable("( #test_table_three# )"));
		runner.loadTable(TEST_TABLE_THREE, new ConstantTable(TEST_THREE));
		runner.loadTable("test_table_four", new ConstantTable("_ #test_table_one"));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensions(69, 88, 2);
		assertThat(
				"two levels of recursion",
				runner.recursiveConsultTable(TEST_TABLE_ONE, point,
						TileType.Tundra, EMPTY, dimensions), equalTo("( ( test_three ) )"));
		assertThat(
				"one level of recursion",
				runner.recursiveConsultTable(TEST_TABLE_TWO, point,
						TileType.Tundra, EMPTY, dimensions), equalTo("( test_three )"));
		assertThat(
				"no recursion",
				runner.recursiveConsultTable(TEST_TABLE_THREE, point,
						TileType.Tundra, EMPTY, dimensions), equalTo(TEST_THREE));
		assertThat(
				"one-sided split",
				runner.recursiveConsultTable("test_table_four", point,
						TileType.Plains, EMPTY, dimensions),
				equalTo("_ ( ( test_three ) )"));
	}

	/**
	 * Test the defaultResults() method.
	 *
	 * @throws MissingTableException if a table is missing
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testDefaultResults() throws MissingTableException {
		runner.loadTable("major_rock", new ConstantTable("test_rock"));
		runner.loadTable("boreal_major_tree", new ConstantTable("boreal_tree"));
		runner.loadTable("temperate_major_tree", new ConstantTable("temperate_tree"));
		final Point point = PointFactory.point(0, 0);
		final MapDimensions dimensions = new MapDimensions(69, 88, 2);
		assertThat("defaultResults in non-forest",
				runner.defaultResults(point, TileType.Tundra, EMPTY, dimensions).trim(),
				equalTo("The primary rock type here is test_rock."));
		assertThat("defaultResults in boreal forest",
				runner.defaultResults(point, TileType.BorealForest, EMPTY, dimensions),
				equalTo(String.format("The primary rock type here is test_rock.%n"
						+ "The main kind of tree is boreal_tree.%n")));
		assertThat("defaultResults in temperate forest",
				runner.defaultResults(point, TileType.TemperateForest, EMPTY, dimensions),
				equalTo(String.format("The primary rock type here is test_rock.%n"
						+ "The main kind of tree is temperate_tree.%n")));
	}

	/**
	 * Test recursive checking. Note that the method returns true if the table in
	 * question, or one it references, does *not* exist.
	 */
	@Test
	public void testRecursiveCheck() {
		runner.loadTable("existent_table", new ConstantTable("exists"));
		assertThat("base case of non-existent table",
				Boolean.valueOf(
						runner.recursiveCheck("non-existent-table")),
				equalTo(Boolean.TRUE));
		assertThat("base case of existent table",
				Boolean.valueOf(runner.recursiveCheck("existent_table")), equalTo(Boolean.FALSE));
		runner.loadTable("referent_one", new ConstantTable("#existent_table#"));
		runner.loadTable("referent_two", new ConstantTable("( #existent_table# )"));
		runner.loadTable("referent_three", new QuadrantTable(1, assertNotNull(
				Arrays.asList("#referent_one#", "#referent_two#"))));
		assertThat("recursive case to exercise cache-hits",
				Boolean.valueOf(runner.recursiveCheck("referent_three")), equalTo(Boolean.FALSE));
		runner.loadTable("false_referent", new ConstantTable("#nonexistent#"));
		assertThat("reference to nonexistent table",
				Boolean.valueOf(runner.recursiveCheck("false_referent")), equalTo(Boolean.TRUE));
	}

	/**
	 * Test the recursiveCheck() method.
	 */
	@Test
	public void testGlobalRecursiveCheck() {
		assertThat("recursive check with no tables",
				Boolean.valueOf(runner.recursiveCheck()),
				equalTo(Boolean.FALSE));
		runner.loadTable("existent", new ConstantTable("true_table"));
		assertThat("recursive check with only valid tables",
				Boolean.valueOf(runner.recursiveCheck()), equalTo(Boolean.FALSE));
		runner.loadTable("false_ref", new ConstantTable("#false#"));
		assertThat("recursive check with an invalid table",
				Boolean.valueOf(runner.recursiveCheck()), equalTo(Boolean.TRUE));
	}

	/**
	 * @return a String representation of the class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestExplorationRunner";
	}
}
