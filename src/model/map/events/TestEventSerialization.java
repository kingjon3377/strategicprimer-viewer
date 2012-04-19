package model.map.events;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.readerng.MapReaderNG;
import controller.map.simplexml.ISPReader;

/**
 * A class to test the serialization of Events.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class TestEventSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";
	/**
	 * Constructor.
	 */
	public TestEventSerialization() {
		super();
		setUp();
	}

	/**
	 * Set-up method.
	 */
	@Before
	public void setUp() {
		reader = new MapReaderNG();
	}

	/**
	 * The XML reader we'll use to test.
	 */
	private ISPReader reader;

	/**
	 * Test serialization of BattlefieldEvents, including error-checking.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testBattefieldSerialization() throws XMLStreamException,
			SPFormatException {
		// ESCA-JAVA0076:
		assertSerialization(
				"First BattlefieldEvent serialization test, reflection",
				reader, new BattlefieldEvent(10), BattlefieldEvent.class);
		// ESCA-JAVA0076:
		assertSerialization(
				"Second BattlefieldEvent serialization test, reflection",
				reader, new BattlefieldEvent(30), BattlefieldEvent.class);
		assertUnwantedChild(reader, "<battlefield dc=\"10\"><troll /></battlefield>",
				BattlefieldEvent.class, false);
		assertMissingProperty(reader, "<battlefield />",
				BattlefieldEvent.class, "dc", false);
	}

	/**
	 * Test serialization of CaveEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testCaveSerialization() throws XMLStreamException,
			SPFormatException {
		assertSerialization("First CaveEvent serialization test, reflection",
				reader, new CaveEvent(10), CaveEvent.class);
		assertSerialization(
				"Second BattlefieldEvent serialization test, reflection",
				reader, new CaveEvent(30), CaveEvent.class);
		assertUnwantedChild(reader, "<cave dc=\"10\"><troll /></cave>", CaveEvent.class,
				false);
		assertMissingProperty(reader, "<cave />", CaveEvent.class, "dc", false);
	}

	/**
	 * Test serialization of CityEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testCitySerialization() throws XMLStreamException,
			SPFormatException {
		for (TownStatus status : TownStatus.values()) {
			for (TownSize size : TownSize.values()) {
				// ESCA-JAVA0076:
				assertSerialization(
						"First CityEvent serialization test, reflection, status "
								+ status + ", size " + size, reader,
						new CityEvent(status, size, 10, "one"), CityEvent.class); // NOPMD
				// ESCA-JAVA0076:
				assertSerialization(
						"Second CityEvent serialization test, reflection, status "
								+ status + ", size " + size, reader,
						new CityEvent(status, size, 40, "two"), CityEvent.class); // NOPMD
			}
		}
		final CityEvent three = new CityEvent(TownStatus.Active, TownSize.Small, 30, "");
		assertSerialization(
				"Serialization of CityEvent without a name, reflection",
				reader, three, CityEvent.class, new Warning(Warning.Action.Ignore));
		assertMissingProperty(reader, three.toXML(), CityEvent.class, "name", true);
	}

	/**
	 * Test serialization of FortificationEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testFortificationSerialization() throws XMLStreamException,
			SPFormatException {
		for (TownStatus status : TownStatus.values()) {
			for (TownSize size : TownSize.values()) {
				assertSerialization(
						"First FortificationEvent serialization test, reflection, status "
								+ status + ", size " + size, reader,
						new FortificationEvent(status, size, 10, "one"), // NOPMD
						FortificationEvent.class);
				assertSerialization(
						"Second FortificationEvent serialization test, reflection, status "
								+ status + " and size " + size, reader,
						new FortificationEvent(status, size, 40, "two"), // NOPMD
						FortificationEvent.class);
			}
		}
		final FortificationEvent three = new FortificationEvent(TownStatus.Active, TownSize.Small, 30, "");
		assertSerialization(
				"Serialization of FortificationEvent without a name, reflection",
				reader, three, FortificationEvent.class, new Warning(
						Warning.Action.Ignore));
		assertMissingProperty(reader, three.toXML(), FortificationEvent.class, "name", true);
	}

	/**
	 * Test serialization of MineralEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testMineralSerialization() throws XMLStreamException,
			SPFormatException {
		assertSerialization(
				"First MineralEvent serialization test, reflection", reader,
				new MineralEvent("one", true, 10), MineralEvent.class);
		final MineralEvent two = new MineralEvent("two", false, 35);
		assertSerialization(
				"Second MineralEvent serialization test, reflection", reader,
				two, MineralEvent.class);
		final String xml = two.toXML().replace("kind", "mineral");
		assertEquals("Deserialization of deprecated Mineral idiom, reflection",
				two, reader.readXML(new StringReader(xml), MineralEvent.class,
						true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialization of deprecated Mineral idiom, non-reflection",
				two, reader.readXML(new StringReader(xml), MineralEvent.class,
						false, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, xml, MineralEvent.class, "mineral",
				true);
		assertUnwantedChild(reader, "<mineral><troll /></mineral>",
				MineralEvent.class, false);
		assertMissingProperty(reader, "<mineral dc=\"0\" exposed=\"false\" />",
				MineralEvent.class, KIND_PROPERTY, false);
		assertMissingProperty(reader,
				"<mineral kind=\"gold\" exposed=\"false\" />",
				MineralEvent.class, "dc", false);
		assertMissingProperty(reader, "<mineral dc=\"0\" kind=\"gold\" />",
				MineralEvent.class, "exposed", false);
	}

	/**
	 * Test serialization of StoneEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testStoneSerialization() throws XMLStreamException,
			SPFormatException {
		for (StoneKind kind : StoneKind.values()) {
			assertSerialization("First StoneEvent test, reflection, kind: "
					+ kind, reader, new StoneEvent(kind, 8), StoneEvent.class); // NOPMD
			assertSerialization("Second StoneEvent test, reflection, kind: "
					+ kind, reader, new StoneEvent(kind, 15), StoneEvent.class); // NOPMD
		}
		final StoneEvent three = new StoneEvent(StoneKind.Marble, 10);
		final String xml = three.toXML().replace("kind", "stone");
		assertEquals("Deserialization of deprecated stone idiom, reflection",
				three, reader.readXML(new StringReader(xml), StoneEvent.class,
						true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialization of deprecated stone idiom, non-reflection",
				three, reader.readXML(new StringReader(xml), StoneEvent.class,
						false, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, xml, StoneEvent.class, "stone", true);
		assertUnwantedChild(reader, 
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>",
				StoneEvent.class, false);
		assertMissingProperty(reader, "<stone kind=\"stone\" />",
				StoneEvent.class, "dc", false);
		assertMissingProperty(reader, "<stone dc=\"10\" />",
				StoneEvent.class, KIND_PROPERTY, false);
	}

	/**
	 * Test serialization of TownEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testTownSerialization() throws XMLStreamException,
			SPFormatException {
		for (TownStatus status : TownStatus.values()) {
			for (TownSize size : TownSize.values()) {
				assertSerialization(
						"First TownEvent serialization test, reflection, status "
								+ status + " and size " + size, reader,
						new TownEvent(status, size, 10, "one"), TownEvent.class); // NOPMD
				assertSerialization(
						"Second TownEvent serialization test, reflection, status "
								+ status + " and size " + size, reader,
						new TownEvent(status, size, 40, "two"), TownEvent.class); // NOPMD
			}
		}
		final TownEvent three = new TownEvent(TownStatus.Active, TownSize.Small, 30, "");
		assertSerialization(
				"Serialization of TownEvent without a name, reflection",
				reader, three, TownEvent.class, new Warning(Warning.Action.Ignore));
		assertMissingProperty(reader, three.toXML(), TownEvent.class, "name", true);
		assertMissingProperty(reader, "<town />", TownEvent.class,
				"dc", false);
		assertMissingProperty(reader, "<town dc=\"0\" status=\"active\" />",
				TownEvent.class, "size", false);
		assertMissingProperty(reader, "<town dc=\"0\" size=\"small\" />",
				TownEvent.class, STATUS_PROPERTY, false);
	}
}
