package model.map;

import static org.junit.Assert.assertEquals;

import javax.xml.stream.XMLStreamException;

import model.map.fixtures.Forest;
import model.map.fixtures.Fortress;
import model.map.fixtures.Griffin;
import model.map.fixtures.TextFixture;
import model.map.fixtures.Unit;

import org.junit.Before;
import org.junit.Test;

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
		final Player one = new Player(1, "one");
		assertEquals("First Player serialization test, reflection", one,
				helpSerialization(reader, one, Player.class, true));
		assertEquals("First Player serialization test, non-reflection", one,
				helpSerialization(reader, one, Player.class, false));
		final Player two = new Player(2, "two");
		assertEquals("Second Player serialization test, reflection", two,
				helpSerialization(reader, two, Player.class, true));
		assertEquals("Second Player serialization test, non-reflection", two,
				helpSerialization(reader, two, Player.class, false));
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
			assertEquals("First River serialization test, reflection", river,
					helpSerialization(reader, river, River.class, true));
			assertEquals("First River serialization test, non-reflection",
					river, helpSerialization(reader, river, River.class, false));
		}
	}

	/**
	 * Further test River serialization.
	 * 
	 * @throws SPFormatException
	 *             on SP format error
	 * @throws XMLStreamException
	 *             on XML reading error
	 */
	@Test
	public void testRiverSerializationTwo() throws XMLStreamException,
			SPFormatException {
		final Tile tile = new Tile(0, 0, TileType.Plains);
		tile.addRiver(River.East);
		assertEquals("Second River serialization test, reflection", tile,
				helpSerialization(reader, tile, Tile.class, true));
		assertEquals("Second River serialization test, non-reflection", tile,
				helpSerialization(reader, tile, Tile.class, false));
		tile.removeRiver(River.East);
		tile.addRiver(River.Lake);
		assertEquals("Third River serialization test, reflection", tile,
				helpSerialization(reader, tile, Tile.class, true));
		assertEquals("Third River serialization test, non-reflection", tile,
				helpSerialization(reader, tile, Tile.class, false));
		tile.removeRiver(River.Lake);
		tile.addRiver(River.North);
		assertEquals("Fourth River serialization test, reflection", tile,
				helpSerialization(reader, tile, Tile.class, true));
		assertEquals("Fourth River serialization test, non-reflection", tile,
				helpSerialization(reader, tile, Tile.class, false));
		tile.addRiver(River.South);
		assertEquals("Fifth River serialization test, reflection", tile,
				helpSerialization(reader, tile, Tile.class, true));
		assertEquals("Fifth River serialization test, non-reflection", tile,
				helpSerialization(reader, tile, Tile.class, false));
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
		final Tile one = new Tile(0, 0, TileType.Desert);
		assertEquals("First Tile serialization test, reflection", one,
				helpSerialization(reader, one, Tile.class, true));
		assertEquals("First Tile serialization test, non-reflection", one,
				helpSerialization(reader, one, Tile.class, false));
		final Tile two = new Tile(1, 1, TileType.Plains);
		two.addFixture(new Griffin());
		assertEquals("Second Tile serialization test, reflection", two,
				helpSerialization(reader, two, Tile.class, true));
		assertEquals("Second Tile serialization test, non-reflection", two,
				helpSerialization(reader, two, Tile.class, false));
		final Tile three = new Tile(2, 2, TileType.Steppe);
		three.addFixture(new Unit(new Player(1, ""), "unitOne", "firstUnit"));
		three.addFixture(new Forest("forestKind", true));
		assertEquals("Third Tile serialization test, reflection", three,
				helpSerialization(reader, three, Tile.class, true));
		assertEquals("Third Tile serialization test, non-reflection", three,
				helpSerialization(reader, three, Tile.class, false));
		final Tile four = new Tile(3, 3, TileType.Jungle);
		final Fortress fort = new Fortress(new Player(2, ""), "fortOne");
		fort.addUnit(new Unit(new Player(2, ""), "unitTwo", "secondUnit"));
		four.addFixture(fort);
		four.addFixture(new TextFixture("Random text here", 5));
		four.addRiver(River.Lake);
		assertEquals("Fourth Tile serialization test, reflection", four,
				helpSerialization(reader, four, Tile.class, true));
		assertEquals("Fourth Tile serialization test, non-reflection", four,
				helpSerialization(reader, four, Tile.class, false));
	}
}
