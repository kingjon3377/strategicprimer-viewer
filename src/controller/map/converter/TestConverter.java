package controller.map.converter;

import controller.map.cxml.CompactXMLWriter;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.MapReaderAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMapNG;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import org.junit.Test;
import util.IteratorWrapper;
import util.Warning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The test case for map-conversion code paths.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class TestConverter {
	/**
	 * Extracted constant: the type of rock used in the test-data tables.
	 */
	public static final String ROCK_TYPE = "rock1";
	/**
	 * Extracted constant: the type of tree used for boreal forests in the test-data
	 * tables.
	 */
	public static final String BOREAL_TREE = "btree1";
	/**
	 * Extracted constant: the type of field used in the test-data tables.
	 */
	public static final String FIELD_TYPE = "grain1";
	/**
	 * Extracted constant: the type of tree used for temperate forests in the test-data
	 * tables.
	 */
	public static final String TEMP_TREE = "ttree1";

	/**
	 * Test conversion.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(),
								   0);
		final Point pointOne = PointFactory.point(0, 0);
		final Animal fixture = new Animal("animal", false, true,
												 "domesticated", 1);
		start.addFixture(pointOne, fixture);
		final Point pointTwo = PointFactory.point(0, 1);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
																2);
		start.addFixture(pointTwo, fixtureTwo);
		final Point pointThree = PointFactory.point(1, 0);
		final IUnit fixtureThree = new Unit(new Player(0, "A. Player"),
												   "legion", "eagles", 3);
		start.addFixture(pointThree, fixtureThree);
		final Point pointFour = PointFactory.point(1, 1);
		final Fortress fixtureFour = new Fortress(new Player(1, "B. Player"),
														 "HQ", 4);
		start.addFixture(pointFour, fixtureFour);
		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertTrue("Combined tile should contain fixtures from tile one",
				doesStreamContain(converted.streamOtherFixtures(zeroPoint), fixture));
		assertTrue(
				"Combined tile should contain fixtures from tile two",
				doesStreamContain(converted.streamOtherFixtures(zeroPoint),
						fixtureTwo));
		assertTrue(
				"Combined tile should contain fixtures from tile three",
				doesStreamContain(converted.streamOtherFixtures(zeroPoint),
						fixtureThree));
		assertTrue(
				"Combined tile should contain fixtures from tile four",
				doesStreamContain(converted.streamOtherFixtures(zeroPoint),
						fixtureFour));
	}
	/**
	 * Test version-1 to version-2 conversion.
	 *
	 * TODO: Make the converter (and the tests) pass the map size to the encounter tables.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testOneToTwoConversion() throws IOException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		original.setBaseTerrain(PointFactory.point(0, 0), TileType.BorealForest);
		original.setBaseTerrain(PointFactory.point(0, 1), TileType.TemperateForest);
		original.setBaseTerrain(PointFactory.point(1, 0), TileType.Desert);
		original.setBaseTerrain(PointFactory.point(1, 1), TileType.Plains);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.setBaseTerrain(PointFactory.point(0, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 0), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 1), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 1), new Meadow(FIELD_TYPE, true, true, 12,
				                                                         FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(0, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 2), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 2), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 3), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 3), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 3), new Grove(true, true, "fruit1", 9));
		converted.setBaseTerrain(PointFactory.point(0, 4), TileType.Plains);
		converted.setGround(PointFactory.point(0, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 4), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 5), TileType.Plains);
		converted.setGround(PointFactory.point(0, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 6), TileType.Plains);
		converted.setGround(PointFactory.point(0, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 6), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 7), TileType.Plains);
		converted.setGround(PointFactory.point(0, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 7), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 0), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 1), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(1, 1),
				new Meadow(FIELD_TYPE, true, true, 6, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(1, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 2), new Forest(BOREAL_TREE, false));

		final Player noPlayer = new Player(-1, "");

		converted.addFixture(PointFactory.point(1, 2),
				new Village(TownStatus.Active, "", 0, noPlayer, "dwarf"));

		final String maxIterWarn = "FIXME: A fixture here was force-added after " +
				                           "MAX_ITER";

		converted.addFixture(PointFactory.point(1, 2), new TextFixture(maxIterWarn, 10));
		converted.addFixture(PointFactory.point(1, 2), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 3), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 3), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 4), TileType.Plains);
		converted.setGround(PointFactory.point(1, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 4), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 5), TileType.Plains);
		converted.setGround(PointFactory.point(1, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 6), TileType.Plains);
		converted.setGround(PointFactory.point(1, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 6), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 7), TileType.Plains);
		converted.setGround(PointFactory.point(1, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 7), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(1, 7),
				new Meadow(FIELD_TYPE, true, true, 4, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(2, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 0), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 1), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 2), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(2, 2), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 3), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 3), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(2, 3),
				new Meadow(FIELD_TYPE, true, true, 21, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(2, 4), TileType.Plains);
		converted.setGround(PointFactory.point(2, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 4), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 5), TileType.Plains);
		converted.setGround(PointFactory.point(2, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 6), TileType.Plains);
		converted.setGround(PointFactory.point(2, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 6), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(2, 6),
				new Village(TownStatus.Active, "", 1, noPlayer, "human"));
		converted.addFixture(PointFactory.point(2, 6), new TextFixture(maxIterWarn, 10));
		converted.setBaseTerrain(PointFactory.point(2, 7), TileType.Plains);
		converted.setGround(PointFactory.point(2, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 7), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(2, 7),
				new Meadow(FIELD_TYPE, true, true, 20, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(3, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 0), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 1), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 2), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 3), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 3), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 4), TileType.Plains);
		converted.setGround(PointFactory.point(3, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 4), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 5), TileType.Plains);
		converted.setGround(PointFactory.point(3, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 6), TileType.Plains);
		converted.setGround(PointFactory.point(3, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 6), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(3, 6),
				new Meadow(FIELD_TYPE, true, true, 7, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(3, 7), TileType.Plains);
		converted.setGround(PointFactory.point(3, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 7), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(3, 7),
				new Grove(true, true, "fruit1", 19));

		converted.setBaseTerrain(PointFactory.point(4, 0), TileType.Desert);
		converted.setGround(PointFactory.point(4, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 1), TileType.Desert);
		converted.setGround(PointFactory.point(4, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 1),
				new Meadow(FIELD_TYPE, true, true, 10, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(4, 2), TileType.Desert);
		converted.setGround(PointFactory.point(4, 2), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(4, 2), new Grove(true, true, "fruit1", 5));
		converted.setBaseTerrain(PointFactory.point(4, 3), TileType.Desert);
		converted.setGround(PointFactory.point(4, 3), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(4, 3), new Grove(true, true, "fruit1", 18));
		converted.setBaseTerrain(PointFactory.point(4, 4), TileType.Plains);
		converted.setGround(PointFactory.point(4, 4), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(4, 4), new Grove(true, true, "fruit1", 14));
		converted.setBaseTerrain(PointFactory.point(4, 5), TileType.Plains);
		converted.setGround(PointFactory.point(4, 5), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(4, 5), new Grove(true, true, "fruit1", 16));
		converted.setBaseTerrain(PointFactory.point(4, 6), TileType.Plains);
		converted.setGround(PointFactory.point(4, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 6),
				new Meadow(FIELD_TYPE, true, true, 8, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(4, 7), TileType.Plains);
		converted.setGround(PointFactory.point(4, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 0), TileType.Plains);
		converted.setGround(PointFactory.point(5, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 1), TileType.Desert);
		converted.setGround(PointFactory.point(5, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 2), TileType.Plains);
		converted.setGround(PointFactory.point(5, 2), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 2),
				new Village(TownStatus.Active, "", 2, noPlayer, "human"));
		converted.addFixture(PointFactory.point(5, 2), new TextFixture(maxIterWarn, 10));
		converted.setBaseTerrain(PointFactory.point(5, 3), TileType.Plains);
		converted.setGround(PointFactory.point(5, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 4), TileType.Plains);
		converted.setGround(PointFactory.point(5, 4), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 4),
				new Grove(true, true, "fruit1", 11));
		converted.setBaseTerrain(PointFactory.point(5, 5), TileType.Plains);
		converted.setGround(PointFactory.point(5, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 5),
				new Village(TownStatus.Active, "", 3, noPlayer, "human"));
		converted.addFixture(PointFactory.point(5, 5), new TextFixture(maxIterWarn, 10));
		converted.setBaseTerrain(PointFactory.point(5, 6), TileType.Plains);
		converted.setGround(PointFactory.point(5, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 6),
				new Meadow(FIELD_TYPE, true, true, 13, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(5, 7), TileType.Plains);
		converted.setGround(PointFactory.point(5, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 0), TileType.Plains);
		converted.setGround(PointFactory.point(6, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 1), TileType.Desert);
		converted.setGround(PointFactory.point(6, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 1),
				new Meadow(FIELD_TYPE, true, true, 22, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(6, 2), TileType.Desert);
		converted.setGround(PointFactory.point(6, 2), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 2),
				new Meadow(FIELD_TYPE, true, true, 15, FieldStatus.Growing));
		converted.setBaseTerrain(PointFactory.point(6, 3), TileType.Plains);
		converted.setGround(PointFactory.point(6, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 4), TileType.Plains);
		converted.setGround(PointFactory.point(6, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 5), TileType.Plains);
		converted.setGround(PointFactory.point(6, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 5),
				new Grove(true, true, "fruit1", 17));
		converted.setBaseTerrain(PointFactory.point(6, 6), TileType.Plains);
		converted.setGround(PointFactory.point(6, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(6, 6), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(6, 7), TileType.Plains);
		converted.setGround(PointFactory.point(6, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 0), TileType.Plains);
		converted.setGround(PointFactory.point(7, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 1), TileType.Plains);
		converted.setGround(PointFactory.point(7, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 2), TileType.Plains);
		converted.setGround(PointFactory.point(7, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 3), TileType.Desert);
		converted.setGround(PointFactory.point(7, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 4), TileType.Plains);
		converted.setGround(PointFactory.point(7, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 5), TileType.Plains);
		converted.setGround(PointFactory.point(7, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 6), TileType.Plains);
		converted.setGround(PointFactory.point(7, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 7), TileType.Plains);
		converted.setGround(PointFactory.point(7, 7), new Ground(ROCK_TYPE, false));

		final StringWriter outOne = new StringWriter();
		final StringWriter outTwo = new StringWriter();
		assertEquals("Products of two runs are both or neither subsets of expected",
				converted.isSubset(
						new OneToTwoConverter().convert(original, true), outOne, ""),
				converted.isSubset(
						new OneToTwoConverter().convert(original, true), outTwo, ""));
		assertEquals("Two runs produce identical results", outOne.toString(), outTwo.toString());
		assertTrue("Actual is at least subset of expected converted", converted.isSubset(
				new OneToTwoConverter().convert(original, true), System.out, ""));
	}
	/**
	 * Test whether an item is in a Stream. Note that this is a stream-modifying operation.
	 *
	 * @param stream a stream
	 * @param item an item
	 * @param <T> the type of the items in the stream
	 * @param <U> the type of the item
	 * @return whether the stream contains the item
	 */
	private static <T, U extends T> boolean doesStreamContain(final Stream<T> stream, final U item) {
		return stream.anyMatch(each -> Objects.equals(each, item));
	}

	/**
	 * Test version-0 to version-1 conversion.
	 */
	@Test
	public void testZeroToOneConversion()
			throws XMLStreamException, IOException, SPFormatException {
		// FIXME: Include tile fixtures beyond those implicit in events
		final String orig = "<map version='0' rows='2' columns='2'><player number='0' code_name='Test Player' /><row index='0'><tile row='0' column='0' type='tundra' event='0'></tile><tile row='0' column='1' type='boreal_forest' event='183'></tile></row><row index='1'><tile row='1' column='0' type='mountain' event='229'></tile><tile row='1' column='1' type='temperate_forest' event='219'></tile></row></map>";
		final StringWriter out = new StringWriter();
		ZeroToOneConverter.convert(
				new IteratorWrapper<XMLEvent>(XMLInputFactory.newInstance()
						                              .createXMLEventReader(
								                              new StringReader(orig))),
				out);
		final StringWriter actualXML = new StringWriter();
		CompactXMLWriter.writeSPObject(actualXML, new MapReaderAdapter()
				                                                  .readMapFromStream(
						                                                  new StringReader(out.toString()),
						                                                  Warning.Ignore));
		final IMutableMapNG expected = new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		expected.addPlayer(new Player(0, "Test Player"));
		expected.setBaseTerrain(PointFactory.point(0, 0), TileType.Tundra);
		expected.setBaseTerrain(PointFactory.point(0, 1), TileType.BorealForest);
		expected.setBaseTerrain(PointFactory.point(1, 0), TileType.Mountain);
		expected.setBaseTerrain(PointFactory.point(1, 1), TileType.TemperateForest);
		expected.addFixture(PointFactory.point(1, 0),
				new Town(TownStatus.Burned, TownSize.Small, 0, "", 0,
						        new Player(-1, "Independent")));
		expected.addFixture(PointFactory.point(1, 1), new MineralVein("coal", true, 0, 1));

		final StringWriter expectedXML = new StringWriter();
		CompactXMLWriter.writeSPObject(expectedXML, expected);
		assertEquals("Converted map's serialized form was as expected", expectedXML.toString(), actualXML.toString());
		assertEquals("Converted map was as expected", expected, new MapReaderAdapter()
				                                                        .readMapFromStream(
						                                                        new StringReader(out.toString()),
						                                                        Warning.Ignore));
	}
	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestConverter test case";
	}
}
