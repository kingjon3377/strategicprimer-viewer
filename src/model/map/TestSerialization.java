package model.map;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.fixtures.Forest;
import model.map.fixtures.Fortress;
import model.map.fixtures.Griffin;
import model.map.fixtures.TextFixture;
import model.map.fixtures.Unit;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

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
	 * XML reader we'll be using.
	 */
	private SimpleXMLReader reader;

	/**
	 * Set-up method.
	 */
	@Before
	public void setUp() {
		reader = new SimpleXMLReader();
	}

	/**
	 * Constructor.
	 */
	public TestSerialization() {
		super();
		setUp();
	}

	/**
	 * Test Player serialization.
	 * 
	 * @throws SPFormatException
	 *             on SP format error
	 * @throws XMLStreamException
	 *             on XML reading error
	 */
	@Test
	public void testPlayerSerialization() throws XMLStreamException,
			SPFormatException {
		assertSerialization("First Player serialization test, reflection",
				reader, new Player(1, "one"), Player.class);
		assertSerialization("Second Player serialization test, reflection",
				reader, new Player(2, "two"), Player.class);
		assertUnwantedChild(reader, "<player><troll /></player>",
				Player.class, false);
		assertMissingProperty(reader, "<player code_name=\"one\" />",
				Player.class, "number", false);
		assertMissingProperty(reader, "<player number=\"1\" />",
				Player.class, "code_name", false);
	}

	/**
	 * Test River serialization.
	 * 
	 * @throws SPFormatException
	 *             on SP format error
	 * @throws XMLStreamException
	 *             on XML reading error
	 */
	@Test
	public void testRiverSerializationOne() throws XMLStreamException,
			SPFormatException {
		for (River river : River.values()) {
			assertSerialization("First River serialization test, reflection",
					reader, river, River.class);
		}
		final Tile tile = new Tile(0, 0, TileType.Plains);
		tile.addRiver(River.East);
		assertSerialization("Second River serialization test, reflection", reader, tile, Tile.class);
		tile.removeRiver(River.East);
		tile.addRiver(River.Lake);
		assertSerialization("Third River serialization test, reflection", reader, tile, Tile.class);
		tile.removeRiver(River.Lake);
		tile.addRiver(River.North);
		assertSerialization("Fourth River serialization test, reflection", reader, tile, Tile.class);
		tile.addRiver(River.South);
		assertSerialization("Fifth River serialization test, reflection", reader, tile, Tile.class);
		assertUnwantedChild(reader, "<tile row=\"1\" column=\"1\" kind=\"plains\"><lake><troll /></lake></tile>",
				Tile.class, false);
		assertMissingProperty(reader, "<tile row=\"1\" column=\"\" kind=\"plains\"><river /></tile>",
				Tile.class, "direction", false);
	}
	/**
	 * Test Tile serialization.
	 * 
	 * @throws SPFormatException
	 *             on SP format error
	 * @throws XMLStreamException
	 *             on XML reading error
	 */
	@Test
	public void testTileSerialization() throws XMLStreamException,
			SPFormatException {
		assertSerialization("First Tile serialization test, reflection",
				reader, new Tile(0, 0, TileType.Desert), Tile.class);
		final Tile two = new Tile(1, 1, TileType.Plains);
		two.addFixture(new Griffin());
		assertSerialization("Second Tile serialization test, reflection",
				reader, two, Tile.class);
		final Tile three = new Tile(2, 2, TileType.Steppe);
		three.addFixture(new Unit(new Player(1, ""), "unitOne", "firstUnit"));
		three.addFixture(new Forest("forestKind", true));
		assertSerialization("Third Tile serialization test, reflection", reader, three, Tile.class);
		final Tile four = new Tile(3, 3, TileType.Jungle);
		final Fortress fort = new Fortress(new Player(2, ""), "fortOne");
		fort.addUnit(new Unit(new Player(2, ""), "unitTwo", "secondUnit"));
		four.addFixture(fort);
		four.addFixture(new TextFixture("Random text here", 5));
		four.addRiver(River.Lake);
		assertSerialization("Fourth Tile serialization test, reflection", reader, four, Tile.class);
		final Tile five = new Tile(4, 4, TileType.Plains);
		final String xml = five.toXML().replace("kind", "type");
		assertEquals("Test Tile deserialization of deprecated tile-type idiom, non-reflection", five,
				reader.readXML(new StringReader(xml), Tile.class, false, new Warning(Warning.Action.Ignore)));
		assertEquals("Test Tile deserialization of deprecated tile-type idiom, reflection", five,
				reader.readXML(new StringReader(xml), Tile.class, true, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, xml, Tile.class, "type", true);
		assertMissingProperty(reader, "<tile column=\"0\" kind=\"plains\" />",
				Tile.class, "row", false);
		assertMissingProperty(reader, "<tile row=\"0\" kind=\"plains\" />",
				Tile.class, "column", false);
		assertMissingProperty(reader, "<tile row=\"0\" column=\"0\" />",
				Tile.class, KIND_PROPERTY, false);
		assertUnwantedChild(
				reader,
				"<tile row=\"0\" column=\"0\" kind=\"plains\"><tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>",
				Tile.class, false);
	}
	/**
	 * Test that row nodes are ignored, and that "future" tags are skipped but warned about.
	 * @throws SPFormatException
	 *             on SP format error
	 * @throws XMLStreamException
	 *             on XML reading error
	 */
	@Test
	public void testSkppableSerialization() throws XMLStreamException,
			SPFormatException {
		assertEquals(
				"Two maps, one with row tags, one without, non-reflection",
				reader.readXML(new StringReader(
						"<map rows=\"1\" columns=\"1\" version=\"2\" />"),
						SPMap.class, false, Warning.INSTANCE),
				reader.readXML(
						new StringReader(
								"<map rows=\"1\" columns=\"1\" version=\"2\"><row /></map>"),
						SPMap.class, false, Warning.INSTANCE));
		assertEquals(
				"Two maps, one with row tags, one without, reflection",
				reader.readXML(new StringReader(
						"<map rows=\"1\" columns=\"1\" version=\"2\" />"),
						SPMap.class, true, Warning.INSTANCE),
				reader.readXML(
						new StringReader(
								"<map rows=\"1\" columns=\"1\" version=\"2\"><row /></map>"),
						SPMap.class, true, Warning.INSTANCE));
		assertEquals(
				"Two maps, one with future tag, one without, non-reflection",
				reader.readXML(new StringReader(
						"<map rows=\"1\" columns=\"1\" version=\"2\" />"),
						SPMap.class, false, new Warning(Warning.Action.Ignore)),
				reader.readXML(
						new StringReader(
								"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>"),
						SPMap.class, false, new Warning(Warning.Action.Ignore)));
		assertEquals(
				"Two maps, one with future tag, one without, reflection",
				reader.readXML(new StringReader(
						"<map rows=\"1\" columns=\"2\" version=\"2\" />"),
						SPMap.class, true, new Warning(Warning.Action.Ignore)),
				reader.readXML(
						new StringReader(
								"<map rows=\"1\" columns=\"2\" version=\"2\"><future /></map>"),
						SPMap.class, true, new Warning(Warning.Action.Ignore)));
		assertUnsupportedTag(reader,
				"<map rows=\"1\" columns=\"1\" version=\"2\"><future /></map>",
				SPMap.class, "future", true);
	}
}
