package controller.map.converter;

import controller.map.cxml.CompactXMLWriter;
import controller.map.fluidxml.SPFluidWriter;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.MapReaderAdapter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.stream.Stream;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), 0);
		final Point pointOne = PointFactory.point(0, 0);
		final Animal fixture = new Animal("animal", false, true, "domesticated", 1);
		start.addFixture(pointOne, fixture);
		start.setBaseTerrain(pointOne, TileType.Desert);
		final Point pointTwo = PointFactory.point(0, 1);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
																2);
		start.addFixture(pointTwo, fixtureTwo);
		start.setBaseTerrain(pointTwo, TileType.Desert);
		final Point pointThree = PointFactory.point(1, 0);
		final IUnit fixtureThree =
				new Unit(new Player(0, "A. Player"), "legion", "eagles", 3);
		start.addFixture(pointThree, fixtureThree);
		start.setBaseTerrain(pointThree, TileType.Desert);
		final Point pointFour = PointFactory.point(1, 1);
		final Fortress fixtureFour = new Fortress(new Player(1, "B. Player"), "HQ", 4);
		start.addFixture(pointFour, fixtureFour);
		start.setBaseTerrain(pointFour, TileType.Plains);
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
	@SuppressWarnings("static-method")
	@Test
	public void testMoreConversion() {
		final IMutableMapNG start =
				new SPMapNG(new MapDimensions(2, 2, 2), new PlayerCollection(), 0);
		final Point pointOne = PointFactory.point(0, 0);
		start.setMountainous(pointOne, true);
		final Ground groundOne = new Ground("groundOne", false);
		start.setGround(pointOne, groundOne);
		start.addRivers(pointOne, River.East, River.South);
		start.setBaseTerrain(pointOne, TileType.Steppe);
		final Point pointTwo = PointFactory.point(0, 1);
		final Ground groundTwo = new Ground("groundTwo", false);
		start.setGround(pointTwo, groundTwo);
		start.addRivers(pointTwo, River.Lake);
		start.setBaseTerrain(pointTwo, TileType.Steppe);
		final Point pointThree = PointFactory.point(1, 0);
		final Forest forestOne = new Forest("forestOne", false);
		start.setForest(pointThree, forestOne);
		start.setBaseTerrain(pointThree, TileType.Plains);
		final Point pointFour = PointFactory.point(1, 1);
		final Forest forestTwo = new Forest("forestTwo", false);
		start.setForest(pointFour, forestTwo);
		start.setBaseTerrain(pointFour, TileType.Desert);
		final IMapNG converted = ResolutionDecreaseConverter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertThat("One mountainous point makes the reduced point mountainous",
				converted.isMountainous(zeroPoint), equalTo(true));
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
	 * @throws IOException on I/O error causing test failure
	 *
	 * TODO: Make the converter (and the tests) pass the map size to the encounter tables.
	 */
	@SuppressWarnings({"deprecation", "boxing", "static-method"})
	@Test
	public void testOneToTwoConversion() throws IOException {
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
		converted.setBaseTerrain(PointFactory.point(0, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(0, 0),
				new Village(TownStatus.Active, "", 0, independent, "dwarf"));
		converted.setBaseTerrain(PointFactory.point(0, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 1), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 1),
				new Meadow(FIELD_TYPE, true, true, 5, Growing));
		converted.setBaseTerrain(PointFactory.point(0, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 2), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 3), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 3), new Forest(BOREAL_TREE, false));
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
		converted
				.addFixture(PointFactory.point(1, 0), new Grove(true, true, "fruit1", 8));
		converted.setBaseTerrain(PointFactory.point(1, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 1), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(1, 1),
				new Meadow(FIELD_TYPE, true, true, 9, Growing));
		converted.setBaseTerrain(PointFactory.point(1, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 2), new Forest(BOREAL_TREE, false));

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
		converted.setBaseTerrain(PointFactory.point(2, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 0), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 1), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 1), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(2, 1), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 2), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 2), new Forest(BOREAL_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 3), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 3), new Forest(BOREAL_TREE, false));
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
				new Grove(true, true, "fruit1", 4));
		converted.setBaseTerrain(PointFactory.point(2, 7), TileType.Plains);
		converted.setGround(PointFactory.point(2, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 7), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 0), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 0), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(3, 0),
				new Meadow(FIELD_TYPE, true, true, 14, Growing));
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
				new Grove(true, true, "fruit1", 12));
		converted.setBaseTerrain(PointFactory.point(3, 7), TileType.Plains);
		converted.setGround(PointFactory.point(3, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 7),
				new Village(TownStatus.Active, "", 1, independent, "human"));

		converted.setBaseTerrain(PointFactory.point(4, 0), TileType.Desert);
		converted.setGround(PointFactory.point(4, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 0),
				new Village(TownStatus.Active, "", 2, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(4, 1), TileType.Desert);
		converted.setGround(PointFactory.point(4, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 1),
				new Grove(true, true, "fruit1", 7));
		converted.setBaseTerrain(PointFactory.point(4, 2), TileType.Desert);
		converted.setGround(PointFactory.point(4, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 3), TileType.Plains);
		converted.setGround(PointFactory.point(4, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 4), TileType.Plains);
		converted.setGround(PointFactory.point(4, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 5), TileType.Plains);
		converted.setGround(PointFactory.point(4, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 6), TileType.Plains);
		converted.setGround(PointFactory.point(4, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 6),
				new Grove(true, true, "fruit1", 17));
		converted.setBaseTerrain(PointFactory.point(4, 7), TileType.Plains);
		converted.setGround(PointFactory.point(4, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 7),
				new Meadow(FIELD_TYPE, true, true, 6, Growing));
		converted.setBaseTerrain(PointFactory.point(5, 0), TileType.Desert);
		converted.setGround(PointFactory.point(5, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 0),
				new Meadow(FIELD_TYPE, true, true, 11, Growing));
		converted.setBaseTerrain(PointFactory.point(5, 1), TileType.Desert);
		converted.setGround(PointFactory.point(5, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 2), TileType.Desert);
		converted.setGround(PointFactory.point(5, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 3), TileType.Plains);
		converted.setGround(PointFactory.point(5, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 4), TileType.Plains);
		converted.setGround(PointFactory.point(5, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 5), TileType.Plains);
		converted.setGround(PointFactory.point(5, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 6), TileType.Plains);
		converted.setGround(PointFactory.point(5, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 7), TileType.Plains);
		converted.setGround(PointFactory.point(5, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(5, 7), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(6, 0), TileType.Desert);
		converted.setGround(PointFactory.point(6, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 1), TileType.Desert);
		converted.setGround(PointFactory.point(6, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 2), TileType.Plains);
		converted.setGround(PointFactory.point(6, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 3), TileType.Desert);
		converted.setGround(PointFactory.point(6, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 4), TileType.Plains);
		converted.setGround(PointFactory.point(6, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 5), TileType.Plains);
		converted.setGround(PointFactory.point(6, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 5),
				new Grove(true, true, "fruit1", 10));
		converted.setBaseTerrain(PointFactory.point(6, 6), TileType.Plains);
		converted.setGround(PointFactory.point(6, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 6),
				new Meadow(FIELD_TYPE, true, true, 15, Growing));
		converted.setBaseTerrain(PointFactory.point(6, 7), TileType.Plains);
		converted.setGround(PointFactory.point(6, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 7),
				new Meadow(FIELD_TYPE, true, true, 16, Growing));
		converted.setBaseTerrain(PointFactory.point(7, 0), TileType.Desert);
		converted.setGround(PointFactory.point(7, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 1), TileType.Plains);
		converted.setGround(PointFactory.point(7, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 2), TileType.Desert);
		converted.setGround(PointFactory.point(7, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 3), TileType.Plains);
		converted.setGround(PointFactory.point(7, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 4), TileType.Plains);
		converted.setGround(PointFactory.point(7, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 5), TileType.Plains);
		converted.setGround(PointFactory.point(7, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 6), TileType.Plains);
		converted.setGround(PointFactory.point(7, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 6),
				new Village(TownStatus.Active, "", 3, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(7, 7), TileType.Plains);
		converted.setGround(PointFactory.point(7, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 7),
				new Grove(true, true, "fruit1", 13));


//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter(); StringWriter outTwo = new StringWriter()) {
			final SPFluidWriter writer = new SPFluidWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
				StringWriter outTwo = new StringWriter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne, "")));
			assertThat("Two runs produce identical results", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		assertThat("Actual is at least subset of expected converted", converted.isSubset(
				new OneToTwoConverter().convert(original, true), SystemOut.SYS_OUT,
				""), equalTo(true));
	}
	/**
	 * Test more version-1 to version-2 conversion.
	 *
	 * @throws IOException on I/O error causing test failure
	 *
	 * TODO: Make the converter (and the tests) pass the map size to the encounter tables.
	 */
	@SuppressWarnings({"deprecation", "boxing", "static-method"})
	@Test
	public void testMoreOneToTwoConversion() throws IOException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		original.setBaseTerrain(PointFactory.point(0, 0), TileType.Jungle);
		original.setBaseTerrain(PointFactory.point(0, 1), TileType.TemperateForest);
		original.setForest(PointFactory.point(0, 1), new Forest(TEMP_TREE, false));
		original.setBaseTerrain(PointFactory.point(1, 0), TileType.Mountain);
		original.setBaseTerrain(PointFactory.point(1, 1), TileType.Tundra);
		original.setGround(PointFactory.point(1, 1), new Ground(ROCK_TYPE, false));
		final Player player = new Player(1, "playerName");
		original.addPlayer(player);
		final Player independent = new Player(2, "independent");
		original.addPlayer(independent);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.addPlayer(player);
		converted.addPlayer(independent);
		converted.setBaseTerrain(PointFactory.point(0, 0), TileType.Jungle);
		converted.setGround(PointFactory.point(0, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(0, 0),
				new Village(TownStatus.Active, "", 0, independent, "dwarf"));
		converted.setBaseTerrain(PointFactory.point(0, 1), TileType.Jungle);
		converted.setGround(PointFactory.point(0, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(0, 1),
				new Meadow(FIELD_TYPE, true, true, 4, Growing));
		converted.setBaseTerrain(PointFactory.point(0, 2), TileType.Jungle);
		converted.setGround(PointFactory.point(0, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 3), TileType.Jungle);
		converted.setGround(PointFactory.point(0, 3), new Ground(ROCK_TYPE, false));
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
		converted.setBaseTerrain(PointFactory.point(1, 0), TileType.Jungle);
		converted.setGround(PointFactory.point(1, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 1), TileType.Jungle);
		converted.setGround(PointFactory.point(1, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 2), TileType.Jungle);
		converted.setGround(PointFactory.point(1, 2), new Ground(ROCK_TYPE, false));

		converted.setBaseTerrain(PointFactory.point(1, 3), TileType.Jungle);
		converted.setGround(PointFactory.point(1, 3), new Ground(ROCK_TYPE, false));
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
		converted.setBaseTerrain(PointFactory.point(2, 0), TileType.Jungle);
		converted.setGround(PointFactory.point(2, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 1), TileType.Jungle);
		converted.setGround(PointFactory.point(2, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 2), TileType.Jungle);
		converted.setGround(PointFactory.point(2, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 3), TileType.Jungle);
		converted.setGround(PointFactory.point(2, 3), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(2, 3),
				new Meadow(FIELD_TYPE, true, true, 9, Growing));
		converted.setBaseTerrain(PointFactory.point(2, 4), TileType.Plains);
		converted.setGround(PointFactory.point(2, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 4), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 5), TileType.Plains);
		converted.setGround(PointFactory.point(2, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 5), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(2, 5),
				new Grove(true, true, "fruit1", 10));
		converted.setBaseTerrain(PointFactory.point(2, 6), TileType.Plains);
		converted.setGround(PointFactory.point(2, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 6), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(2, 7), TileType.Plains);
		converted.setGround(PointFactory.point(2, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 7), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 0), TileType.Jungle);
		converted.setGround(PointFactory.point(3, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 0),
				new Meadow(FIELD_TYPE, true, true, 12, Growing));
		converted.setBaseTerrain(PointFactory.point(3, 1), TileType.Jungle);
		converted.setGround(PointFactory.point(3, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 1),
				new Meadow(FIELD_TYPE, true, true, 6, Growing));
		converted.setBaseTerrain(PointFactory.point(3, 2), TileType.Jungle);
		converted.setGround(PointFactory.point(3, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 3), TileType.Jungle);
		converted.setGround(PointFactory.point(3, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 4), TileType.Plains);
		converted.setGround(PointFactory.point(3, 4), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 4),
				new Village(TownStatus.Active, "", 1, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(3, 5), TileType.Plains);
		converted.setGround(PointFactory.point(3, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 5), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(3, 5),
				new Meadow(FIELD_TYPE, true, true, 13, Growing));
		converted.setBaseTerrain(PointFactory.point(3, 6), TileType.Plains);
		converted.setGround(PointFactory.point(3, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 6), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 7), TileType.Plains);
		converted.setGround(PointFactory.point(3, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 7), new Forest(TEMP_TREE, false));

		converted.setBaseTerrain(PointFactory.point(4, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 0), true);
		converted.setGround(PointFactory.point(4, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 0),
				new Village(TownStatus.Active, "", 2, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(4, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 1), true);
		converted.setGround(PointFactory.point(4, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(4, 1), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(4, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 2), true);
		converted.setGround(PointFactory.point(4, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 3), true);
		converted.setGround(PointFactory.point(4, 3), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(4, 3), new Grove(true, true, "fruit1", 11));
		converted.setBaseTerrain(PointFactory.point(4, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 4), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 4),
				new Meadow(FIELD_TYPE, true, true, 16, Growing));
		converted.setBaseTerrain(PointFactory.point(4, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 6), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 0), true);
		converted.setGround(PointFactory.point(5, 0), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(5, 0), new Grove(true, true, "fruit1", 8));
		converted.setBaseTerrain(PointFactory.point(5, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 1), true);
		converted.setGround(PointFactory.point(5, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 1),
				new Meadow(FIELD_TYPE, true, true, 5, Growing));
		converted.setBaseTerrain(PointFactory.point(5, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 2), true);
		converted.setGround(PointFactory.point(5, 2), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(5, 2), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(5, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 3), true);
		converted.setGround(PointFactory.point(5, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(5, 4), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(5, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 6), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 0), true);
		converted.setGround(PointFactory.point(6, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 1), true);
		converted.setGround(PointFactory.point(6, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 2), true);
		converted.setGround(PointFactory.point(6, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 3), true);
		converted.setGround(PointFactory.point(6, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(6, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(6, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 5),
				new Meadow(FIELD_TYPE, true, true, 15, Growing));
		converted.setBaseTerrain(PointFactory.point(6, 6), TileType.Tundra);
		converted.setGround(PointFactory.point(6, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 6),
				new Grove(true, true, "fruit1", 7));
		converted.setBaseTerrain(PointFactory.point(6, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(6, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 0), true);
		converted.setGround(PointFactory.point(7, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 1), true);
		converted.setGround(PointFactory.point(7, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(7, 1), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(7, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 2), true);
		converted.setGround(PointFactory.point(7, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 3), true);
		converted.setGround(PointFactory.point(7, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 5),
				new Meadow(FIELD_TYPE, true, true, 14, Growing));
		converted.setBaseTerrain(PointFactory.point(7, 6), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(7, 6), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(7, 6),
				new Village(TownStatus.Active, "", 3, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(7, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 7), new Ground(ROCK_TYPE, false));

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter(); StringWriter outTwo = new StringWriter()) {
			final SPFluidWriter writer = new SPFluidWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne, "")));
			assertThat("Two runs produce identical results", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		assertThat("Actual is at least subset of expected converted", converted.isSubset(
				new OneToTwoConverter().convert(original, true), SystemOut.SYS_OUT,
				""), equalTo(true));
	}
	/**
	 * Test more version-1 to version-2 conversion.
	 *
	 * @throws IOException on I/O error causing test failure
	 *
	 * TODO: Make the converter (and the tests) pass the map size to the encounter tables.
	 */
	@SuppressWarnings({"deprecation", "boxing", "static-method"})
	@Test
	public void testThirdOneToTwoConversion() throws IOException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		original.setGround(PointFactory.point(0, 0), new Ground(ROCK_TYPE, false));
		original.setBaseTerrain(PointFactory.point(0, 1), TileType.BorealForest);
		original.setForest(PointFactory.point(0, 1), new Forest(TEMP_TREE, false));
		original.addFixture(PointFactory.point(0, 1), new Hill(1));
		original.addFixture(PointFactory.point(0, 1),
				new Animal("animalKind", false, false, "wild", 2));
		original.addFixture(PointFactory.point(0, 1),
				new Mine("mineral", TownStatus.Active, 3));
		final Player independent = new Player(2, "independent");
		original.addFixture(PointFactory.point(0, 1),
				new AdventureFixture(independent, "briefDescription", "fullDescription",
											4));
		original.addFixture(PointFactory.point(0, 1), new Simurgh(5));
		original.addFixture(PointFactory.point(0, 1), new Griffin(6));
		original.addFixture(PointFactory.point(0, 1),
				new City(TownStatus.Ruined, TownSize.Large, 0, "cityName", 7,
								independent));
		original.addFixture(PointFactory.point(0, 1), new Ogre(8));
		original.addFixture(PointFactory.point(0, 1), new Minotaur(9));
		original.addFixture(PointFactory.point(0, 1), new Centaur("hill", 10));
		original.addFixture(PointFactory.point(0, 1), new Giant("frost", 11));
		original.addFixture(PointFactory.point(0, 1), new Djinn(12));
		original.addFixture(PointFactory.point(0, 1), new Fairy("lesser", 13));
		original.addFixture(PointFactory.point(0, 1), new Dragon("ice", 14));
		original.addFixture(PointFactory.point(0, 1), new Troll(15));
		original.addFixture(PointFactory.point(0, 1),
				new Fortification(TownStatus.Burned, TownSize.Medium, 0, "townName", 16,
										 independent));
		original.addFixture(PointFactory.point(0, 1),
				new StoneDeposit(StoneKind.Conglomerate, 0, 17));
		original.setBaseTerrain(PointFactory.point(1, 0), TileType.Mountain);
		original.setBaseTerrain(PointFactory.point(1, 1), TileType.Tundra);
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

		converted.setGround(PointFactory.point(0, 0), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(0, 1), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(0, 2), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(0, 2),
				new Meadow(FIELD_TYPE, true, true, 24, Growing));
		converted.setGround(PointFactory.point(0, 3), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(0, 3),
				new Meadow(FIELD_TYPE, true, true, 28, Growing));
		converted.setBaseTerrain(PointFactory.point(0, 4), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 4), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 4), new Hill(1));
		converted.addFixture(PointFactory.point(0, 4), new Centaur("hill", 10));
		converted.addFixture(PointFactory.point(0, 4),
				new Meadow(FIELD_TYPE, true, true, 32, Growing));
		converted.setBaseTerrain(PointFactory.point(0, 5), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 5), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 5), new Troll(15));
		converted.addFixture(PointFactory.point(0, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(0, 6), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 6), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 6),
				new AdventureFixture(independent, "briefDescription", "fullDescription",
											4));
		converted.setBaseTerrain(PointFactory.point(0, 7), TileType.Steppe);
		converted.setGround(PointFactory.point(0, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(0, 7), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(0, 7), new Griffin(6));
		converted.addFixture(PointFactory.point(0, 7), new Forest(TEMP_TREE, false));
		converted.setGround(PointFactory.point(1, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 0), new Forest(TEMP_TREE, false));
		converted.setGround(PointFactory.point(1, 1), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(1, 2), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(1, 3), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 3), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(1, 3),
				new Village(TownStatus.Active, "", 0, independent, "dwarf"));
		converted.setBaseTerrain(PointFactory.point(1, 4), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 4), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(1, 4),
				new Mine("mineral", TownStatus.Active, 3));
		converted.addFixture(PointFactory.point(1, 4),
				new Meadow(FIELD_TYPE, true, true, 22, Growing));
		converted.setBaseTerrain(PointFactory.point(1, 5), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 5), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(1, 5), new Giant("frost", 11));
		converted.addFixture(PointFactory.point(1, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(1, 6), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 6), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(1, 6),
				new StoneDeposit(StoneKind.Conglomerate, 0, 17));
		converted.setBaseTerrain(PointFactory.point(1, 7), TileType.Steppe);
		converted.setGround(PointFactory.point(1, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(1, 7), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(1, 7), new Dragon("ice", 14));
		converted.addFixture(PointFactory.point(1, 7),
				new Meadow(FIELD_TYPE, true, true, 21, Growing));
		converted.setGround(PointFactory.point(2, 0), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(2, 1), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(2, 2), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(2, 2),
				new Grove(true, true, "fruit1", 29));
		converted.setGround(PointFactory.point(2, 3), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(2, 3),
				new Meadow(FIELD_TYPE, true, true, 27, Growing));
		converted.setBaseTerrain(PointFactory.point(2, 4), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 4), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(2, 4), new Fairy("lesser", 13));
		converted.addFixture(PointFactory.point(2, 4),
				new Meadow(FIELD_TYPE, true, true, 35, Growing));
		converted.setBaseTerrain(PointFactory.point(2, 5), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(2, 5),
				new Fortification(TownStatus.Burned, TownSize.Medium, 0, "townName", 16,
										 independent));
		converted.addFixture(PointFactory.point(2, 5),
				new Meadow(FIELD_TYPE, true, true, 34, Growing));
		converted.setBaseTerrain(PointFactory.point(2, 6), TileType.Steppe);
		converted.addRivers(PointFactory.point(2, 6), River.Lake, River.South);
		converted.setGround(PointFactory.point(2, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 6), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(2, 6), new Djinn(12));
		converted.addFixture(PointFactory.point(2, 6),
				new Village(TownStatus.Active, "", 18, independent, "dwarf"));
		converted.addFixture(PointFactory.point(2, 6), new TextFixture(OneToTwoConverter.MAX_ITER_WARN, 10));
		converted.addFixture(PointFactory.point(2, 6),
				new Meadow(FIELD_TYPE, true, true, 38, Growing));
		converted.setBaseTerrain(PointFactory.point(2, 7), TileType.Steppe);
		converted.setGround(PointFactory.point(2, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(2, 7), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(2, 7), new Ogre(8));
		converted.setGround(PointFactory.point(3, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 0), new Grove(true, true, "fruit1", 23));
		converted.setGround(PointFactory.point(3, 1), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(3, 2), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(3, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 4), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 4), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 4), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(3, 4), new Simurgh(5));
		converted.addFixture(PointFactory.point(3, 4), new Grove(true, true, "fruit1", 33));
		converted.setBaseTerrain(PointFactory.point(3, 5), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 5), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 5), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(3, 5), new Minotaur(9));
		converted.addFixture(PointFactory.point(3, 5), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(3, 6), TileType.Steppe);
		converted.addRivers(PointFactory.point(3, 6), River.North, River.South);
		converted.setGround(PointFactory.point(3, 6), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 6), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(3, 6),
				new City(TownStatus.Ruined, TownSize.Large, 0, "cityName", 7,
								independent));
		converted.setBaseTerrain(PointFactory.point(3, 7), TileType.Steppe);
		converted.setGround(PointFactory.point(3, 7), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(3, 7), new Forest(BOREAL_TREE, false));
		converted.addFixture(PointFactory.point(3, 7),
				new Animal("animalKind", false, false, "wild", 2));
		converted.addFixture(PointFactory.point(3, 7),
				new Grove(true, true, "fruit1", 36));
		converted.setBaseTerrain(PointFactory.point(4, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 0), true);
		converted.setGround(PointFactory.point(4, 0), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(4, 0), new Forest(TEMP_TREE, false));
		converted.addFixture(PointFactory.point(4, 0),
				new Village(TownStatus.Active, "", 19, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(4, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 1), true);
		converted.setGround(PointFactory.point(4, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 2), true);
		converted.setGround(PointFactory.point(4, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(4, 3), true);
		converted.setGround(PointFactory.point(4, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 6), TileType.Tundra);
		converted.addRivers(PointFactory.point(4, 6), River.North, River.South);
		converted.setGround(PointFactory.point(4, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 6),
				new Meadow(FIELD_TYPE, true, true, 39, Growing));
		converted.setBaseTerrain(PointFactory.point(4, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(4, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 7),
				new Meadow(FIELD_TYPE, true, true, 25, Growing));
		converted.setBaseTerrain(PointFactory.point(5, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 0), true);
		converted.setGround(PointFactory.point(5, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 1), true);
		converted.setGround(PointFactory.point(5, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(5, 1),
				new Grove(true, true, "fruit1", 26));
		converted.setBaseTerrain(PointFactory.point(5, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 2), true);
		converted.setGround(PointFactory.point(5, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(5, 3), true);
		converted.setGround(PointFactory.point(5, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 6), TileType.Tundra);
		converted.addRivers(PointFactory.point(5, 6), River.North, River.South);
		converted.setGround(PointFactory.point(5, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(5, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 0), true);
		converted.addRivers(PointFactory.point(6, 0), River.East, River.West);
		converted.setGround(PointFactory.point(6, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 1), true);
		converted.addRivers(PointFactory.point(6, 1), River.East, River.West);
		converted.setGround(PointFactory.point(6, 1), new Ground(ROCK_TYPE, false));
		converted.setForest(PointFactory.point(6, 1), new Forest(TEMP_TREE, false));
		converted.setBaseTerrain(PointFactory.point(6, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 2), true);
		converted.addRivers(PointFactory.point(6, 2), River.East, River.West);
		converted.setGround(PointFactory.point(6, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(6, 3), true);
		converted.addRivers(PointFactory.point(6, 3), River.East, River.West);
		converted.setGround(PointFactory.point(6, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 4), TileType.Tundra);
		converted.addRivers(PointFactory.point(6, 4), River.East, River.West);
		converted.setGround(PointFactory.point(6, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 5), TileType.Tundra);
		converted.addRivers(PointFactory.point(6, 5), River.East, River.West);
		converted.setGround(PointFactory.point(6, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 5),
				new Meadow(FIELD_TYPE, true, true, 30, Growing));
		converted.setBaseTerrain(PointFactory.point(6, 6), TileType.Tundra);
		converted.addRivers(PointFactory.point(6, 6), River.North, River.West);
		converted.setGround(PointFactory.point(6, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(6, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 7),
				new Meadow(FIELD_TYPE, true, true, 37, Growing));
		converted.setBaseTerrain(PointFactory.point(7, 0), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 0), true);
		converted.setGround(PointFactory.point(7, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 1), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 1), true);
		converted.setGround(PointFactory.point(7, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 2), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 2), true);
		converted.setGround(PointFactory.point(7, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 3), TileType.Plains);
		converted.setMountainous(PointFactory.point(7, 3), true);
		converted.setGround(PointFactory.point(7, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 4), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 5), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 5),
				new Meadow(FIELD_TYPE, true, true, 31, Growing));
		converted.setBaseTerrain(PointFactory.point(7, 6), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 6),
				new Village(TownStatus.Active, "", 20, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(7, 7), TileType.Tundra);
		converted.setGround(PointFactory.point(7, 7), new Ground(ROCK_TYPE, false));

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter(); StringWriter outTwo = new StringWriter()) {
			final SPFluidWriter writer = new SPFluidWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne, "")));
			assertThat("Two runs produce identical results", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		assertThat("Actual is at least subset of expected converted", converted.isSubset(
				new OneToTwoConverter().convert(original, true), SystemOut.SYS_OUT,
				""), equalTo(true));
	}
	/**
	 * Test more version-1 to version-2 conversion.
	 *
	 * @throws IOException on I/O error causing test failure
	 *
	 * TODO: Make the converter (and the tests) pass the map size to the encounter tables.
	 */
	@SuppressWarnings({"deprecation", "boxing", "static-method"})
	@Test
	public void testFourthOneToTwoConversion() throws IOException {
		final IMutableMapNG original =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		original.setBaseTerrain(PointFactory.point(0, 0), TileType.Ocean);
		original.setBaseTerrain(PointFactory.point(0, 1), TileType.Desert);
		original.setBaseTerrain(PointFactory.point(1, 0), TileType.Desert);
		original.setBaseTerrain(PointFactory.point(1, 1), TileType.Desert);
		final Player player = new Player(1, "playerName");
		original.addPlayer(player);
		final Player independent = new Player(2, "independent");
		original.addPlayer(independent);

		final IMutableMapNG converted =
				new SPMapNG(new MapDimensions(8, 8, 2), new PlayerCollection(), -1);
		converted.addPlayer(player);
		converted.addPlayer(independent);
		converted.setBaseTerrain(PointFactory.point(0, 0), TileType.Ocean);
		converted.setGround(PointFactory.point(0, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(0, 0),
				new Village(TownStatus.Active, "", 0, independent, "dwarf"));
		converted.setBaseTerrain(PointFactory.point(0, 1), TileType.Ocean);
		converted.setGround(PointFactory.point(0, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 2), TileType.Ocean);
		converted.setGround(PointFactory.point(0, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 3), TileType.Ocean);
		converted.setGround(PointFactory.point(0, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 4), TileType.Desert);
		converted.setGround(PointFactory.point(0, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 5), TileType.Desert);
		converted.setGround(PointFactory.point(0, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 6), TileType.Plains);
		converted.setGround(PointFactory.point(0, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(0, 7), TileType.Plains);
		converted.setGround(PointFactory.point(0, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 0), TileType.Ocean);
		converted.setGround(PointFactory.point(1, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 1), TileType.Ocean);
		converted.setGround(PointFactory.point(1, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 2), TileType.Ocean);
		converted.setGround(PointFactory.point(1, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 3), TileType.Ocean);
		converted.setGround(PointFactory.point(1, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 4), TileType.Desert);
		converted.setGround(PointFactory.point(1, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 5), TileType.Plains);
		converted.setGround(PointFactory.point(1, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 6), TileType.Plains);
		converted.setGround(PointFactory.point(1, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(1, 7), TileType.Plains);
		converted.setGround(PointFactory.point(1, 7), new Ground(ROCK_TYPE, false));
		converted.setGround(PointFactory.point(1, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 0), TileType.Ocean);
		converted.setGround(PointFactory.point(2, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 1), TileType.Ocean);
		converted.setGround(PointFactory.point(2, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 2), TileType.Ocean);
		converted.setGround(PointFactory.point(2, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 3), TileType.Ocean);
		converted.setGround(PointFactory.point(2, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 4), TileType.Plains);
		converted.setGround(PointFactory.point(2, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 5), TileType.Plains);
		converted.setGround(PointFactory.point(2, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(2, 6), TileType.Desert);
		converted.setGround(PointFactory.point(2, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(2, 6), new Grove(true, true, "fruit1", 4));
		converted.setBaseTerrain(PointFactory.point(2, 7), TileType.Desert);
		converted.setGround(PointFactory.point(2, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(2, 7),
				new Meadow(FIELD_TYPE, true, true, 5, Growing));
		converted.setBaseTerrain(PointFactory.point(3, 0), TileType.Ocean);
		converted.setGround(PointFactory.point(3, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 1), TileType.Ocean);
		converted.setGround(PointFactory.point(3, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 2), TileType.Ocean);
		converted.setGround(PointFactory.point(3, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 3), TileType.Ocean);
		converted.setGround(PointFactory.point(3, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 4), TileType.Plains);
		converted.setGround(PointFactory.point(3, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 5), TileType.Desert);
		converted.setGround(PointFactory.point(3, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(3, 6), TileType.Desert);
		converted.setGround(PointFactory.point(3, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 6),
				new Meadow(FIELD_TYPE, true, true, 11, Growing));
		converted.setBaseTerrain(PointFactory.point(3, 7), TileType.Plains);
		converted.setGround(PointFactory.point(3, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(3, 7),
				new Village(TownStatus.Active, "", 1, independent, "human"));

		converted.setBaseTerrain(PointFactory.point(4, 0), TileType.Desert);
		converted.setGround(PointFactory.point(4, 0), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 0),
				new Village(TownStatus.Active, "", 2, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(4, 1), TileType.Desert);
		converted.setGround(PointFactory.point(4, 1), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 1),
				new Meadow(FIELD_TYPE, true, true, 8, Growing));
		converted.setBaseTerrain(PointFactory.point(4, 2), TileType.Desert);
		converted.setGround(PointFactory.point(4, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 3), TileType.Desert);
		converted.setGround(PointFactory.point(4, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 4), TileType.Plains);
		converted.setGround(PointFactory.point(4, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 5), TileType.Desert);
		converted.setGround(PointFactory.point(4, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(4, 6), TileType.Desert);
		converted.setGround(PointFactory.point(4, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 6),
				new Grove(true, true, "fruit1", 14));
		converted.setBaseTerrain(PointFactory.point(4, 7), TileType.Desert);
		converted.setGround(PointFactory.point(4, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(4, 7),
				new Meadow(FIELD_TYPE, true, true, 6, Growing));
		converted.setBaseTerrain(PointFactory.point(5, 0), TileType.Desert);
		converted.setGround(PointFactory.point(5, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 1), TileType.Desert);
		converted.setGround(PointFactory.point(5, 1), new Ground(ROCK_TYPE, false));
		converted
				.addFixture(PointFactory.point(5, 1), new Grove(true, true, "fruit1", 7));
		converted.setBaseTerrain(PointFactory.point(5, 2), TileType.Desert);
		converted.setGround(PointFactory.point(5, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 3), TileType.Plains);
		converted.setGround(PointFactory.point(5, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 4), TileType.Plains);
		converted.setGround(PointFactory.point(5, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 5), TileType.Desert);
		converted.setGround(PointFactory.point(5, 5), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 6), TileType.Plains);
		converted.setGround(PointFactory.point(5, 6), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(5, 7), TileType.Desert);
		converted.setGround(PointFactory.point(5, 7), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 0), TileType.Desert);
		converted.setGround(PointFactory.point(6, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 1), TileType.Desert);
		converted.setGround(PointFactory.point(6, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 2), TileType.Plains);
		converted.setGround(PointFactory.point(6, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 3), TileType.Plains);
		converted.setGround(PointFactory.point(6, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 4), TileType.Plains);
		converted.setGround(PointFactory.point(6, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(6, 5), TileType.Desert);
		converted.setGround(PointFactory.point(6, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 5), new Grove(true, true, "fruit1", 9));
		converted.setBaseTerrain(PointFactory.point(6, 6), TileType.Desert);
		converted.setGround(PointFactory.point(6, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 6),
				new Meadow(FIELD_TYPE, true, true, 12, Growing));
		converted.setBaseTerrain(PointFactory.point(6, 7), TileType.Desert);
		converted.setGround(PointFactory.point(6, 7), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(6, 7),
				new Meadow(FIELD_TYPE, true, true, 13, Growing));
		converted.setBaseTerrain(PointFactory.point(7, 0), TileType.Desert);
		converted.setGround(PointFactory.point(7, 0), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 1), TileType.Desert);
		converted.setGround(PointFactory.point(7, 1), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 2), TileType.Plains);
		converted.setGround(PointFactory.point(7, 2), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 3), TileType.Desert);
		converted.setGround(PointFactory.point(7, 3), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 4), TileType.Plains);
		converted.setGround(PointFactory.point(7, 4), new Ground(ROCK_TYPE, false));
		converted.setBaseTerrain(PointFactory.point(7, 5), TileType.Desert);
		converted.setGround(PointFactory.point(7, 5), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 5),
				new Grove(true, true, "fruit1", 10));
		converted.setBaseTerrain(PointFactory.point(7, 6), TileType.Desert);
		converted.setGround(PointFactory.point(7, 6), new Ground(ROCK_TYPE, false));
		converted.addFixture(PointFactory.point(7, 6),
				new Village(TownStatus.Active, "", 3, independent, "human"));
		converted.setBaseTerrain(PointFactory.point(7, 7), TileType.Plains);
		converted.setGround(PointFactory.point(7, 7), new Ground(ROCK_TYPE, false));

//		new SPFluidWriter().write(SystemOut.SYS_OUT,
//				new OneToTwoConverter().convert(original, true));
		try (StringWriter outOne = new StringWriter(); StringWriter outTwo = new StringWriter()) {
			final SPFluidWriter writer = new SPFluidWriter();
			writer.write(outOne, converted);
			writer.write(outTwo, new OneToTwoConverter().convert(original, true));
			assertThat("Produces expected result", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		try (StringWriter outOne = new StringWriter();
			 StringWriter outTwo = new StringWriter()) {
			assertThat("Products of two runs are both or neither subsets of expected",
					converted.isSubset(
							new OneToTwoConverter().convert(original, true), outTwo, ""),
					equalTo(converted.isSubset(
							new OneToTwoConverter().convert(original, true), outOne, "")));
			assertThat("Two runs produce identical results", outTwo.toString(),
					equalTo(outOne.toString()));
		}
		assertThat("Actual is at least subset of expected converted", converted.isSubset(
				new OneToTwoConverter().convert(original, true), SystemOut.SYS_OUT,
				""), equalTo(true));
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
	private static <T, U extends T> boolean doesStreamContain(final Stream<T> stream,
															final U item) {
		return stream.anyMatch(each -> Objects.equals(each, item));
	}

	/**
	 * Test version-0 to version-1 conversion.
	 * @throws IOException on I/O error causing test failure
	 * @throws XMLStreamException on malformed XML in tests
	 * @throws SPFormatException on malformed SP XML in tests
	 */
	@SuppressWarnings({ "deprecation", "static-method" })
	@Test
	public void testZeroToOneConversion()
			throws XMLStreamException, IOException, SPFormatException {
		// FIXME: Include tile fixtures beyond those implicit in events
		final String orig =
				"<map xmlns:sp=\"" + ISPReader.NAMESPACE + "\" version='0' rows='2' columns='2'><player number='0' " +
						"code_name='Test Player' /><row index='0'><tile row='0' " +
						"column='0' type='tundra' event='0'>Random event here</tile><tile row='0' " +
						"column='1' type='boreal_forest' event='183'></tile></row><row " +
						"index='1'><sp:tile row='1' column='0' type='mountain' " +
						"event='229'><sp:fortress name='HQ' owner='0' id='15'/></sp:tile><tile row='1' column='1' " +
						"type='temperate_forest' event='219'></tile></row></map>";
		final StringWriter out = new StringWriter();
		//noinspection unchecked
		ZeroToOneConverter.convert(new IteratorWrapper<>(XMLInputFactory.newInstance()
																.createXMLEventReader(
																		new StringReader(orig))),
				out);
		final StringWriter actualXML = new StringWriter();
		CompactXMLWriter.writeSPObject(actualXML, new MapReaderAdapter()
														.readMapFromStream(
																new StringReader(out.toString()),
																Warning.Ignore));
		final IMutableMapNG expected =
				new SPMapNG(new MapDimensions(2, 2, 1), new PlayerCollection(), 0);
		final Player player = new Player(0, "Test Player");
		expected.addPlayer(player);
		expected.setBaseTerrain(PointFactory.point(0, 0), TileType.Tundra);
		expected.addFixture(PointFactory.point(0, 0), new TextFixture("Random event here", -1));
		expected.setBaseTerrain(PointFactory.point(0, 1), TileType.BorealForest);
		expected.setBaseTerrain(PointFactory.point(1, 0), TileType.Mountain);
		expected.setBaseTerrain(PointFactory.point(1, 1), TileType.TemperateForest);
		expected.addFixture(PointFactory.point(1, 0),
				new Town(TownStatus.Burned, TownSize.Small, 0, "", 0,
								new Player(-1, "Independent")));
		expected.addFixture(PointFactory.point(1, 0), new Fortress(player, "HQ", 15));
		expected.addFixture(PointFactory.point(1, 1), new MineralVein("coal", true, 0, 1));

		final StringWriter expectedXML = new StringWriter();
		CompactXMLWriter.writeSPObject(expectedXML, expected);
		assertThat("Converted map's serialized form was as expected",
				actualXML.toString(), equalTo(expectedXML.toString()));
		assertThat("Converted map was as expected",
				new MapReaderAdapter()
						.readMapFromStream(new StringReader(out.toString()),
								Warning.Ignore), equalTo(expected));
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
