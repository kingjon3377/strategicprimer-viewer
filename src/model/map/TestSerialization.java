package model.map;

import static model.map.PointFactory.point;
import static model.map.River.Lake;
import static model.map.TileType.Desert;
import static model.map.TileType.Jungle;
import static model.map.TileType.NotVisible;
import static model.map.TileType.Plains;
import static model.map.TileType.Steppe;
import static org.junit.Assert.assertEquals;
import static util.NullCleaner.assertNotNull;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;

import org.junit.Test;

import util.Warning.Action;
import controller.map.formatexceptions.SPFormatException;

/**
 * A class to test the serialization of XMLWritable objects other than Fixtures.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TestSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Test Player serialization.
	 *
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException on I/O error creating serialized form
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
	 * A factory to encapsulate rivers in a tile.
	 * @param rivers the rivers to put on a tile
	 * @return a tile containing them. Declared mutable for the sake of calling code.
	 */
	private static IMutableTile encapsulateRivers(final River... rivers) {
		final IMutableTile tile = new Tile(TileType.Plains);
		for (final River river : rivers) {
			if (river != null) {
				tile.addRiver(river);
			}
		}
		return tile;
	}

	/**
	 * @param tile a tile. Since a mutable tile collection can't contain
	 *        immutable tiles, it must bee mutable.
	 * @param point its location
	 * @return a map containing the tile
	 */
	private static SPMap encapsulateTile(final Point point,
			final IMutableTile tile) {
		final SPMap retval = new SPMap(new MapDimensions(point.row + 1,
				point.col + 1, 2));
		retval.addTile(point, tile);
		return retval;
	}
	/**
	 * Encapsulate the given string in a 'tile' tag.
	 * @param str a string
	 * @return it, encapsulated.
	 */
	private static String encapsulateTileString(final String str) {
		final StringBuilder builder = new StringBuilder(55 + str.length());
		builder.append("<tile row=\"1\" column=\"1\" kind=\"plains\">");
		builder.append(str);
		builder.append("</tile>");
		return assertNotNull(builder.toString());
	}
	/**
	 * Test River serialization.
	 *
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testRiverSerializationOne() throws XMLStreamException,
			SPFormatException, IOException {
		for (final River river : River.values()) {
			assert river != null;
			assertSerialization("River alone", river, River.class);
		}
		assertUnwantedChild(encapsulateTileString("<lake><troll /></lake>"),
				Tile.class, false);
		assertMissingProperty(encapsulateTileString("<river />"), Tile.class,
				"direction", false);
		final Point point = point(0, 0);
		assertSerialization("River in tile",
				encapsulateTile(point, encapsulateRivers(River.East)),
				SPMap.class);
		assertSerialization("Lake in tile",
				encapsulateTile(point, encapsulateRivers(River.Lake)),
				SPMap.class);
		assertSerialization("Another river in tile",
				encapsulateTile(point, encapsulateRivers(River.North)),
				SPMap.class);
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
						River.South, River.North));
		final RiverFixture fixOne = new RiverFixture(River.North);
		final RiverFixture fixTwo = new RiverFixture(River.South);
		fixOne.addRiver(River.South);
		fixTwo.addRiver(River.North);
		assertEquals("Rivers added separately", fixOne, fixTwo);
		final Set<TileFixture> hsetOne = new HashSet<>();
		hsetOne.add(fixOne);
		final Set<TileFixture> hsetTwo = new HashSet<>();
		hsetTwo.add(fixTwo);
		assertEquals("Check Set.equals()", fixOne, fixTwo);
		final IMutableTile tone = new Tile(TileType.Plains);
		tone.addFixture(fixOne);
		final IMutableTile ttwo = new Tile(TileType.Plains);
		ttwo.addFixture(fixTwo);
		assertEquals("Tile.equals(), RiverFixtures built separately", tone,
				ttwo);
		assertEquals("Tile equality with rivers",
				encapsulateRivers(River.North, River.South),
				encapsulateRivers(River.North, River.South));
		assertEquals("Tile equality with different order of rivers",
				encapsulateRivers(River.North, River.South),
				encapsulateRivers(River.South, River.North));
		assertSerialization(
				"Two rivers",
				encapsulateTile(point,
						encapsulateRivers(River.North, River.South)),
				SPMap.class);
	}
	
	/**
	 * @param type
	 *            a tile type
	 * @param fixtures
	 *            fixtures
	 * @return a tile of that type containing them. Declared mutable for the
	 *         sake of calling code.
	 */
	private static IMutableTile encapsulateFixtures(final TileType type,
			final TileFixture... fixtures) {
		final IMutableTile tile = new Tile(type);
		for (final TileFixture fix : fixtures) {
			if (fix != null) {
				tile.addFixture(fix);
			}
		}
		return tile;
	}
	/**
	 * Test Tile serialization.
	 *
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testTileSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("Simple Tile",
				encapsulateTile(point(0, 0), new Tile(Desert)), SPMap.class);
		assertSerialization(
				"Tile with one fixture",
				encapsulateTile(point(1, 1),
						encapsulateFixtures(Plains, new Griffin(1))),
				SPMap.class);
		assertSerialization(
				"Tile with two fixtures",
				encapsulateTile(
						point(2, 2),
						encapsulateFixtures(Steppe, new Unit(new Player(1, ""),
								"unitOne", "firstUnit", 1), new Forest(
								"forestKind", true))), SPMap.class);
		final IMutableTile four = new Tile(Jungle);
		final Fortress fort = new Fortress(new Player(2, ""), "fortOne", 1);
		fort.addUnit(new Unit(new Player(2, ""), "unitTwo", "secondUnit", 2));
		four.addFixture(fort);
		four.addFixture(new TextFixture("Random text here", 5));
		four.addRiver(Lake);
		assertSerialization("More complex tile",
				encapsulateTile(point(3, 3), four), SPMap.class);
		final SPMap five = encapsulateTile(point(4, 4), new Tile(Plains));
		final String oldKindProperty = "type"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of deprecated tile-type idiom", five,
				assertNotNull(createSerializedForm(five, true)
						.replace("kind", oldKindProperty)), SPMap.class,
				oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated tile-type idiom", five,
				assertNotNull(createSerializedForm(five, false)
						.replace("kind", oldKindProperty)), SPMap.class,
				oldKindProperty);
		assertMissingProperty("<tile column=\"0\" kind=\"plains\" />",
				Tile.class, "row", false);
		assertMissingProperty("<tile row=\"0\" kind=\"plains\" />", Tile.class,
				"column", false);
		assertMissingProperty("<tile row=\"0\" column=\"0\" />", Tile.class,
				KIND_PROPERTY, false);
		assertUnwantedChild(
				encapsulateTileString("<tile row=\"2\" column=\"0\" "
						+ "kind=\"plains\" />"), Tile.class, false);
		final IMutableTile six = encapsulateFixtures(TileType.Jungle, new Unit(
				new Player(2, ""), "explorer", "name one", 1), new Unit(
				new Player(2, ""), "explorer", "name two", 2));
		assertEquals("Just checking ...", 2, iteratorSize(six));
		assertSerialization("Multiple units should come through",
				encapsulateTile(point(2, 3), six), SPMap.class);
		final String xmlTwo = new StringBuilder(280)
				.append("<map version=\"2\" rows=\"3\" columns=\"4\">\n")
				.append("\t<row index=\"2\">\n")
				.append("\t\t<tile row=\"2\" column=\"3\" kind=\"jungle\">\n")
				.append("\t\t\t<unit owner=\"2\" kind=\"explorer\" ")
				.append("name=\"name one\" id=\"1\" />\n")
				.append("\t\t\t<unit owner=\"2\" kind=\"explorer\" ")
				.append("name=\"name two\" id=\"2\" />\n")
				.append("\t\t</tile>\n").append("\t</row>\n</map>\n")
				.toString();
		assertEquals("Multiple units", xmlTwo,
				createSerializedForm(encapsulateTile(point(2, 3), six), true));
		assertEquals("Multiple units", xmlTwo,
				createSerializedForm(encapsulateTile(point(2, 3), six), false));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"<map version=\"2\" rows=\"1\" columns=\"1\" />\n",
				createSerializedForm(
						encapsulateTile(point(0, 0), new Tile(NotVisible)),
						true));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"<map version=\"2\" rows=\"1\" columns=\"1\">\n</map>\n",
				createSerializedForm(
						encapsulateTile(point(0, 0), new Tile(NotVisible)),
						false));
		assertImageSerialization("Unit image property is preserved", new Unit(
				new Player(5, ""), "herder", "herderName", 9), Unit.class);
	}

	/**
	 * Test that row nodes are ignored, and that "future" tags are skipped but
	 * warned about.
	 *
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 */
	@Test
	public void testSkppableSerialization() throws XMLStreamException,
			SPFormatException {
		assertEquivalentForms("Two maps, one with row tags, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><row /></map>",
				SPMap.class, Action.Die);
		assertEquivalentForms("Two maps, one with future tag, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				SPMap.class, Action.Ignore);
		assertUnsupportedTag(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				SPMap.class, "future", true);
	}

	/**
	 * Test Map serialization ... primarily errors.
	 *
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testMapSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertUnwantedChild(
				"<map rows=\"1\" columns=\"1\" version=\"2\"><hill /></map>",
				SPMap.class, false);
		final SPMap one = new SPMap(new MapDimensions(1, 1, 2));
		one.addPlayer(new Player(1, "playerOne"));
		one.getPlayers().getPlayer(1).setCurrent(true);
		final Point point = point(0, 0);
		one.addTile(point, new Tile(TileType.Plains));
		assertSerialization("Simple Map serialization", one, SPMap.class);
		assertMissingProperty("<map version=\"2\" columns=\"1\" />",
				SPMap.class, "rows", false);
		assertMissingProperty("<map version=\"2\" rows=\"1\" />", SPMap.class,
				"columns", false);
	}

	/**
	 * Test view serialization.
	 *
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML reading error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testViewSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		final SPMap mOne = new SPMap(new MapDimensions(1, 1, 2));
		mOne.addPlayer(new Player(1, "playerOne"));
		final Point point = point(0, 0);
		mOne.addTile(point, new Tile(TileType.Steppe));
		final MapView one = new MapView(mOne, 1, 0);
		assertSerialization("MapView serialization", one, MapView.class);
		assertMissingProperty("<view current_turn=\"0\">"
				+ "<map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
				MapView.class, "current_player", false);
		assertMissingProperty("<view current_player=\"0\">"
				+ "<map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
				MapView.class, "current_turn", false);
		assertMissingChild("<view current_player=\"1\" current_turn=\"0\" />",
				MapView.class, false);
		assertUnwantedChild(assertNotNull(new StringBuilder(150)
				.append("<view current_player=\"0\" current_turn=\"0\">")
				.append("<map version=\"2\" rows=\"1\" columns=\"1\" />")
				.append("<map version=\"2\" rows=\"1\" columns=\"1\" />")
				.append("</view>").toString()), MapView.class, false);
		assertUnwantedChild(
				"<view current_player=\"0\" current_turn=\"0\"><hill /></view>",
				MapView.class, false);
		assertMapDeserialization(
				"Proper deserialization of map into view",
				one,
				assertNotNull(new StringBuilder(200)
						.append("<map version=\"2\" rows=\"1\" ")
						.append("columns=\"1\" current_player=\"1\">")
						.append("<player number=\"1\" code_name=\"playerOne\" />")
						.append("<row index=\"0\">")
						.append("<tile row=\"0\" column=\"0\" kind=\"steppe\" />")
						.append("</row>").append("</map>").toString()));
	}

	/**
	 * Test the <include> tag.
	 *
	 * @throws SPFormatException on SP format error
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
	@Override
	public String toString() {
		return "TestSerialization";
	}
}
