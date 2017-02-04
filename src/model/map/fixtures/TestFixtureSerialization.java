package model.map.fixtures;

import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.ISPReader;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import model.map.BaseTestFixtureSerialization;
import model.map.HasMutableImage;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.PlayerCollection;
import model.map.PlayerImpl;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMapNG;
import model.map.TileType;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.SimpleImmortal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.TownSize;
import org.junit.Test;
import util.SingletonRandom;
import util.Warning;

/**
 * A class to test serialization of TileFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestFixtureSerialization extends
		BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Test the serialization of Animal, including catching format errors.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testAnimalSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Animal serialization",
				new Animal("animalOne", false, false, "wild", 0));
		assertSerialization("Second test of Animal serialization",
				new Animal("animalTwo", false, true, "semi-domesticated", 1));
		assertSerialization("Third test of Animal serialization",
				new Animal("animalThree", true, false, "domesticated", 2));
		final HasMutableImage
				fourthAnimal = new Animal("animalFour", true, true, "status", 3);
		assertSerialization("Fourth test of Animal serialization", fourthAnimal);
		assertUnwantedChild("<animal kind=\"animal\"><troll /></animal>",
				Animal.class, false);
		assertMissingProperty("<animal />", Animal.class, KIND_PROPERTY, false);
		assertForwardDeserialization("Forward-looking XML in re talking, reflection",
				new Animal("animalFive", false, false, "wild", 3),
				"<animal kind=\"animalFive\" talking=\"false\" id=\"3\" />");
		assertMissingProperty("<animal kind=\"animalSix\" talking=\"true\" />",
				Animal.class, "id", true);
		assertMissingProperty("<animal kind=\"animalEight\" id=\"nonNumeric\" />",
				Animal.class, "id", false);
		assertForwardDeserialization("Explicit default status of animal",
				new Animal("animalSeven", false, false, "wild", 4),
				"<animal kind=\"animalSeven\" status=\"wild\" id=\"4\" />");
		assertImageSerialization("Animal image property is preserved", fourthAnimal);
		assertForwardDeserialization("Namespaced attribute",
				new Animal("animalNine", true, true, "tame", 5),
				"<animal xmlns:sp=\"" + ISPReader.NAMESPACE +
						"\" sp:kind=\"animalNine\" sp:talking=\"true\" " +
						"sp:traces=\"true\" sp:status=\"tame\" sp:id=\"5\" />");
	}

	/**
	 * Test the serialization of CacheFixture, including catching format errors.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testCacheSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Cache serialization",
				new CacheFixture("kindOne", "contentsOne", 1));
		assertSerialization("Second test of Cache serialization",
				new CacheFixture("kindTwo", "contentsTwo", 2));
		assertUnwantedChild(
				"<cache kind=\"kind\" contents=\"cont\"><troll /></cache>",
				CacheFixture.class, false);
		assertMissingProperty("<cache contents=\"contents\" />",
				CacheFixture.class, KIND_PROPERTY, false);
		assertMissingProperty("<cache kind=\"kind\" />", CacheFixture.class,
				"contents", false);
		assertMissingProperty("<cache kind=\"ind\" contents=\"contents\" />",
				CacheFixture.class, "id", true);
		assertImageSerialization("Cache image property is preserved",
				new CacheFixture("kindThree", "contentsThree", 3));
	}

	/**
	 * Test the serialization of Centaurs, including catching format errors.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testCentaurSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Centaur serialization",
				new Centaur("firstCentaur", 0));
		assertSerialization("Second test of Centaur serialization",
				new Centaur("secondCentaur", 1));
		assertUnwantedChild("<centaur kind=\"forest\"><troll /></centaur>",
				Centaur.class, false);
		assertMissingProperty("<centaur />", Centaur.class, KIND_PROPERTY,
				false);
		assertMissingProperty("<centaur kind=\"kind\" />", Centaur.class, "id",
				true);
		assertImageSerialization("Centaur image property is preserved",
				new Centaur("thirdCentaur", 2));
	}

	/**
	 * Test the serialization of Dragons.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testDragonSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Dragon serialization", new Dragon("", 1));
		assertSerialization("Second test of Dragon serialization",
				new Dragon("secondDragon", 2));
		assertUnwantedChild("<dragon kind=\"ice\"><hill /></dragon>",
				Dragon.class, false);
		assertMissingProperty("<dragon />", Dragon.class, KIND_PROPERTY, false);
		assertMissingProperty("<dragon kind=\"kind\" />", Dragon.class, "id",
				true);
		assertImageSerialization("Dragon image property is preserved",
				new Dragon("thirdDragon", 3));
	}

	/**
	 * Test the serialization of Fairies.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testFairySerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Fairy serialization",
				new Fairy("oneFairy", 1));
		assertSerialization("Second test of Fairy serialization",
				new Fairy("twoFairy", 2));
		assertUnwantedChild("<fairy kind=\"great\"><hill /></fairy>",
				Fairy.class, false);
		assertMissingProperty("<fairy />", Fairy.class, KIND_PROPERTY, false);
		assertMissingProperty("<fairy kind=\"kind\" />", Fairy.class, "id",
				true);
		assertImageSerialization("Fairy image property is preserved",
				new Fairy("threeFairy", 3));
	}

	/**
	 * Test the serialization of Forests.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testForestSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Forest serialization",
				new Forest("firstForest", false, 1));
		assertSerialization("Second test of Forest serialization",
				new Forest("secondForest", true, 2));
		assertUnwantedChild("<forest kind=\"trees\"><hill /></forest>",
				Forest.class, false);
		assertMissingProperty("<forest />", Forest.class, KIND_PROPERTY, false);
		assertImageSerialization("Forest image property is preserved",
				new Forest("thirdForest", true, 3));
		final IMutableMapNG map =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), -1);
		final Point point = PointFactory.point(0, 0);
		map.setBaseTerrain(point, TileType.Plains);
		map.setForest(point, new Forest("trees", false, 4));
		map.addFixture(point, new Forest("secondForest", true, 5));
		assertSerialization("Map with multiple Forests on a tile", map);
		assertEquivalentForms("Duplicate Forests ignored", encapsulateTileString(
				"<forest kind=\"trees\" id=\"4\" /><forest kind=\"second\" rows=\"true\"" +
						" id=\"5\" />"),
				encapsulateTileString(
						"<forest kind=\"trees\" id=\"4\" /><forest kind=\"trees\" " +
								"id=\"4\" /><forest kind=\"second\" rows=\"true\" id=\"5\" />"),
			IMutableMapNG.class, Warning.Ignore);
	}

	/**
	 * Test the serialization of Fortresses.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testFortressSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		// Can't give player names because our test environment doesn't let us
		// pass a set of players in
		final Player firstPlayer = new PlayerImpl(1, "");
		assertSerialization("First test of Fortress serialization",
				new Fortress(firstPlayer, "one", 1, TownSize.Small));
		assertSerialization("Second test of Fortress serialization",
				new Fortress(firstPlayer, "two", 2, TownSize.Medium));
		final Player secondPlayer = new PlayerImpl(2, "");
		assertSerialization("Third test of Fortress serialization",
				new Fortress(secondPlayer, "three", 3, TownSize.Large));
		assertSerialization("Fourth test of Fortress serialization",
				new Fortress(secondPlayer, "four", 4, TownSize.Small));
		final Fortress five = new Fortress(secondPlayer, "five", 5, TownSize.Small);
		five.addMember(new Unit(secondPlayer, "unitOne", "unitTwo", 1));
		assertSerialization("Fifth test of Fortress serialization", five);
		assertUnwantedChild("<fortress><hill /></fortress>", Fortress.class,
				false);
		assertMissingProperty("<fortress />", Fortress.class, "owner", true);
		assertMissingProperty("<fortress owner=\"1\" />", Fortress.class,
				"name", true);
		assertMissingProperty("<fortress owner=\"1\" name=\"name\" />",
				Fortress.class, "id", true);
		assertImageSerialization("Fortress image property is preserved", five);
	}

	/**
	 * Test the serialization of Giants.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testGiantSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Giant serialization", new Giant("one", 1));
		assertSerialization("Second test of Giant serialization", new Giant("two", 2));
		assertUnwantedChild("<giant kind=\"hill\"><hill /></giant>",
				Giant.class, false);
		assertMissingProperty("<giant />", Giant.class, KIND_PROPERTY, false);
		assertMissingProperty("<giant kind=\"kind\" />", Giant.class, "id",
				true);
		assertImageSerialization("Giant image property is preserved",
				new Giant("three", 3));
	}

	/**
	 * Test the serialization of Ground Fixtures.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testGroundSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First test of Ground serialization",
				new Ground(1, "one", true));
		assertSerialization("Second test of Ground serialization",
				new Ground(2, "two", true));
		assertSerialization("Third test of Ground serialization",
				new Ground(3, "three", false));
		final Point point = PointFactory.point(0, 0);
		final IMutableMapNG map =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), -1);
		map.setBaseTerrain(point, TileType.Plains);
		map.setGround(point, new Ground(-1, "four", true));
		assertSerialization("Test that reader handles ground as a fixture", map);
		assertForwardDeserialization("Duplicate Ground ignored", map,
				"<view current_turn=\"-1\"><map version=\"2\" rows=\"1\" " +
						"columns=\"1\"><tile row=\"0\" column=\"0\" " +
						"kind=\"plains\"><ground kind=\"four\" exposed=\"true\" " +
						"/><ground kind=\"four\" exposed=\"true\" " +
						"/></tile></map></view>");
		map.addFixture(point, new Ground(-1, "five", false));
		assertForwardDeserialization("Exposed Ground made main", map,
				"<view current_turn=\"-1\"><map version=\"2\" rows=\"1\" " +
						"columns=\"1\"><tile row=\"0\" column=\"0\" " +
						"kind=\"plains\"><ground kind=\"five\" exposed=\"false\" " +
						"/><ground kind=\"four\" exposed=\"true\" " +
						"/></tile></map></view>");
		assertForwardDeserialization("Exposed Ground left as main", map,
				"<view current_turn=\"-1\"><map version=\"2\" rows=\"1\" " +
						"columns=\"1\"><tile row=\"0\" column=\"0\" " +
						"kind=\"plains\"><ground kind=\"four\" exposed=\"true\" " +
						"/><ground kind=\"five\" exposed=\"false\" " +
						"/></tile></map></view>");
		assertUnwantedChild(
				"<ground kind=\"sand\" exposed=\"true\"><hill /></ground>",
				Ground.class, false);
		assertMissingProperty("<ground />", Ground.class, KIND_PROPERTY, false);
		assertMissingProperty("<ground kind=\"ground\" />", Ground.class,
				"exposed", false);
		assertDeprecatedProperty(
				"<ground ground=\"ground\" exposed=\"true\" />", Ground.class,
				"ground", KIND_PROPERTY, "ground", true);
		assertImageSerialization("Ground image property is preserved",
				new Ground(5, "five", true));
	}

	/**
	 * Test that simple (no-parameter-except-ID) fixtures shouldn't have children.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testSimpleSerializationNoChildren()
			throws XMLStreamException, SPFormatException {
		assertUnwantedChild("<djinn><troll /></djinn>", SimpleImmortal.class, false);
		assertUnwantedChild("<griffin><djinn /></griffin>", SimpleImmortal.class,
				false);
		assertUnwantedChild("<hill><griffin /></hill>", Hill.class, false);
		assertUnwantedChild("<minotaur><troll /></minotaur>", SimpleImmortal.class,
				false);
		assertUnwantedChild("<oasis><troll /></oasis>", Oasis.class, false);
		assertUnwantedChild("<ogre><troll /></ogre>", SimpleImmortal.class, false);
		assertUnwantedChild("<phoenix><troll /></phoenix>", SimpleImmortal.class,
				false);
		assertUnwantedChild("<sandbar><troll /></sandbar>", Sandbar.class,
				false);
		assertUnwantedChild("<simurgh><troll /></simurgh>", SimpleImmortal.class,
				false);
		assertUnwantedChild("<sphinx><troll /></sphinx>", SimpleImmortal.class, false);
		assertUnwantedChild("<troll><troll /></troll>", SimpleImmortal.class, false);
	}

	/**
	 * Test that serialization of simple (no-parameter) fixtures preserves image
	 * property.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testSimpleImageSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		for (final SimpleImmortal.SimpleImmortalKind kind : SimpleImmortal
																	.SimpleImmortalKind
																	.values()) {
			assertImageSerialization(kind + " image property is preserved",
					new SimpleImmortal(kind, SingletonRandom.RANDOM.nextInt(1048576)));
		}
		assertImageSerialization("Hill image property is preserved", new Hill(3));
		assertImageSerialization("Oasis image property is preserved", new Oasis(3));
		assertImageSerialization("Sandbar image property is preserved", new Sandbar(3));
	}

	/**
	 * Test the serialization of simple (no-parameter) fixtures, including format errors.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testSimpleSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		for (final SimpleImmortal.SimpleImmortalKind kind : SimpleImmortal
																	.SimpleImmortalKind
																	.values()) {
			assertSerialization(kind + " serialization",
					new SimpleImmortal(kind, SingletonRandom.RANDOM.nextInt(1048576)));
		}
		assertMissingProperty("<djinn />", SimpleImmortal.class, "id", true);
		assertMissingProperty("<griffin />", SimpleImmortal.class, "id", true);
		assertSerialization("Hill serialization", new Hill(1));
		assertSerialization("Hill serialization", new Hill(2));
		assertMissingProperty("<hill />", Hill.class, "id", true);
		assertMissingProperty("<minotaur />", SimpleImmortal.class, "id", true);
		assertSerialization("Oasis serialization", new Oasis(1));
		assertSerialization("Oasis serialization", new Oasis(2));
		assertMissingProperty("<oasis />", Oasis.class, "id", true);
		assertMissingProperty("<ogre />", SimpleImmortal.class, "id", true);
		assertMissingProperty("<phoenix />", SimpleImmortal.class, "id", true);
		assertSerialization("Sandbar serialization", new Sandbar(1));
		assertSerialization("Sandbar serialization", new Sandbar(2));
		assertMissingProperty("<sandbar />", Sandbar.class, "id", true);
		assertMissingProperty("<simurgh />", SimpleImmortal.class, "id", true);
		assertMissingProperty("<sphinx />", SimpleImmortal.class, "id", true);
		assertMissingProperty("<troll />", SimpleImmortal.class, "id", true);
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestFixtureSerialization";
	}
}
