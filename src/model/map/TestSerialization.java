package model.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import model.map.fixtures.RiverFixture;
import model.map.fixtures.TextFixture;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.towns.Fortress;

import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.converter.ResolutionDecreaseConverter;

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
				new Player(1, "one", FAKE_FILENAME), Player.class);
		assertSerialization("Second Player serialization test, reflection",
				new Player(2, "two", FAKE_FILENAME), Player.class);
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
		setFileOnObject(tile);
		for (final River river : rivers) {
			tile.addRiver(BaseTestFixtureSerialization.setFileOnObject(river));
		}
		return tile;
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
		assertSerialization(
				"Second River serialization test, reflection",
				addRivers(setFileOnObject(new Tile(0, 0, TileType.Plains,
						FAKE_FILENAME)), River.East), Tile.class);
		assertSerialization(
				"Third River serialization test, reflection",
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.Lake), Tile.class);
		assertSerialization(
				"Fourth River serialization test, reflection",
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.North), Tile.class);
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
		final Set<TileFixture> hsetOne = new HashSet<TileFixture>();
		hsetOne.add(fixOne);
		final Set<TileFixture> hsetTwo = new HashSet<TileFixture>();
		hsetTwo.add(fixTwo);
		assertEquals("Check Set.equals()", fixOne, fixTwo);
		final Tile tone = new Tile(0, 0, TileType.Plains, FAKE_FILENAME);
		tone.addFixture(fixOne);
		final Tile ttwo = new Tile(0, 0, TileType.Plains, FAKE_FILENAME);
		ttwo.addFixture(fixTwo);
		assertEquals("Tile.equals(), RiverFixtures constructed separately",
				tone, ttwo);
		assertEquals(
				"Make sure equals() works properly for rivers",
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.North, River.South),
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.North, River.South));
		assertEquals(
				"Make sure equals() works properly if rivers added in different order",
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.North, River.South),
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.South, River.North));
		assertSerialization(
				"Fifth River serialization test, reflection",
				addRivers(new Tile(0, 0, TileType.Plains, FAKE_FILENAME),
						River.North, River.South), Tile.class);
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
				new Tile(0, 0, TileType.Desert, FAKE_FILENAME), Tile.class);
		final Tile two = new Tile(1, 1, TileType.Plains, FAKE_FILENAME);
		two.setFile(FAKE_FILENAME);
		two.addFixture(setFileOnObject(new Griffin(1, FAKE_FILENAME)));
		assertSerialization("Second Tile serialization test, reflection", two,
				Tile.class);
		final Tile three = new Tile(2, 2, TileType.Steppe, FAKE_FILENAME);
		three.setFile(FAKE_FILENAME);
		three.addFixture(setFileOnObject(new Unit(setFileOnObject(new Player(1,
				"", FAKE_FILENAME)), "unitOne", "firstUnit", 1, FAKE_FILENAME)));
		three.addFixture(setFileOnObject(new Forest("forestKind", true,
				FAKE_FILENAME)));
		assertSerialization("Third Tile serialization test, reflection", three,
				Tile.class);
		final Tile four = new Tile(3, 3, TileType.Jungle, FAKE_FILENAME);
		four.setFile(FAKE_FILENAME);
		final Fortress fort = setFileOnObject(new Fortress(new Player(2, "",
				FAKE_FILENAME), "fortOne", 1, FAKE_FILENAME));
		fort.addUnit(setFileOnObject(new Unit(new Player(2, "", FAKE_FILENAME),
				"unitTwo", "secondUnit", 2, FAKE_FILENAME)));
		four.addFixture(fort);
		four.addFixture(setFileOnObject(new TextFixture("Random text here", 5)));
		four.addRiver(River.Lake);
		assertSerialization("Fourth Tile serialization test, reflection", four,
				Tile.class);
		final Tile five = new Tile(4, 4, TileType.Plains, FAKE_FILENAME);
		final String oldKindProperty = "type"; // NOPMD
		assertDeprecatedDeserialization(
				"Test Tile deserialization of deprecated tile-type idiom",
				five,
				createSerializedForm(five, true).replace("kind",
						oldKindProperty), Tile.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Test Tile deserialization of deprecated tile-type idiom",
				five,
				createSerializedForm(five, false).replace("kind",
						oldKindProperty), Tile.class, oldKindProperty);
		assertMissingProperty("<tile column=\"0\" kind=\"plains\" />",
				Tile.class, "row", false);
		assertMissingProperty("<tile row=\"0\" kind=\"plains\" />", Tile.class,
				"column", false);
		assertMissingProperty("<tile row=\"0\" column=\"0\" />", Tile.class,
				KIND_PROPERTY, false);
		assertUnwantedChild("<tile row=\"0\" column=\"0\" kind=\"plains\">"
				+ "<tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>",
				Tile.class, false);
		final Tile six = new Tile(2, 3, TileType.Jungle, FAKE_FILENAME);
		six.addFixture(setFileOnObject(new Unit(
				new Player(2, "", FAKE_FILENAME), "explorer", "name one", 1,
				FAKE_FILENAME)));
		six.addFixture(setFileOnObject(new Unit(
				new Player(2, "", FAKE_FILENAME), "explorer", "name two", 2,
				FAKE_FILENAME)));
		six.setFile(FAKE_FILENAME);
		assertEquals("Just checking ...", 2, iteratorSize(six));
		assertSerialization("Multiple units should come through", six,
				Tile.class);
		final String xmlTwo = new StringBuilder(
				"<tile row=\"2\" column=\"3\" kind=\"jungle\">\n")
				.append("\t<unit owner=\"2\" kind=\"explorer\" name=\"name one\" id=\"1\" />\n")
				.append("\t<unit owner=\"2\" kind=\"explorer\" name=\"name two\" id=\"2\" />\n")
				.append("</tile>\n").toString();
		assertEquals("Multiple units should come through", xmlTwo,
				createSerializedForm(six, true));
		assertEquals("Multiple units should come through", xmlTwo,
				createSerializedForm(six, false));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"",
				createSerializedForm(new Tile(0, 0, TileType.NotVisible,
						FAKE_FILENAME), true));
		assertEquals(
				"Shouldn't print empty not-visible tiles",
				"",
				createSerializedForm(new Tile(0, 0, TileType.NotVisible,
						FAKE_FILENAME), false));
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
		final SPMap one = new SPMap(2, 1, 1, FAKE_FILENAME);
		one.setFile(FAKE_FILENAME);
		one.addPlayer(setFileOnObject(new Player(1, "playerOne", FAKE_FILENAME)));
		one.getPlayers().getPlayer(1).setCurrent(true);
		one.addTile(setFileOnObject(new Tile(0, 0, TileType.Plains,
				FAKE_FILENAME)));
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
		final SPMap mOne = setFileOnObject(new SPMap(2, 1, 1, FAKE_FILENAME));
		mOne.addPlayer(setFileOnObject(new Player(1, "playerOne", FAKE_FILENAME)));
		mOne.addTile(setFileOnObject(new Tile(0, 0, TileType.Steppe,
				FAKE_FILENAME)));
		final MapView one = setFileOnObject(new MapView(mOne, 1, 0,
				FAKE_FILENAME));
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
		assertForwardDeserialization("Reading Ogre via <include>", new Ogre(1,
				FAKE_FILENAME),
				"<include file=\"string:&lt;ogre id=&quot;1&quot; /&gt;\" />",
				Ogre.class);
	}

	/**
	 * Test conversion.
	 */
	@Test
	public void testConversion() {
		final SPMap start = new SPMap(2, 2, 2, FAKE_FILENAME);
		final Tile tileOne = new Tile(0, 0, TileType.Steppe, FAKE_FILENAME);
		final Animal fixture = new Animal("animal", false, true, 1,
				FAKE_FILENAME);
		tileOne.addFixture(fixture);
		start.addTile(tileOne);
		final Tile tileTwo = new Tile(0, 1, TileType.Ocean, FAKE_FILENAME);
		final CacheFixture fixtureTwo = new CacheFixture("gemstones", "small",
				2, FAKE_FILENAME);
		tileTwo.addFixture(fixtureTwo);
		start.addTile(tileTwo);
		final Tile tileThree = new Tile(1, 0, TileType.Plains, FAKE_FILENAME);
		final Unit fixtureThree = new Unit(new Player(0, "A. Player",
				FAKE_FILENAME), "legion", "eagles", 3, FAKE_FILENAME);
		tileThree.addFixture(fixtureThree);
		start.addTile(tileThree);
		final Tile tileFour = new Tile(1, 1, TileType.Jungle, FAKE_FILENAME);
		final Fortress fixtureFour = new Fortress(new Player(1, "B. Player",
				FAKE_FILENAME), "HQ", 4, FAKE_FILENAME);
		tileFour.addFixture(fixtureFour);
		start.addTile(tileFour);
		final ResolutionDecreaseConverter converter = new ResolutionDecreaseConverter();
		final MapView converted = converter.convert(start);
		final Point zeroPoint = PointFactory.point(0, 0);
		assertTrue("Combined tile should contain fixtures from tile one",
				doesIterableContain(converted.getTile(zeroPoint), fixture));
		assertTrue("Combined tile should contain fixtures from tile two",
				doesIterableContain(converted.getTile(zeroPoint), fixtureTwo));
		assertTrue(
				"Combined tile should contain fixtures from tile three",
				doesIterableContain(converted.getTile(zeroPoint), fixtureThree));
		assertTrue("Combined tile should contain fixtures from tile four",
				doesIterableContain(converted.getTile(zeroPoint), fixtureFour));
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestSerialization";
	}
}
