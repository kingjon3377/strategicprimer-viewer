package controller.map.converter;

import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.SPWriter;
import controller.map.iointerfaces.TestReaderFactory;
import controller.map.misc.IDFactory;
import controller.map.misc.IDRegistrar;
import controller.map.misc.MapReaderAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Formatter;
import java.util.regex.Pattern;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.Point;
import model.map.PointFactory;
import model.map.River;
import model.map.SPMapNG;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.Ground;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import org.junit.Test;
import util.IteratorWrapper;
import util.Warning;
import view.util.SystemOut;

import static model.map.fixtures.resources.FieldStatus.Growing;
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
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), 0);
		final Animal fixture = new Animal("animal", false, true, "domesticated", 1);
		initialize(start, PointFactory.point(0, 0), TileType.Desert, fixture);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
																2);
		initialize(start, PointFactory.point(0, 1), TileType.Desert, fixtureTwo);
		final IUnit fixtureThree =
				new Unit(new Player(0, "A. Player"), "legion", "eagles", 3);
		initialize(start, PointFactory.point(1, 0), TileType.Desert, fixtureThree);
		final Fortress fixtureFour = new Fortress(new Player(1, "B. Player"), "HQ", 4,
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
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), 0);
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
				new SPMapNG(new MapDimensions(3, 3, 2), new PlayerCollection(), -1));
		fail("Shouldn't accept non-even dimensions");
	}

	/**
	 * Test version-1 to version-2 conversion.
	 *
	 * @throws IOException        on I/O error causing test failure
	 * @throws XMLStreamException on error in creating XML
	 * @throws SPFormatException on SP format error causing test failure
	 */
	@SuppressWarnings({"deprecation", "boxing"})
	@Test
	public void testOneToTwoConversion()
			throws IOException, XMLStreamException, SPFormatException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		original.setBaseTerrain(PointFactory.point(0, 0), TileType.BorealForest);
		original.setBaseTerrain(PointFactory.point(0, 1), TileType.TemperateForest);
		original.setBaseTerrain(PointFactory.point(1, 0), TileType.Desert);
		original.setBaseTerrain(PointFactory.point(1, 1), TileType.Plains);
		final Player player = new Player(1, "playerName");
		original.addPlayer(player);
		final Player independent = new Player(2, "independent");
		original.addPlayer(independent);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.addPlayer(player);
		converted.addPlayer(independent);
		initialize(converted, PointFactory.point(0, 0), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false),
				new Village(TownStatus.Active, "", -1, independent, "human"));
		initialize(converted, PointFactory.point(0, 1), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(1, 0), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(1, 1), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 1), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(2, 6), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(3, 0), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(3, 6), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(3, 7), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false),
				new Village(TownStatus.Active, "", -1, independent, "dwarf"));
		initialize(converted, PointFactory.point(4, 0), TileType.Desert,
				new Ground(-1, ROCK_TYPE, false),
				new Village(TownStatus.Active, "", -1, independent, "human"));
		initialize(converted, PointFactory.point(4, 1), TileType.Desert,
				new Ground(-1, ROCK_TYPE, false), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(4, 6), TileType.Plains,
				new Ground(-1, "rock4", false), new Grove(true, true, "fruit4", -1));
		initialize(converted, PointFactory.point(4, 7), TileType.Plains,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(5, 0), TileType.Desert,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(6, 5), TileType.Plains,
				new Ground(-1, "rock4", false), new Grove(true, true, "fruit4", -1));
		initialize(converted, PointFactory.point(6, 6), TileType.Plains,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(6, 7), TileType.Plains,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(7, 6), TileType.Plains,
				new Ground(-1, "rock4", false),
				new Village(TownStatus.Active, "", -1, independent, "human"));
		initialize(converted, PointFactory.point(5, 7), TileType.Plains,
				new Ground(-1, "rock4", false), new Forest("ttree4", false, -1));
		initialize(converted, PointFactory.point(7, 7), TileType.Plains,
				new Ground(-1, "rock4", false), new Grove(true, true, "fruit4", -1));
		for (final Point point : Arrays.asList(PointFactory.point(0, 2),
				PointFactory.point(0, 3), PointFactory.point(1, 2),
				PointFactory.point(1, 3), PointFactory.point(2, 0),
				PointFactory.point(2, 2), PointFactory.point(2, 3),
				PointFactory.point(3, 1), PointFactory.point(3, 2),
				PointFactory.point(3, 3))) {
			initialize(converted, point, TileType.Steppe,
					new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1));
		}
		for (final Point point : Arrays.asList(PointFactory.point(0, 4),
				PointFactory.point(0, 5), PointFactory.point(0, 6),
				PointFactory.point(0, 7), PointFactory.point(1, 4),
				PointFactory.point(1, 5), PointFactory.point(1, 6),
				PointFactory.point(1, 7), PointFactory.point(2, 4),
				PointFactory.point(2, 5), PointFactory.point(2, 7),
				PointFactory.point(3, 4), PointFactory.point(3, 5))) {
			initialize(converted, point, TileType.Plains,
					new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1));
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 2),
				PointFactory.point(5, 1), PointFactory.point(5, 2),
				PointFactory.point(6, 0), PointFactory.point(6, 1),
				PointFactory.point(6, 3), PointFactory.point(7, 0),
				PointFactory.point(7, 2))) {
			initialize(converted, point, TileType.Desert, new Ground(-1, ROCK_TYPE, false));
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 3),
				PointFactory.point(5, 3), PointFactory.point(6, 2),
				PointFactory.point(7, 1), PointFactory.point(7, 3))) {
			initialize(converted, point, TileType.Plains, new Ground(-1, ROCK_TYPE, false));
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 4),
				PointFactory.point(4, 5), PointFactory.point(5, 4),
				PointFactory.point(5, 5), PointFactory.point(5, 6),
				PointFactory.point(6, 4), PointFactory.point(7, 4),
				PointFactory.point(7, 5))) {
			initialize(converted, point, TileType.Plains, new Ground(-1, "rock4", false));
		}

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result",
					POSITIVE_IDS.matcher(outTwo.toString()).replaceAll("id=\"-1\""),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createOldWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			//noinspection HardcodedFileSeparator
			assertThat("Deprecated I/O produces expected result",
					POSITIVE_IDS.matcher(outTwo.toString()).replaceAll("id=\"-1\""),
					equalTo(outOne.toString()));
		}
		try (Formatter outOne = new Formatter();
			 Formatter outTwo = new Formatter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne,
							"")));
			assertThat("Two runs produce identical results", outTwo.out().toString(),
					equalTo(outOne.out().toString()));
		}
		try (StringWriter out = new StringWriter();
			 Formatter err = new Formatter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.writeSPObject(out, new OneToTwoConverter().convert(original, true));
			try (final StringReader in = new StringReader(POSITIVE_IDS
																  .matcher(out.toString())
																  .replaceAll(
																		  "id=\"-1\""))) {
				assertThat("Actual is at least subset of expected converted, modulo IDs",
						converted.isSubset(new MapReaderAdapter().readMapFromStream(in,
								Warning.Ignore), err, ""), equalTo(true));
			}
		}
	}

	/**
	 * Test more version-1 to version-2 conversion.
	 *
	 * @throws IOException        on I/O error causing test failure
	 * @throws XMLStreamException on error creating XML
	 * @throws SPFormatException on SP format error causing test failure
	 */
	@SuppressWarnings({"deprecation", "boxing"})
	@Test
	public void testMoreOneToTwoConversion()
			throws IOException, XMLStreamException, SPFormatException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		initialize(original, PointFactory.point(0, 0), TileType.Jungle);
		initialize(original, PointFactory.point(0, 1), TileType.TemperateForest,
				new Forest(TEMP_TREE, false, 1));
		initialize(original, PointFactory.point(1, 0), TileType.Mountain);
		initialize(original, PointFactory.point(1, 1), TileType.Tundra,
				new Ground(-1, ROCK_TYPE, false));
		final Player player = new Player(1, "playerName");
		original.addPlayer(player);
		final Player independent = new Player(2, "independent");
		original.addPlayer(independent);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.addPlayer(player);
		converted.addPlayer(independent);
		initialize(converted, PointFactory.point(0, 0), TileType.Jungle,
				new Ground(-1, ROCK_TYPE, false),
				new Village(TownStatus.Active, "", -1, independent, "human"));
		initialize(converted, PointFactory.point(0, 1), TileType.Jungle,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 3), TileType.Jungle,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 5), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(3, 0), TileType.Jungle,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(3, 1), TileType.Jungle,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(3, 4), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false),
				new Village(TownStatus.Active, "", -1, independent, "human"));
		initialize(converted, PointFactory.point(3, 5), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(4, 0), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false),
				new Village(TownStatus.Active, "", -1, independent, "Danan"));
		initialize(converted, PointFactory.point(4, 3), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(4, 4), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(5, 0), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(5, 1), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(5, 4), TileType.Tundra,
				new Ground(-1, "rock4", false), new Forest("ttree4", false, -1));
		initialize(converted, PointFactory.point(5, 6), TileType.Tundra,
				new Ground(-1, "rock4", false), new Ground(-1, ROCK_TYPE, false));
		initialize(converted, PointFactory.point(6, 5), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(6, 6), TileType.Tundra,
				new Ground(-1, "rock4", false), new Grove(true, true, "fruit4", -1));
		initialize(converted, PointFactory.point(7, 5), TileType.Tundra, new Ground(-1, "rock4", false), new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(7, 6), TileType.Tundra,
				new Ground(-1, "rock4", false), new Forest("ttree4", false, -1),
				new Village(TownStatus.Active, "", -1, independent, "dwarf"));
		for (final Point point : Arrays.asList(PointFactory.point(0, 2),
				PointFactory.point(0, 3), PointFactory.point(1, 0),
				PointFactory.point(1, 1), PointFactory.point(1, 2),
				PointFactory.point(1, 3), PointFactory.point(2, 0),
				PointFactory.point(2, 1), PointFactory.point(2, 2),
				PointFactory.point(3, 2), PointFactory.point(3, 3))) {
			initialize(converted, point, TileType.Jungle, new Ground(-1, ROCK_TYPE, false));
		}
		for (final Point point : Arrays.asList(PointFactory.point(0, 4),
				PointFactory.point(0, 5), PointFactory.point(0, 6),
				PointFactory.point(0, 7), PointFactory.point(1, 4),
				PointFactory.point(1, 5), PointFactory.point(1, 6),
				PointFactory.point(1, 7), PointFactory.point(2, 4),
				PointFactory.point(2, 6), PointFactory.point(2, 7),
				PointFactory.point(3, 6), PointFactory.point(3, 7))) {
			initialize(converted, point, TileType.Plains, new Ground(-1, ROCK_TYPE, false),
					new Forest(TEMP_TREE, false, -1));
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 1),
				PointFactory.point(5, 2), PointFactory.point(7, 1))) {
			initialize(converted, point, TileType.Plains, new Ground(-1, ROCK_TYPE, false),
					new Forest(TEMP_TREE, false, -1));
			converted.setMountainous(point, true);
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 2),
				PointFactory.point(5, 3), PointFactory.point(6, 0),
				PointFactory.point(6, 1), PointFactory.point(6, 2),
				PointFactory.point(6, 3), PointFactory.point(7, 0),
				PointFactory.point(7, 2), PointFactory.point(7, 3))) {
			initialize(converted, point, TileType.Plains, new Ground(-1, ROCK_TYPE, false));
			converted.setMountainous(point, true);
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 5),
				PointFactory.point(4, 6), PointFactory.point(4, 7),
				PointFactory.point(5, 5), PointFactory.point(5, 7),
				PointFactory.point(6, 4), PointFactory.point(6, 7),
				PointFactory.point(7, 4), PointFactory.point(7, 7))) {
			initialize(converted, point, TileType.Tundra, new Ground(-1, "rock4", false));
		}

		// The converter adds a second forest here, but trying to do the same gets it
		// filtered out because it would have the same ID. So we create it, *then* reset
		// its ID.
		final Forest tempForest = new Forest(TEMP_TREE, false, 0);
		converted.addFixture(PointFactory.point(3, 7), tempForest);
		tempForest.setID(-1);

		converted.setMountainous(PointFactory.point(4, 0), true);
		converted.setMountainous(PointFactory.point(4, 3), true);
		converted.setMountainous(PointFactory.point(5, 0), true);
		converted.setMountainous(PointFactory.point(5, 1), true);

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result",
					POSITIVE_IDS.matcher(outTwo.toString()).replaceAll("id=\"-1\""),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createOldWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			//noinspection HardcodedFileSeparator
			assertThat("Deprecated I/O produces expected result",
					POSITIVE_IDS.matcher(outTwo.toString()).replaceAll("id=\"-1\""),
					equalTo(outOne.toString()));
		}
		try (Formatter outOne = new Formatter();
			 Formatter outTwo = new Formatter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne,
							"")));
			assertThat("Two runs produce identical results", outTwo.out().toString(),
					equalTo(outOne.out().toString()));
		}
		try (StringWriter out = new StringWriter();
			 Formatter err = new Formatter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.writeSPObject(out, new OneToTwoConverter().convert(original, true));
			try (final StringReader in = new StringReader(POSITIVE_IDS
																  .matcher(out.toString())
																  .replaceAll(
																		  "id=\"-1\""))) {
				assertThat("Actual is at least subset of expected converted, modulo IDs",
						converted.isSubset(new MapReaderAdapter().readMapFromStream(in,
								Warning.Ignore), err, ""), equalTo(true));
			}
		}
	}

	/**
	 * Test more version-1 to version-2 conversion.
	 *
	 * @throws IOException        on I/O error causing test failure
	 * @throws XMLStreamException on error creating XML
	 * @throws SPFormatException on SP format error causing test failure
	 */
	@SuppressWarnings({"deprecation", "boxing"})
	@Test
	public void testThirdOneToTwoConversion()
			throws IOException, XMLStreamException, SPFormatException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		initialize(original, PointFactory.point(0, 0), TileType.NotVisible,
				new Ground(-1, ROCK_TYPE, false));
		final Player independent = new Player(2, "independent");
		initialize(original, PointFactory.point(0, 1), TileType.BorealForest,
				new Forest(TEMP_TREE, false, 1), new Hill(1),
				new Animal("animalKind", false, false, "wild", 2),
				new Mine("mineral", TownStatus.Active, 3),
				new AdventureFixture(independent, "briefDescription", "fullDescription",
											4), new Simurgh(5), new Griffin(6),
				new City(TownStatus.Ruined, TownSize.Large, 0, "cityName", 7,
								independent), new Ogre(8), new Minotaur(9),
				new Centaur("hill", 10), new Giant("frost", 11), new Djinn(12),
				new Fairy("lesser", 13), new Dragon("ice", 14), new Troll(15),
				new Fortification(TownStatus.Burned, TownSize.Medium, 0, "townName", 16,
										 independent),
				new StoneDeposit(StoneKind.Conglomerate, 0, 17));
		initialize(original, PointFactory.point(1, 0), TileType.Mountain);
		initialize(original, PointFactory.point(1, 1), TileType.Tundra);
		original.addRivers(PointFactory.point(0, 1), River.Lake, River.South);
		original.addRivers(PointFactory.point(1, 0), River.East, River.West);
		original.addRivers(PointFactory.point(1, 1), River.West, River.North);
		final Player player = new Player(1, "playerName");
		original.addPlayer(player);
		original.addPlayer(independent);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.addPlayer(player);
		converted.addPlayer(independent);

		initialize(converted, PointFactory.point(0, 4), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Hill(-1), new Centaur("hill", -1),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(0, 5), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Troll(-1), new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(0, 6), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new AdventureFixture(independent, "briefDescription", "fullDescription",
											-1));
		initialize(converted, PointFactory.point(0, 7), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Griffin(-1), new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(1, 0), TileType.NotVisible,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(1, 3), TileType.NotVisible,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new Village(TownStatus.Active, "", -1, independent, "half-elf"));
		initialize(converted, PointFactory.point(1, 4), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Mine("mineral", TownStatus.Active, -1),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(1, 5), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Giant("frost", -1), new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(1, 6), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new StoneDeposit(StoneKind.Conglomerate, 0, -1));
		initialize(converted, PointFactory.point(1, 7), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Dragon("ice", -1), new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 2), TileType.NotVisible,
				new Ground(-1, ROCK_TYPE, false), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(2, 3), TileType.NotVisible,
				new Ground(-1, ROCK_TYPE, false),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 4), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Fairy("lesser", -1), new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 5), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false),
				new Fortification(TownStatus.Burned, TownSize.Medium, 0, "townName", -1,
										 independent),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 6), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Djinn(-1),
				new Village(TownStatus.Active, "", -1, independent, "Danan"),
				new TextFixture(OneToTwoConverter.MAX_ITER_WARN, 10),
				new Meadow(FIELD_TYPE, true, true, -1, Growing));
		initialize(converted, PointFactory.point(2, 7), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Ogre(-1));
		initialize(converted, PointFactory.point(3, 0), TileType.NotVisible,
				new Ground(-1, ROCK_TYPE, false), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(3, 4), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Simurgh(-1), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(3, 5), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Minotaur(-1), new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(3, 6), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new City(TownStatus.Ruined, TownSize.Large, 0, "cityName", -1,
								independent));
		initialize(converted, PointFactory.point(3, 7), TileType.Steppe,
				new Ground(-1, ROCK_TYPE, false), new Forest(BOREAL_TREE, false, -1),
				new Animal("animalKind", false, false, "wild", -1),
				new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(4, 0), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1),
				new Village(TownStatus.Active, "", -1, independent, "dwarf"));
		initialize(converted, PointFactory.point(4, 6), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(4, 7), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(5, 1), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Grove(true, true, "fruit1", -1));
		initialize(converted, PointFactory.point(6, 1), TileType.Plains,
				new Ground(-1, ROCK_TYPE, false), new Forest(TEMP_TREE, false, -1));
		initialize(converted, PointFactory.point(6, 5), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(6, 7), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(7, 5), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Meadow("grain4", true, true, -1, Growing));
		initialize(converted, PointFactory.point(7, 6), TileType.Tundra,
				new Ground(-1, "rock4", false),
				new Village(TownStatus.Active, "", -1, independent, "human"));
		for (final Point point : Arrays.asList(PointFactory.point(0, 0),
				PointFactory.point(0, 1), PointFactory.point(1, 1),
				PointFactory.point(1, 2), PointFactory.point(2, 0),
				PointFactory.point(2, 1), PointFactory.point(3, 1),
				PointFactory.point(3, 2), PointFactory.point(3, 3))) {
			initialize(converted, point, TileType.NotVisible,
					new Ground(-1, ROCK_TYPE, false));
		}
		for (final Point point : Arrays.asList(PointFactory.point(0, 2),
				PointFactory.point(0, 3))) {
			initialize(converted, point, TileType.NotVisible,
					new Ground(-1, ROCK_TYPE, false),
					new Meadow(FIELD_TYPE, true, true, -1, Growing));
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 1),
				PointFactory.point(4, 2), PointFactory.point(4, 3),
				PointFactory.point(5, 0), PointFactory.point(5, 2),
				PointFactory.point(5, 3), PointFactory.point(6, 0),
				PointFactory.point(6, 2), PointFactory.point(6, 3),
				PointFactory.point(7, 0), PointFactory.point(7, 1),
				PointFactory.point(7, 2), PointFactory.point(7, 3))) {
			initialize(converted, point, TileType.Plains, new Ground(-1, ROCK_TYPE, false));
			converted.setMountainous(point, true);
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 4),
				PointFactory.point(4, 5), PointFactory.point(5, 4),
				PointFactory.point(5, 5), PointFactory.point(5, 6),
				PointFactory.point(5, 7), PointFactory.point(6, 4),
				PointFactory.point(6, 6), PointFactory.point(7, 4),
				PointFactory.point(7, 7))) {
			initialize(converted, point, TileType.Tundra, new Ground(-1, "rock4", false));
		}
		converted.addRivers(PointFactory.point(2, 6), River.Lake, River.South);
		converted.addRivers(PointFactory.point(3, 6), River.North, River.South);
		converted.setMountainous(PointFactory.point(4, 0), true);
		converted.addRivers(PointFactory.point(4, 6), River.North, River.South);
		converted.setMountainous(PointFactory.point(5, 1), true);
		converted.addRivers(PointFactory.point(5, 6), River.North, River.South);
		converted.addRivers(PointFactory.point(6, 0), River.East, River.West);
		converted.addRivers(PointFactory.point(6, 1), River.East, River.West);
		converted.setMountainous(PointFactory.point(6, 1), true);
		converted.addRivers(PointFactory.point(6, 2), River.East, River.West);
		converted.addRivers(PointFactory.point(6, 3), River.East, River.West);
		converted.addRivers(PointFactory.point(6, 4), River.East, River.West);
		converted.addRivers(PointFactory.point(6, 5), River.East, River.West);
		converted.addRivers(PointFactory.point(6, 6), River.North, River.West);

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result",
					POSITIVE_IDS.matcher(outTwo.toString()).replaceAll("id=\"-1\""),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createOldWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			//noinspection HardcodedFileSeparator
			assertThat("Deprecated I/O produces expected result",
					POSITIVE_IDS.matcher(outTwo.toString()).replaceAll("id=\"-1\""),
					equalTo(outOne.toString()));
		}
		try (Formatter outOne = new Formatter();
			 Formatter outTwo = new Formatter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne,
							"")));
			assertThat("Two runs produce identical results", outTwo.out().toString(),
					equalTo(outOne.out().toString()));
		}
		try (StringWriter out = new StringWriter();
			 StringWriter err = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.writeSPObject(out, new OneToTwoConverter().convert(original, true));
			try (final StringReader in = new StringReader(POSITIVE_IDS
																  .matcher(out.toString())
																  .replaceAll(
																		  "id=\"-1\""));
				 final Formatter stdout = new Formatter(SystemOut.SYS_OUT)) {
				assertThat("Actual is at least subset of expected converted, modulo IDs",
						converted.isSubset(new MapReaderAdapter().readMapFromStream(in,
								Warning.Ignore), stdout, ""), equalTo(true));
			}
		}
	}
	/**
	 * @param idFac an ID factory
	 * @param fix a fixture
	 * @return the fixture, having registered its ID.
	 */
	private static TileFixture register(final IDRegistrar idFac, final TileFixture fix) {
		switch (idFac.register(fix.getID())) {
		case 1:
			System.err.println("1 was registered");
			break;
		case 2:
			System.err.println("2 was registered");
			break;
		default:
			if (fix.getID() == 1 || fix.getID() == 2) {
				System.err.println("1 or 2 wasn't registered!");
			}
		}
		System.err.flush();
		return fix;
	}
	/**
	 * Test more version-1 to version-2 conversion.
	 *
	 * @throws IOException        on I/O error causing test failure
	 */
	@SuppressWarnings({"boxing", "static-method"})
	@Test
	public void testFourthOneToTwoConversion() throws IOException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		initialize(original, PointFactory.point(0, 0), TileType.Ocean);
		for (final Point point : Arrays.asList(PointFactory.point(0, 1),
				PointFactory.point(1, 0), PointFactory.point(1, 1))) {
			initialize(original, point, TileType.Desert);
		}
		final Player player = new Player(1, "playerName");
		original.addPlayer(player);
		final Player independent = new Player(2, "independent");
		original.addPlayer(independent);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.addPlayer(player);
		converted.addPlayer(independent);
		final IDRegistrar testFactory = new IDFactory();
		initialize(converted, PointFactory.point(0, 0), TileType.Ocean,
				register(testFactory,
						new Village(TownStatus.Active, "", 16, independent, "human")));
		initialize(converted, PointFactory.point(3, 7), TileType.Plains,
				register(testFactory, new Village(TownStatus.Active, "", 33, independent, "half-elf")));
		initialize(converted, PointFactory.point(4, 0), TileType.Desert,
				register(testFactory, new Village(TownStatus.Active, "", 50, independent, "human")));
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				initialize(converted, PointFactory.point(i, j), TileType.NotVisible,
						new Ground(testFactory.createID(), ROCK_TYPE, false));
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 4; j < 8; j++) {
				initialize(converted, PointFactory.point(i, j), TileType.NotVisible,
						new Ground(testFactory.createID(), ROCK_TYPE, false));
			}
		}
		for (int i = 4; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				initialize(converted, PointFactory.point(i, j), TileType.NotVisible,
						new Ground(testFactory.createID(), ROCK_TYPE, false));
			}
		}
		for (int i = 4; i < 8; i++) {
			for (int j = 4; j < 8; j++) {
				initialize(converted, PointFactory.point(i, j), TileType.NotVisible,
						new Ground(testFactory.createID(), "rock4", false));
			}
		}
		initialize(converted, PointFactory.point(3, 6), TileType.Desert,
				register(testFactory, new Meadow(FIELD_TYPE, true, true, 75, Growing)));
		initialize(converted, PointFactory.point(4, 1), TileType.Desert,
				register(testFactory, new Meadow(FIELD_TYPE, true, true, 72, Growing)));
		initialize(converted, PointFactory.point(4, 6), TileType.Desert,
				register(testFactory, new Grove(true, true, "fruit4", 78)));
		initialize(converted, PointFactory.point(5, 1), TileType.Desert,
				register(testFactory, new Grove(true, true, "fruit1", 71)));
		initialize(converted, PointFactory.point(6, 5), TileType.Desert,
				register(testFactory, new Grove(true, true, "fruit4", 73)));
		initialize(converted, PointFactory.point(6, 6), TileType.Desert,
				register(testFactory, new Meadow("grain4", true, true, 76, Growing)));
		initialize(converted, PointFactory.point(6, 7), TileType.Desert,
				register(testFactory, new Meadow("grain4", true, true, 77, Growing)));
		initialize(converted, PointFactory.point(7, 5), TileType.Desert,
				register(testFactory, new Grove(true, true, "fruit4", 74)));
		initialize(converted, PointFactory.point(7, 6), TileType.Desert,
				register(testFactory, new Village(TownStatus.Active, "", 67, independent, "human")));
		initialize(converted, PointFactory.point(2, 6), TileType.Desert,
				register(testFactory, new Grove(true, true, "fruit1", 68)));
		initialize(converted, PointFactory.point(2, 7), TileType.Desert,
				register(testFactory, new Meadow(FIELD_TYPE, true, true, 69, Growing)));
		initialize(converted, PointFactory.point(4, 7), TileType.Desert,
				register(testFactory, new Meadow("grain4", true, true, 70, Growing)));
		for (final Point point : Arrays.asList(PointFactory.point(0, 1),
				PointFactory.point(0, 2), PointFactory.point(0, 3),
				PointFactory.point(1, 0), PointFactory.point(1, 1),
				PointFactory.point(1, 2), PointFactory.point(1, 3),
				PointFactory.point(2, 0), PointFactory.point(2, 1),
				PointFactory.point(2, 2), PointFactory.point(2, 3),
				PointFactory.point(3, 0), PointFactory.point(3, 1),
				PointFactory.point(3, 2), PointFactory.point(3, 3))) {
			initialize(converted, point, TileType.Ocean);
		}
		for (final Point point : Arrays.asList(PointFactory.point(0, 4),
				PointFactory.point(0, 5), PointFactory.point(1, 4),
				PointFactory.point(3, 5), PointFactory.point(4, 2),
				PointFactory.point(4, 3), PointFactory.point(5, 0),
				PointFactory.point(5, 2), PointFactory.point(6, 0),
				PointFactory.point(6, 1), PointFactory.point(7, 0),
				PointFactory.point(7, 1), PointFactory.point(7, 3))) {
			initialize(converted, point, TileType.Desert);
		}
		for (final Point point : Arrays.asList(PointFactory.point(0, 6),
				PointFactory.point(0, 7), PointFactory.point(1, 5),
				PointFactory.point(1, 6), PointFactory.point(1, 7),
				PointFactory.point(2, 4), PointFactory.point(2, 5),
				PointFactory.point(3, 4), PointFactory.point(5, 3),
				PointFactory.point(6, 2), PointFactory.point(6, 3),
				PointFactory.point(7, 2))) {
			initialize(converted, point, TileType.Plains);
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 4),
				PointFactory.point(5, 4), PointFactory.point(5, 6),
				PointFactory.point(6, 4), PointFactory.point(7, 4),
				PointFactory.point(7, 7))) {
			initialize(converted, point, TileType.Plains);
		}
		for (final Point point : Arrays.asList(PointFactory.point(4, 5),
				PointFactory.point(5, 5), PointFactory.point(5, 7))) {
			initialize(converted, point, TileType.Desert);
		}

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			final SPWriter writer = TestReaderFactory.createNewWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		try (Formatter outOne = new Formatter();
			 Formatter outTwo = new Formatter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne,
							"")));
			assertThat("Two runs produce identical results", outTwo.out().toString(),
					equalTo(outOne.out().toString()));
		}
		try (final Formatter out = new Formatter(SystemOut.SYS_OUT)) {
			assertThat("Actual is at least subset of expected converted",
					converted.isSubset(new OneToTwoConverter().convert(original, true),
							out, ""), equalTo(true));
		}
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
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		final Player player = new Player(0, "Test Player");
		expected.addPlayer(player);
		initialize(expected, PointFactory.point(0, 0), TileType.Tundra,
				new TextFixture("Random event here", -1));
		initialize(expected, PointFactory.point(0, 1), TileType.BorealForest);
		initialize(expected, PointFactory.point(1, 0), TileType.Mountain,
				new Town(TownStatus.Burned, TownSize.Small, 0, "", 0,
								new Player(-1, "Independent")),
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
