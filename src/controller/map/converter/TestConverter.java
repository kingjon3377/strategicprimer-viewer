package controller.map.converter;

import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.SPWriter;
import controller.map.iointerfaces.TestReaderFactory;
import controller.map.misc.MapReaderAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensionsImpl;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.PlayerImpl;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import org.junit.Test;
import util.IteratorWrapper;
import util.Warning;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * The test case for map-conversion code paths.
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
public final class TestConverter {
	/**
	 * Extracted constant: the type of rock used in the test-data tables.
	 */
	private static final String ROCK_TYPE = "rock1";
	/**
	 * Extracted constant: the type of tree used for boreal forests in the test-data
	 * tables.
	 */
	private static final String BOREAL_TREE = "btree1";
	/**
	 * Extracted constant: the type of field used in the test-data tables.
	 */
	private static final String FIELD_TYPE = "grain1";
	/**
	 * Extracted constant: the type of tree used for temperate forests in the test-data
	 * tables.
	 */
	private static final String TEMP_TREE = "ttree1";
	/**
	 * A pattern to match all positive IDs in XML.
	 */
	private static final Pattern POSITIVE_IDS =
			Pattern.compile("id=\"[0-9]*\"");
	/**
	 * Initialize a point in the map with its terrain and a list of fixtures.
	 * @param map a map
	 * @param point a point
	 * @param terrain the terrain type there
	 * @param fixtures fixtures to put there
	 */
	private static void initialize(final IMutableMapNG map, final Point point,
								   final TileType terrain,
								   final TileFixture... fixtures) {
		if (TileType.NotVisible != terrain) {
			map.setBaseTerrain(point, terrain);
		}
		for (final TileFixture fixture : fixtures) {
			if (fixture instanceof Ground && map.getGround(point) == null) {
				map.setGround(point, (Ground) fixture);
			} else if (fixture instanceof Forest && map.getForest(point) == null) {
				map.setForest(point, (Forest) fixture);
			} else {
				map.addFixture(point, fixture);
			}
		}
	}
	/**
	 * Test conversion.
	 */
	@Test
	public void testConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensionsImpl(2, 2, 2), new PlayerCollection(), 0);
		final Animal fixture = new Animal("animal", false, true, "domesticated", 1);
		initialize(start, PointFactory.point(0, 0), TileType.Desert, fixture);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
																2);
		initialize(start, PointFactory.point(0, 1), TileType.Desert, fixtureTwo);
		final IUnit fixtureThree =
				new Unit(new PlayerImpl(0, "A. Player"), "legion", "eagles", 3);
		initialize(start, PointFactory.point(1, 0), TileType.Desert, fixtureThree);
		final Fortress fixtureFour = new Fortress(new PlayerImpl(1, "B. Player"), "HQ", 4,
														 TownSize.Small);
		initialize(start, PointFactory.point(1, 1), TileType.Plains, fixtureFour);

		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertThat("Combined tile should contain fixtures from tile one",
				converted.getOtherFixtures(zeroPoint), hasItem(fixture));
		assertThat("Combined tile should contain fixtures from tile two",
				converted.getOtherFixtures(zeroPoint), hasItem(fixtureTwo));
		assertThat("Combined tile should contain fixtures from tile three",
				converted.getOtherFixtures(zeroPoint), hasItem(fixtureThree));
		assertThat("Combined tile should contain fixtures from tile four",
				converted.getOtherFixtures(zeroPoint), hasItem(fixtureFour));
		assertThat("Combined tile has type of most of input tiles",
				converted.getBaseTerrain(zeroPoint), equalTo(TileType.Desert));
	}

	/**
	 * Test more corners of resolution-decrease conversion.
	 */
	@Test
	public void testMoreConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensionsImpl(2, 2, 2), new PlayerCollection(), 0);
		final Point pointOne = PointFactory.point(0, 0);
		start.setMountainous(pointOne, true);
		start.addRivers(pointOne, River.East, River.South);
		final Ground groundOne = new Ground(-1, "groundOne", false);
		initialize(start, pointOne, TileType.Steppe, groundOne);
		final Point pointTwo = PointFactory.point(0, 1);
		start.addRivers(pointTwo, River.Lake);
		final Ground groundTwo = new Ground(-1, "groundTwo", false);
		initialize(start, pointTwo, TileType.Steppe, groundTwo);
		final Point pointThree = PointFactory.point(1, 0);
		final Forest forestOne = new Forest("forestOne", false, 1);
		initialize(start, pointThree, TileType.Plains, forestOne);
		final Point pointFour = PointFactory.point(1, 1);
		final Forest forestTwo = new Forest("forestTwo", false, 2);
		initialize(start, pointFour, TileType.Desert, forestTwo);
		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertThat("One mountainous point makes the reduced point mountainous",
				Boolean.valueOf(converted.isMountainous(zeroPoint)),
				equalTo(Boolean.TRUE));
		assertThat("Ground carries over", converted.getGround(zeroPoint),
				equalTo(groundOne));
		assertThat("Ground carries over even when already set",
				converted.getOtherFixtures(zeroPoint), hasItem(groundTwo));
		assertThat("Forest carries over", converted.getForest(zeroPoint),
				equalTo(forestOne));
		assertThat("Forest carries over even when already set",
				converted.getOtherFixtures(zeroPoint), hasItem(forestTwo));
		assertThat("Non-interior rivers carry over", converted.getRivers(zeroPoint),
				hasItem(River.Lake));
		assertThat("Non-interior rivers carry over", converted.getRivers(zeroPoint),
				not(hasItems(River.East, River.South)));
		assertThat("Combined tile has most common terrain type among inputs",
				converted.getBaseTerrain(zeroPoint), equalTo(TileType.Steppe));
	}

	/**
	 * Test that resolution-decrease conversion fails fast.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testResolutionDecreaseRequirement() {
		ResolutionDecreaseConverter.convert(
				new SPMapNG(new MapDimensionsImpl(3, 3, 2), new PlayerCollection(), -1));
		fail("Shouldn't accept non-even dimensions");
	}

	/**
	 * Test version-0 to version-1 conversion.
	 *
	 * @throws IOException        on I/O error causing test failure
	 * @throws XMLStreamException on malformed XML in tests
	 * @throws SPFormatException  on malformed SP XML in tests
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testZeroToOneConversion()
			throws XMLStreamException, IOException, SPFormatException {
		// FIXME: Include tile fixtures beyond those implicit in events
		final String orig = "<map xmlns:sp=\"" + ISPReader.NAMESPACE +
									"\" version='0' rows='2' columns='2'><player " +
									"number='0' code_name='Test Player' /><row " +
									"index='0'><tile row='0' column='0' type='tundra' " +
									"event='0'>Random event here</tile><tile row='0' " +
									"column='1' type='boreal_forest' " +
									"event='183'></tile></row><row index='1'><sp:tile " +
									"row='1' column='0' type='mountain' " +
									"event='229'><sp:fortress name='HQ' owner='0' " +
									"id='15'/></sp:tile><tile row='1' column='1' " +
									"type='temperate_forest' " +
									"event='219'></tile></row></map>";
		final StringWriter out = new StringWriter();
		//noinspection unchecked
		ZeroToOneConverter.convert(new IteratorWrapper<>(
				XMLInputFactory.newInstance().createXMLEventReader(
						new StringReader(orig))), out);
		final StringWriter actualXML = new StringWriter();
		final SPWriter writer = TestReaderFactory.createOldWriter();
		writer.writeSPObject(actualXML, new MapReaderAdapter()
												.readMapFromStream(
														new StringReader(out.toString()),
														Warning.Ignore));
		final IMutableMapNG expected =
				new SPMapNG(new MapDimensionsImpl(2, 2, 1), new PlayerCollection(), 0);
		final Player player = new PlayerImpl(0, "Test Player");
		expected.addPlayer(player);
		initialize(expected, PointFactory.point(0, 0), TileType.Tundra,
				new TextFixture("Random event here", -1));
		initialize(expected, PointFactory.point(0, 1), TileType.BorealForest);
		initialize(expected, PointFactory.point(1, 0), TileType.Mountain,
				new Town(TownStatus.Burned, TownSize.Small, 0, "", 0,
								new PlayerImpl(-1, "Independent")),
				new Fortress(player, "HQ", 15, TownSize.Small));
		initialize(expected, PointFactory.point(1, 1), TileType.TemperateForest,
				new MineralVein("coal", true, 0, 1));

		final StringWriter expectedXML = new StringWriter();
		writer.writeSPObject(expectedXML, expected);
		assertThat("Converted map's serialized form was as expected",
				actualXML.toString(), equalTo(expectedXML.toString()));
		assertThat("Converted map was as expected",
				new MapReaderAdapter()
						.readMapFromStream(new StringReader(out.toString()),
								Warning.Ignore), equalTo(expected));
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestConverter test case";
	}
}
