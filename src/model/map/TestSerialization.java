package model.map;

import controller.map.formatexceptions.SPFormatException;
import controller.map.formatexceptions.UnsupportedTagException;
import controller.map.formatexceptions.UnwantedChildException;
import controller.map.iointerfaces.ISPReader;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;
import org.junit.Test;
import util.Pair;
import util.Warning;

import static model.map.PointFactory.point;
import static model.map.River.Lake;
import static model.map.TileType.Desert;
import static model.map.TileType.Jungle;
import static model.map.TileType.NotVisible;
import static model.map.TileType.Plains;
import static model.map.TileType.Steppe;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
	/**
	 * Pre-compiled pattern for matching "kind".
	 */
	private static final Pattern KIND_PATTERN =
			assertNotNull(Pattern.compile(KIND_PROPERTY, Pattern.LITERAL));

	/**
	 * Test Player serialization.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testPlayerSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First Player serialization test, reflection",
				new Player(1, "one"));
		assertSerialization("Second Player serialization test, reflection",
				new Player(2, "two"));
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
				new SPMapNG(new MapDimensions(point.getRow() + 1, point.getCol() + 1, 2),
								new PlayerCollection(), -1);
		retval.setBaseTerrain(point, Plains);
		retval.addRivers(point, assertNotNull(rivers));
		return retval;
	}

	/**
	 * Encapsulate the given string in a 'tile' tag inside a 'map' tag.
	 *
	 * @param str a string
	 * @return it, encapsulated.
	 */
	@SuppressWarnings("StringBufferReplaceableByString")
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
	public void testRiverSerializationOne()
			throws XMLStreamException, SPFormatException, IOException {
		for (final River river : River.values()) {
			assert river != null;
			assertSerialization("River alone", river);
		}
		assertUnwantedChild(encapsulateTileString("<lake><troll /></lake>"),
				IMapNG.class, false);
		assertMissingProperty(encapsulateTileString("<river />"), IMapNG.class,
				"direction", false);
		final Point point = point(0, 0);
		assertSerialization("River in tile",
				encapsulateRivers(point, River.East));
		assertSerialization("Lake in tile",
				encapsulateRivers(point, Lake));
		assertSerialization("Another river in tile",
				encapsulateRivers(point, River.North));
		final Set<River> setOne = EnumSet.noneOf(River.class);
		final Set<River> setTwo = EnumSet.noneOf(River.class);
		assertThat("Empty sets are equal", setTwo, equalTo(setOne));
		setOne.add(River.North);
		setOne.add(River.South);
		setTwo.add(River.South);
		setTwo.add(River.North);
		assertThat("Rivers added in different order to set", setTwo, equalTo(setOne));
		assertThat("Rivers added in different order to fixture",
				new RiverFixture(River.South, River.North),
				equalTo(new RiverFixture(River.North, River.South)));
		final RiverFixture fixOne = new RiverFixture(River.North);
		fixOne.addRiver(River.South);
		final RiverFixture fixTwo = new RiverFixture(River.South);
		fixTwo.addRiver(River.North);
		assertThat("Rivers added separately", fixTwo, equalTo(fixOne));
		final Collection<TileFixture> hSetOne = new HashSet<>();
		hSetOne.add(fixOne);
		final Collection<TileFixture> hSetTwo = new HashSet<>();
		hSetTwo.add(fixTwo);
		assertThat("Check Set.equals()", hSetTwo, equalTo(hSetOne));
		assertThat("Tile equality with rivers",
				encapsulateRivers(point(1, 1), River.North, River.South),
				equalTo(encapsulateRivers(point(1, 1), River.North, River.South)));
		assertThat("Tile equality with different order of rivers",
				encapsulateRivers(point(1, 2), River.South, River.North),
				equalTo(encapsulateRivers(point(1, 2), River.North, River.South)));
		assertSerialization("Two rivers",
				encapsulateRivers(point, River.North, River.South));
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
												final Pair<Point, TileType>... terrain) {
		final IMutableMapNG retval =
				new SPMapNG(new MapDimensions(dims.getRow(), dims.getCol(), 2),
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
	public void testTileSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("Simple Tile",
				createSimpleMap(point(1, 1), Pair.of(point(0, 0), Desert)));
		final IMutableMapNG firstMap =
				createSimpleMap(point(2, 2), Pair.of(point(1, 1), Plains));
		firstMap.addFixture(point(1, 1), new Griffin(1));
		assertSerialization("Tile with one fixture", firstMap);
		final IMutableMapNG secondMap =
				createSimpleMap(point(3, 3), Pair.of(point(2, 2), Steppe));
		secondMap.addFixture(point(2, 2),
				new Unit(new Player(1, ""), "unitOne", "firstUnit", 1));
		secondMap.setForest(point(2, 2), new Forest("forestKind", true));
		assertSerialization("Tile with two fixtures", secondMap);
		final IMutableMapNG thirdMap =
				createSimpleMap(point(4, 4), Pair.of(point(3, 3), Jungle));
		final Fortress fort = new Fortress(new Player(2, ""), "fortOne", 1);
		fort.addMember(new Unit(new Player(2, ""), "unitTwo", "secondUnit", 2));
		thirdMap.addFixture(point(3, 3), fort);
		thirdMap.addFixture(point(3, 3), new TextFixture("Random text here", 5));
		thirdMap.addRivers(point(3, 3), Lake);
		assertSerialization("More complex tile", thirdMap);
		final IMutableMapNG fourthMap =
				createSimpleMap(point(5, 5), Pair.of(point(4, 4), Plains));
		final String oldKindProperty = "type";
		assertDeprecatedDeserialization(
				"Deserialization of deprecated tile-type idiom",
				fourthMap,
				assertNotNull(KIND_PATTERN.matcher(createSerializedForm(fourthMap, true))
									.replaceAll(
											Matcher.quoteReplacement(oldKindProperty))),
				oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated tile-type idiom", fourthMap,
				assertNotNull(KIND_PATTERN.matcher(createSerializedForm(fourthMap,
						false))
									.replaceAll(
											Matcher.quoteReplacement
															(oldKindProperty))),
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
		assertThat("Just checking ...",
				Long.valueOf(five.streamOtherFixtures(point(2, 3)).count()),
				equalTo(Long.valueOf(2L)));
		assertSerialization("Multiple units should come through", five);
		final String xmlTwo = "<view xmlns=\"" + ISPReader.NAMESPACE +
									"\" current_player=\"-1\" " +
									"current_turn=\"-1\">\n\t<map version=\"2\" " +
									"rows=\"3\" columns=\"4\">\n\t\t<row " +
									"index=\"2\">\n\t\t\t<tile row=\"2\" column=\"3\" " +
									"kind=\"jungle\">\n\t\t\t\t<unit owner=\"2\" " +
									"kind=\"explorer\" name=\"name one\" id=\"1\" " +
									"/>\n\t\t\t\t<unit owner=\"2\" kind=\"explorer\" " +
									"name=\"name two\" id=\"2\" " +
									"/>\n\t\t\t</tile>\n\t\t</row>\n\t</map>\n</view>\n";
		assertThat("Multiple units", createSerializedForm(five, true), equalTo(xmlTwo));
		assertThat("Multiple units", createSerializedForm(five, false),
				equalTo(xmlTwo));
		assertThat("Shouldn't print empty not-visible tiles",
				createSerializedForm(
						createSimpleMap(point(1, 1), Pair.of(point(0, 0), NotVisible)),
						true),
				equalTo("<view xmlns=\"" + ISPReader.NAMESPACE +
						"\" current_player=\"-1\" current_turn=\"-1\">\n\t<map " +
						"version=\"2\" rows=\"1\" columns=\"1\">\n\t</map>\n</view>\n"));
		assertThat("Shouldn't print empty not-visible tiles",
				createSerializedForm(
						createSimpleMap(point(1, 1), Pair.of(point(0, 0), NotVisible)),
						false),
				equalTo("<view xmlns=\"" + ISPReader.NAMESPACE +
						"\" current_player=\"-1\" current_turn=\"-1\">\n\t<map " +
						"version=\"2\" rows=\"1\" columns=\"1\">\n\t</map>\n</view>\n"));
		assertImageSerialization("Unit image property is preserved",
				new Unit(new Player(5, ""), "herder", "herderName", 9));
	}

	/**
	 * Test that row nodes are ignored, and that "future" tags are skipped but warned
	 * about.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 */
	@Test
	public void testSkippableSerialization() throws XMLStreamException,
															SPFormatException {
		assertEquivalentForms("Two maps, one with row tags, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><row /></map>",
				IMapNG.class, Warning.Die);
		assertEquivalentForms("Two maps, one with future tag, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				IMapNG.class, Warning.Ignore);
		assertUnsupportedTag(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				IMapNG.class, "future", true);
		assertUnsupportedTag(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><tile row=\"0\" " +
						"column=\"0\" kind=\"steppe\"><futureTag /></tile></map>",
				IMapNG.class, "futureTag", true);
	}

	/**
	 * Test Map serialization ... primarily errors.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMapSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assertUnwantedChild(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><hill /></map>",
				IMapNG.class, false);
		final Player player = new Player(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), -1);
		firstMap.addPlayer(player);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Plains);
		assertSerialization("Simple Map serialization", firstMap);
		assertMissingProperty("<map version=\"2\" columns=\"1\" />",
				IMapNG.class, "rows", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" />", IMapNG.class,
				"columns", false);
		final String originalFormOne = createSerializedForm(firstMap, false);
		final String originalFormTwo = createSerializedForm(firstMap, true);
		firstMap.setBaseTerrain(point(1, 1), TileType.NotVisible);
		assertThat("Explicitly not visible tile is not serialized",
				createSerializedForm(firstMap, false), equalTo(originalFormOne));
		assertThat("Explicitly not visible tile is not serialized",
				createSerializedForm(firstMap, true), equalTo(originalFormTwo));
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
		final IMutablePlayerCollection players = new PlayerCollection();
		players.add(player);
		final IMutableMapNG firstMap = new SPMapNG(new MapDimensions(1, 1, 2), players, 0);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Plains);
		assertSerialization("Simple Map serialization", firstMap);
		assertMissingProperty("<map version=\"2\" columns=\"1\" />",
				SPMapNG.class, "rows", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" />", SPMapNG.class,
				"columns", false);
		firstMap.setMountainous(point, true);
		assertSerialization("Map with a mountainous point", firstMap);
	}

	/**
	 * Test view serialization.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testViewSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		final Player player = new Player(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), 0);
		firstMap.addPlayer(player);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Steppe);
		assertSerialization("SPMapNG serialization", firstMap);
		assertMissingProperty("<view current_turn=\"0\">" +
									"<map version=\"2\" rows=\"1\" columns=\"1\" " +
									"/></view>", IMapNG.class, "current_player", false);
		assertMissingProperty("<view current_player=\"0\">" +
									"<map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
				IMapNG.class, "current_turn", false);
		assertMissingChild("<view current_player=\"1\" current_turn=\"0\" />",
				IMapNG.class);
		assertMissingChild("<view current_player=\"1\" current_turn=\"13\" />",
				IMapNG.class);
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
	 * Test support for XML namespaces.
	 *
	 * @throws SPFormatException  on SP format error
	 * @throws XMLStreamException on XML reading error
	 */
	@Test
	public void testNamespacedSerialization()
			throws XMLStreamException, SPFormatException {
		final Player player = new Player(1, "playerOne");
		player.setCurrent(true);
		final IMutableMapNG firstMap =
				new SPMapNG(new MapDimensions(1, 1, 2), new PlayerCollection(), 0);
		firstMap.addPlayer(player);
		final Point point = point(0, 0);
		firstMap.setBaseTerrain(point, Steppe);
		assertMapDeserialization("Proper deserialization of namespaced map", firstMap,
				"<map xmlns=\"" + ISPReader.NAMESPACE + "\" version=\"2\" rows=\"1\" columns=\"1\" " +
						"current_player=\"1\"><player number=\"1\" " +
						"code_name=\"playerOne\" /><row index=\"0\"><tile row=\"0\" " +
						"column=\"0\" kind=\"steppe\" /></row></map>");
		assertMapDeserialization(
				"Proper deserialization of map if another namespace is declared default",
				firstMap, "<sp:map xmlns=\"xyzzy\" xmlns:sp=\"" + ISPReader.NAMESPACE +
								"\" version=\"2\" rows=\"1\" columns=\"1\" " +
								"current_player=\"1\"><sp:player number=\"1\" " +
								"code_name=\"playerOne\" /><sp:row " +
								"index=\"0\"><sp:tile row=\"0\" column=\"0\" " +
								"kind=\"steppe\" /></sp:row></sp:map>");
		assertMapDeserialization("Non-root other-namespace tags ignored", firstMap,
				"<map xmlns=\"" + ISPReader.NAMESPACE + "\" version=\"2\" rows=\"1\" columns=\"1\" " +
						"current_player=\"1\" xmlns:xy=\"xyzzy\"><player number=\"1\" " +
						"code_name=\"playerOne\" /><xy:xyzzy /><row index=\"0\"><tile row=\"0\" " +
						"column=\"0\" kind=\"steppe\" /></row></map>");
		try {
			assertMapDeserialization("Root tag must be in supported namespace", firstMap,
					"<map xmlns=\"xyzzy\" version=\"2\" rows=\"1\" columns=\"1\" " +
							"current_player=\"1\"><player number=\"1\" " +
							"code_name=\"playerOne\" /><row index=\"0\"><tile row=\"0\"" +
							" column=\"0\" kind=\"steppe\" /></row></map>");
			fail("Map in an unsupported namespace shouldn't be accepted");
		} catch (final UnwantedChildException except) {
			assertThat("'Tag' that had the unwanted child was what we expected",
					except.getTag().getLocalPart(), equalTo("unknown"));
			assertThat("Unwanted child was the one we expected",
					except.getChild(), equalTo(new QName("xyzzy", "map")));
		}
		try {
			assertUnwantedChild(
					"<adventure xmlns=\"xyzzy\" id=\"1\" brief=\"one\" full=\"two\" />",
					AdventureFixture.class, false);
		} catch (final UnsupportedTagException except) {
			assertThat("Unsupported tag is because of unsupported namespace",
					except.getTag().getNamespaceURI(), equalTo("xyzzy"));
		}
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
				"<include file=\"string:&lt;ogre id=&quot;1&quot; /&gt;\" />");
	}
	/**
	 * Tests that duplicate IDs are warned about.
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 */
	@Test
	public void testDuplicateID() throws XMLStreamException, SPFormatException {
		assertDuplicateID(
				"<map version=\"2\" rows=\"1\" columns=\"1\" " +
						"current_player=\"1\"><player number=\"1\" " +
						"code_name=\"playerOne\" /><row index=\"0\"><tile row=\"0\" " +
						"column=\"0\" kind=\"steppe\"><hill id=\"1\" /><ogre id=\"1\" " +
						"/></tile></row></map>");
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
