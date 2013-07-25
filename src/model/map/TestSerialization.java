package model.map;

import static org.junit.Assert.assertEquals;

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

import util.Warning;
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
		assertUnwantedChild("<player code_name=\"one\" number=\"1\"><troll /></player>", Player.class, false);
		assertMissingProperty("<player code_name=\"one\" />", Player.class,
				"number", false);
		assertMissingProperty("<player number=\"1\" />", Player.class,
				"code_name", false);
	}

	/**
	 * A factory to add rivers to a tile in-line.
	 *
	 * @param tile the tile to use
	 * @param rivers the rivers to add
	 * @return the tile, set up.
	 */
	private static Tile addRivers(final Tile tile, final River... rivers) {
		for (final River river : rivers) {
			tile.addRiver(river);
		}
		return tile;
	}
	/**
	 * @param tile a tile
	 * @param point its location
	 * @return a map containing the tile
	 */
	private static SPMap encapsulateTile(final Point point, final Tile tile) {
		final SPMap retval = new SPMap(new MapDimensions(point.row + 1, point.col + 1, 2));
		retval.addTile(point, tile);
		return retval;
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
			assertSerialization("First River serialization test, reflection",
					river, River.class);
		}
		assertUnwantedChild(
				"<tile row=\"1\" column=\"1\" kind=\"plains\"><lake><troll /></lake></tile>",
				Tile.class, false);
		assertMissingProperty(
				"<tile row=\"1\" column=\"1\" kind=\"plains\"><river /></tile>",
				Tile.class, "direction", false);
		final Point point = PointFactory.point(0, 0);
		assertSerialization(
				"Second River serialization test, reflection",
				encapsulateTile(
						point,
						addRivers(new Tile(TileType.Plains),
								River.East)), SPMap.class);
		assertSerialization(
				"Third River serialization test, reflection",
				encapsulateTile(point, addRivers(new Tile(TileType.Plains),
						River.Lake)), SPMap.class);
		assertSerialization(
				"Fourth River serialization test, reflection",
				encapsulateTile(point, addRivers(new Tile(TileType.Plains),
						River.North)), SPMap.class);
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
		final Tile tone = new Tile(TileType.Plains);
		tone.addFixture(fixOne);
		final Tile ttwo = new Tile(TileType.Plains);
		ttwo.addFixture(fixTwo);
		assertEquals("Tile.equals(), RiverFixtures constructed separately",
				tone, ttwo);
		assertEquals(
				"Make sure equals() works properly for rivers",
				addRivers(new Tile(TileType.Plains),
						River.North, River.South),
				addRivers(new Tile(TileType.Plains),
						River.North, River.South));
		assertEquals(
				"Make sure equals() works properly if rivers added in different order",
				addRivers(new Tile(TileType.Plains),
						River.North, River.South),
				addRivers(new Tile(TileType.Plains),
						River.South, River.North));
		assertSerialization(
				"Fifth River serialization test, reflection",
				encapsulateTile(point, addRivers(new Tile(TileType.Plains),
						River.North, River.South)), SPMap.class);
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
		assertSerialization("First Tile serialization test, reflection",
				encapsulateTile(PointFactory.point(0, 0), new Tile(TileType.Desert)), SPMap.class);
		final Tile two = new Tile(TileType.Plains);
		two.addFixture(new Griffin(1));
		assertSerialization("Second Tile serialization test, reflection", encapsulateTile(PointFactory.point(1, 1), two),
				SPMap.class);
		final Tile three = new Tile(TileType.Steppe);
		three.addFixture(new Unit(new Player(1, ""), "unitOne", "firstUnit", 1));
		three.addFixture(new Forest("forestKind", true));
		assertSerialization("Third Tile serialization test, reflection", encapsulateTile(PointFactory.point(2, 2), three),
				SPMap.class);
		final Tile four = new Tile(TileType.Jungle);
		final Fortress fort = new Fortress(new Player(2, ""), "fortOne", 1);
		fort.addUnit(new Unit(new Player(2, ""),
				"unitTwo", "secondUnit", 2));
		four.addFixture(fort);
		four.addFixture(new TextFixture("Random text here", 5));
		four.addRiver(River.Lake);
		assertSerialization("Fourth Tile serialization test, reflection", encapsulateTile(PointFactory.point(3, 3), four),
				SPMap.class);
		final SPMap five = encapsulateTile(PointFactory.point(4, 4), new Tile(TileType.Plains));
		final String oldKindProperty = "type"; // NOPMD
		assertDeprecatedDeserialization(
				"Test Tile deserialization of deprecated tile-type idiom",
				five,
				createSerializedForm(five, true).replace("kind",
						oldKindProperty), SPMap.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Test Tile deserialization of deprecated tile-type idiom",
				five,
				createSerializedForm(five, false).replace("kind",
						oldKindProperty), SPMap.class, oldKindProperty);
		assertMissingProperty("<tile column=\"0\" kind=\"plains\" />",
				Tile.class, "row", false);
		assertMissingProperty("<tile row=\"0\" kind=\"plains\" />", Tile.class,
				"column", false);
		assertMissingProperty("<tile row=\"0\" column=\"0\" />", Tile.class,
				KIND_PROPERTY, false);
		assertUnwantedChild("<tile row=\"0\" column=\"0\" kind=\"plains\">"
				+ "<tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>",
				Tile.class, false);
		final Tile six = new Tile(TileType.Jungle);
		six.addFixture(new Unit(new Player(2, ""), "explorer", "name one", 1));
		six.addFixture(new Unit(new Player(2, ""), "explorer", "name two", 2));
		assertEquals("Just checking ...", 2, iteratorSize(six));
		assertSerialization("Multiple units should come through", encapsulateTile(PointFactory.point(2, 3), six),
				SPMap.class);
		final String xmlTwo = new StringBuilder("<map version=\"2\" rows=\"3\" columns=\"4\">\n").append("\t<row index=\"2\">\n")
				.append("\t\t<tile row=\"2\" column=\"3\" kind=\"jungle\">\n")
				.append("\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name one\" id=\"1\" />\n")
				.append("\t\t\t<unit owner=\"2\" kind=\"explorer\" name=\"name two\" id=\"2\" />\n")
				.append("\t\t</tile>\n").append("\t</row>\n</map>\n").toString();
		assertEquals("Multiple units should come through", xmlTwo,
				createSerializedForm(encapsulateTile(PointFactory.point(2, 3), six), true));
		assertEquals("Multiple units should come through", xmlTwo,
				createSerializedForm(encapsulateTile(PointFactory.point(2, 3), six), false));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"<map version=\"2\" rows=\"1\" columns=\"1\" />\n",
				createSerializedForm(encapsulateTile(PointFactory.point(0, 0), new Tile(TileType.NotVisible)), true));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"<map version=\"2\" rows=\"1\" columns=\"1\">\n</map>\n",
				createSerializedForm(encapsulateTile(PointFactory.point(0, 0), new Tile(TileType.NotVisible)), false));
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
				SPMap.class, Warning.Action.Die);
		assertEquivalentForms("Two maps, one with future tag, one without",
				"<map rows=\"1\" columns=\"1\" version=\"2\" />",
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				SPMap.class, Warning.Action.Ignore);
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
		final Point point = PointFactory.point(0, 0);
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
		final Point point = PointFactory.point(0, 0);
		mOne.addTile(point, new Tile(TileType.Steppe));
		final MapView one = new MapView(mOne, 1, 0);
		assertSerialization("MapView serialization", one, MapView.class);
		assertMissingProperty(
				"<view current_turn=\"0\"><map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
				MapView.class, "current_player", false);
		assertMissingProperty(
				"<view current_player=\"0\"><map version=\"2\" rows=\"1\" columns=\"1\" /></view>",
				MapView.class, "current_turn", false);
		assertMissingChild("<view current_player=\"1\" current_turn=\"0\" />",
				MapView.class, false);
		assertUnwantedChild(
				new StringBuilder(
						"<view current_player=\"0\" current_turn=\"0\">")
						.append("<map version=\"2\" rows=\"1\" columns=\"1\" />")
						.append("<map version=\"2\" rows=\"1\" columns=\"1\" />")
						.append("</view>").toString(), MapView.class, false);
		assertUnwantedChild(
				"<view current_player=\"0\" current_turn=\"0\"><hill /></view>",
				MapView.class, false);
		assertMapDeserialization(
				"Proper deserialization of map into view",
				one,
				new StringBuilder(
						"<map version=\"2\" rows=\"1\" columns=\"1\" current_player=\"1\">")
						.append("<player number=\"1\" code_name=\"playerOne\" />")
						.append("<row index=\"0\">")
						.append("<tile row=\"0\" column=\"0\" kind=\"steppe\"></tile>")
						.append("</row>").append("</map>").toString());
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
