package model.map;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import org.junit.Test;
import util.Pair;
import util.Warning.Action;

import static model.map.PointFactory.point;
import static model.map.River.Lake;
import static model.map.TileType.Desert;
import static model.map.TileType.Jungle;
import static model.map.TileType.NotVisible;
import static model.map.TileType.Plains;
import static model.map.TileType.Steppe;
import static org.junit.Assert.assertEquals;
import static util.NullCleaner.assertNotNull;

/**
 * A class to test the serialization of XMLWritable objects other than Fixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class TestSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	private static final Pattern KIND_PATTERN =
			Pattern.compile(KIND_PROPERTY, Pattern.LITERAL);

	/**
	 * Test Player serialization.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testPlayerSerialization() throws XMLStreamException,
														 SPFormatException, IOException {
		assertSerialization("First Player serialization test, reflection",
				new Player(1, "one"), Player.class);
		assertSerialization("Second Player serialization test, reflection",
				new Player(2, "two"), Player.class);
		assertUnwantedChild(
				"<player code_name=\"one\" number=\"1\"><troll /></player>",
				Player.class, false);
		assertMissingProperty("<player code_name=\"one\" />", Player.class,
				"number", false);
		assertMissingProperty("<player number=\"1\" />", Player.class,
				"code_name", false);
	}

	/**
	 * A factory to encapsulate rivers in a simple map.
	 *
	 * @param point  where to put the rivers
	 * @param rivers the rivers to put on a tile
	 * @return a map containing them. Declared mutable for the sake of calling code.
	 */
	private static IMapNG encapsulateRivers(final Point point,
											final River... rivers) {
		final IMutableMapNG retval =
				new SPMapNG(new MapDimensions(point.row + 1, point.col + 1, 2),
								   new PlayerCollection(), -1);
		retval.setBaseTerrain(point, Plains);
		retval.addRivers(point, rivers);
		return retval;
	}

	/**
	 * Encapsulate the given string in a 'tile' tag inside a 'map' tag.
	 *
	 * @param str a string
	 * @return it, encapsulated.
	 */
	private static String encapsulateTileString(final String str) {
		final StringBuilder builder = new StringBuilder(55 + str.length());
		builder.append("<map version=\"2\" rows=\"2\" columns=\"2\">");
		builder.append("<tile row=\"1\" column=\"1\" kind=\"plains\">");
		builder.append(str);
		builder.append("</tile></map>");
		return assertNotNull(builder.toString());
	}

	/**
	 * Test River serialization.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testRiverSerializationOne() throws XMLStreamException,
														   SPFormatException,
														   IOException {
		for (final River river : River.values()) {
			assert river != null;
			assertSerialization("River alone", river, River.class);
		}
		assertUnwantedChild(encapsulateTileString("<lake><troll /></lake>"),
				IMapNG.class, false);
		assertMissingProperty(encapsulateTileString("<river />"), IMapNG.class,
				"direction", false);
		final Point point = point(0, 0);
		assertSerialization("River in tile",
				encapsulateRivers(point, River.East), IMapNG.class);
		assertSerialization("Lake in tile",
				encapsulateRivers(point, Lake), IMapNG.class);
		assertSerialization("Another river in tile",
				encapsulateRivers(point, River.North), IMapNG.class);
		final Set<River> setOne = EnumSet.noneOf(River.class);
		final Set<River> setTwo = EnumSet.noneOf(River.class);
		assertEquals("Empty sets are equal", setOne, setTwo);
		setOne.add(River.North);
		setOne.add(River.South);
		setTwo.add(River.South);
		setTwo.add(River.North);
		assertEquals("Rivers added in different order to set", setOne, setTwo);
		assertEquals("Rivers added in different order to fixture",
				new RiverFixture(River.North, River.South), new RiverFixture(
																					River.South,
																					River.North));
		final RiverFixture fixOne = new RiverFixture(River.North);
		fixOne.addRiver(River.South);
		final RiverFixture fixTwo = new RiverFixture(River.South);
		fixTwo.addRiver(River.North);
		assertEquals("Rivers added separately", fixOne, fixTwo);
		final Collection<TileFixture> hsetOne = new HashSet<>();
		hsetOne.add(fixOne);
		final Collection<TileFixture> hsetTwo = new HashSet<>();
		hsetTwo.add(fixTwo);
		assertEquals("Check Set.equals()", hsetOne, hsetTwo);
		assertEquals("Tile equality with rivers",
				encapsulateRivers(point(1, 1), River.North, River.South),
				encapsulateRivers(point(1, 1), River.North, River.South));
		assertEquals("Tile equality with different order of rivers",
				encapsulateRivers(point(1, 2), River.North, River.South),
				encapsulateRivers(point(1, 2), River.South, River.North));
		assertSerialization("Two rivers",
				encapsulateRivers(point, River.North, River.South), IMapNG.class);
	}

	/**
	 * Create a simple SPMapNG.
	 *
	 * @param dims    the dimensions of the map
	 * @param terrain the terrain to set at various points
	 * @return the map
	 */
	@SafeVarargs
	private static IMutableMapNG createSimpleMap(final Point dims,
												 final Pair<Point, TileType>...
														 terrain) {
		final IMutableMapNG retval =
				new SPMapNG(new MapDimensions(dims.row, dims.col, 2),
								   new PlayerCollection(), -1);
		for (final Pair<Point, TileType> pair : terrain) {
			retval.setBaseTerrain(pair.first(), pair.second());
		}
		return retval;
	}

	/**
	 * Test Tile serialization.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testTileSerialization() throws XMLStreamException,
													   SPFormatException, IOException {
		assertSerialization("Simple Tile",
				createSimpleMap(point(1, 1), Pair.of(point(0, 0), Desert)),
				IMapNG.class);
		final IMutableMapNG firstMap =
				createSimpleMap(point(2, 2), Pair.of(point(1, 1), Plains));
		firstMap.addFixture(point(1, 1), new Griffin(1));
		assertSerialization("Tile with one fixture", firstMap, IMapNG.class);
		final IMutableMapNG secondMap =
				createSimpleMap(point(3, 3), Pair.of(point(2, 2), Steppe));
		secondMap.addFixture(point(2, 2), new Unit(new Player(1, ""), "unitOne",
														  "firstUnit", 1));
		secondMap.setForest(point(2, 2), new Forest("forestKind", true));
		assertSerialization("Tile with two fixtures", secondMap, IMapNG.class);
		final IMutableMapNG thirdMap =
				createSimpleMap(point(4, 4), Pair.of(point(3, 3), Jungle));
		final Fortress fort = new Fortress(new Player(2, ""), "fortOne", 1);
		fort.addMember(new Unit(new Player(2, ""), "unitTwo", "secondUnit", 2));
		thirdMap.addFixture(point(3, 3), fort);
		thirdMap.addFixture(point(3, 3), new TextFixture("Random text here", 5));
		thirdMap.addRivers(point(3, 3), Lake);
		assertSerialization("More complex tile", thirdMap, IMapNG.class);
		final IMutableMapNG fourthMap =
				createSimpleMap(point(5, 5), Pair.of(point(4, 4), Plains));
		final String oldKindProperty = "type"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of deprecated tile-type idiom",
				fourthMap,
				assertNotNull(KIND_PATTERN.matcher(createSerializedForm(fourthMap, true))
									  .replaceAll(
											  Matcher.quoteReplacement
															  (oldKindProperty))),
				IMapNG.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated tile-type idiom", fourthMap,
				assertNotNull(KIND_PATTERN.matcher(createSerializedForm(fourthMap,
						false))
									  .replaceAll(
											  Matcher.quoteReplacement
															  (oldKindProperty))),
				IMapNG.class,
				oldKindProperty);
		assertMissingProperty("<map version=\"2\" rows=\"1\" columns=\"1\">"
									  + "<tile column=\"0\" kind=\"plains\" /></map>",
				IMapNG.class,
				"row", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" columns=\"1\">"
									  + "<tile row=\"0\" kind=\"plains\" /></map>",
				IMapNG.class,
				"column", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" columns=\"1\">"
									  + "<tile row=\"0\" column=\"0\" /></map>",
				IMapNG.class,
				KIND_PROPERTY, true);
		assertUnwantedChild(
				encapsulateTileString("<tile row=\"2\" column=\"0\" "
											  + "kind=\"plains\" />"), IMapNG.class,
				false);
		final IMutableMapNG five =
				createSimpleMap(point(3, 4), Pair.of(point(2, 3), Jungle));
		five.addFixture(point(2, 3), new Unit(
													 new Player(2, ""), "explorer",
													 "name one", 1));
		five.addFixture(point(2, 3), new Unit(
													 new Player(2, ""), "explorer",
													 "name two", 2));
		assertEquals("Just checking ...", 2,
				iteratorSize(five.getOtherFixtures(point(2, 3))));
		assertSerialization("Multiple units should come through", five,
				IMapNG.class);
		final String xmlTwo =
				"<view current_player=\"-1\" current_turn=\"-1\">\n\t<map version=\"2\"" +
						" rows=\"3\" columns=\"4\">\n\t\t<row index=\"2\">\n\t\t\t<tile" +
						" row=\"2\" column=\"3\" kind=\"jungle\">\n\t\t\t\t<unit " +
						"owner=\"2\" kind=\"explorer\" name=\"name one\" id=\"1\" />\n" +
						"\t\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name two\"" +
						" id=\"2\" />\n\t\t\t</tile>\n\t\t</row>\n\t</map>\n</view>\n";
		assertEquals("Multiple units", xmlTwo, createSerializedForm(five, true));
		assertEquals("Multiple units", xmlTwo,
				createSerializedForm(five, false));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"<view current_player=\"-1\" current_turn=\"-1\">\n\t"
						+ "<map version=\"2\" rows=\"1\" columns=\"1\" />\n"
						+ "</view>\n",
				createSerializedForm(
						createSimpleMap(point(1, 1),
								Pair.of(point(0, 0), NotVisible)), true));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"<view current_player=\"-1\" current_turn=\"-1\">\n\t"
						+ "<map version=\"2\" rows=\"1\" columns=\"1\">\n\t"
						+ "</map>\n</view>\n",
				createSerializedForm(
						createSimpleMap(point(1, 1),
								Pair.of(point(0, 0), NotVisible)), false));
		assertImageSerialization("Unit image property is preserved", new Unit(
																					 new
																							 Player(5,
																									   ""),
																					 "herder",
																					 "herderName",
																					 9),
				Unit.class);
	}

	/**
	 * Test that row nodes are ignored, and that "future" tags are skipped but warned
	 * about.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 */
	@Test
	public void testSkppableSerialization() throws XMLStreamException,
														   SPFormatException {
		assertEquivalentForms("Two maps, one with row tags, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><row /></map>",
				IMapNG.class, Action.Die);
		assertEquivalentForms("Two maps, one with future tag, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				IMapNG.class, Action.Ignore);
		assertUnsupportedTag(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				IMapNG.class, "future", true);
	}

	/**
	 * Test Map serialization ... primarily errors.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMapSerialization() throws XMLStreamException,
													  SPFormatException, IOException {
		assertUnwantedChild(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><hill /></map>",
				IMapNG.class, false);
		final Player player = new Player(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(),
								   -1);
		firstMap.addPlayer(player);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Plains);
		assertSerialization("Simple Map serialization", firstMap, IMapNG.class);
		assertMissingProperty("<map version=\"2\" columns=\"1\" />",
				IMapNG.class, "rows", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" />", IMapNG.class,
				"columns", false);
	}

	/**
	 * Test Map serialization ... primarily errors.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMapNGSerialization() throws XMLStreamException,
														SPFormatException, IOException {
		assertUnwantedChild(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><hill /></map>",
				SPMapNG.class, false);
		final Player player = new Player(1, "playerOne");
		player.setCurrent(true);
		final PlayerCollection players = new PlayerCollection();
		players.add(player);
		final SPMapNG firstMap = new SPMapNG(new MapDimensions(1, 1, 2), players, 0);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Plains);
		assertSerialization("Simple Map serialization", firstMap, SPMapNG.class);
		assertMissingProperty("<map version=\"2\" columns=\"1\" />",
				SPMapNG.class, "rows", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" />", SPMapNG.class,
				"columns", false);
	}

	/**
	 * Test view serialization.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testViewSerialization() throws XMLStreamException,
													   SPFormatException, IOException {
		final Player player = new Player(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(),
								   0);
		firstMap.addPlayer(player);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Steppe);
		assertSerialization("SPMapNG serialization", firstMap, IMapNG.class);
		assertMissingProperty("<view current_turn=\"0\">"
									  +
									  "<map version=\"2\" rows=\"1\" columns=\"1\" " +
									  "/></view>",
				IMapNG.class, "current_player", false);
		assertMissingProperty("<view current_player=\"0\">"
									  +
									  "<map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
				IMapNG.class, "current_turn", false);
		assertMissingChild("<view current_player=\"1\" current_turn=\"0\" />",
				IMapNG.class, false);
		assertUnwantedChild(
				"<view current_player=\"0\" current_turn=\"0\"><map version=\"2\" " +
						"rows=\"1\" columns=\"1\" /><map version=\"2\" rows=\"1\" " +
						"columns=\"1\" /></view>",
				IMapNG.class, false);
		assertUnwantedChild(
				"<view current_player=\"0\" current_turn=\"0\"><hill /></view>",
				IMapNG.class, false);
		assertMapDeserialization("Proper deserialization of map into view", firstMap,
				"<map version=\"2\" rows=\"1\" columns=\"1\" " +
						"current_player=\"1\"><player number=\"1\" " +
						"code_name=\"playerOne\" /><row index=\"0\"><tile row=\"0\" " +
						"column=\"0\" kind=\"steppe\" /></row></map>");
	}

	/**
	 * Test the <include> tag.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 */
	@Test
	public void testInclude() throws XMLStreamException, SPFormatException {
		assertForwardDeserialization("Reading Ogre via <include>", new Ogre(1),
				"<include file=\"string:&lt;ogre id=&quot;1&quot; /&gt;\" />",
				Ogre.class);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestSerialization";
	}
}
