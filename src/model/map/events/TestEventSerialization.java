package model.map.events;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.ISPReader;
import controller.map.SPFormatException;

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
		reader = createReader();
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
				new BattlefieldEvent(10, 0), BattlefieldEvent.class);
		// ESCA-JAVA0076:
		assertSerialization(
				"Second BattlefieldEvent serialization test, reflection",
				new BattlefieldEvent(30, 1), BattlefieldEvent.class);
		assertUnwantedChild("<battlefield dc=\"10\"><troll /></battlefield>",
				BattlefieldEvent.class, false);
		assertMissingProperty("<battlefield />",
				BattlefieldEvent.class, "dc", false);
		assertMissingProperty("<battlefield dc=\"10\" />",
				BattlefieldEvent.class, "id", true);
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
				new CaveEvent(10, 0), CaveEvent.class);
		assertSerialization(
				"Second BattlefieldEvent serialization test, reflection",
				new CaveEvent(30, 1), CaveEvent.class);
		assertUnwantedChild("<cave dc=\"10\"><troll /></cave>", CaveEvent.class,
				false);
		assertMissingProperty("<cave />", CaveEvent.class, "dc", false);
		assertMissingProperty("<cave dc=\"10\" />", CaveEvent.class, "id", true);
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
								+ status + ", size " + size,
						new CityEvent(status, size, 10, "one", 0), CityEvent.class); // NOPMD
				// ESCA-JAVA0076:
				assertSerialization(
						"Second CityEvent serialization test, reflection, status "
								+ status + ", size " + size,
						new CityEvent(status, size, 40, "two", 1), CityEvent.class); // NOPMD
			}
		}
		final CityEvent three = new CityEvent(TownStatus.Active, TownSize.Small, 30, "", 3);
		assertSerialization(
				"Serialization of CityEvent without a name, reflection",
				three, CityEvent.class, new Warning(Warning.Action.Ignore));
		assertMissingProperty(three.toXML(), CityEvent.class, "name", true);
		assertMissingProperty(
				"<city status=\"active\" size=\"small\" name=\"name\" dc=\"0\" />",
				CityEvent.class, "id", true);
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
								+ status + ", size " + size,
						new FortificationEvent(status, size, 10, "one", 1), // NOPMD
						FortificationEvent.class);
				assertSerialization(
						"Second FortificationEvent serialization test, reflection, status "
								+ status + " and size " + size,
						new FortificationEvent(status, size, 40, "two", 2), // NOPMD
						FortificationEvent.class);
			}
		}
		final FortificationEvent three = new FortificationEvent(
				TownStatus.Active, TownSize.Small, 30, "", 3);
		assertSerialization(
				"Serialization of FortificationEvent without a name, reflection",
				three, FortificationEvent.class, new Warning(
						Warning.Action.Ignore));
		assertMissingProperty(three.toXML(), FortificationEvent.class, "name", true);
		assertMissingProperty(
				"<fortification status=\"active\" size=\"small\" name=\"name\" dc=\"0\" />",
				FortificationEvent.class, "id", true);
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
				"First MineralEvent serialization test, reflection",
				new MineralEvent("one", true, 10, 1), MineralEvent.class);
		final MineralEvent two = new MineralEvent("two", false, 35, 2);
		assertSerialization(
				"Second MineralEvent serialization test, reflection",
				two, MineralEvent.class);
		final String xml = two.toXML().replace("kind", "mineral");
		assertEquals("Deserialization of deprecated Mineral idiom, reflection",
				two, reader.readXML(new StringReader(xml), MineralEvent.class,
						true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialization of deprecated Mineral idiom, non-reflection",
				two, reader.readXML(new StringReader(xml), MineralEvent.class,
						false, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(xml, MineralEvent.class, "mineral",
				true);
		assertUnwantedChild("<mineral kind=\"gold\" exposed=\"false\" dc=\"0\"><troll /></mineral>",
				MineralEvent.class, false);
		assertMissingProperty("<mineral dc=\"0\" exposed=\"false\" />",
				MineralEvent.class, KIND_PROPERTY, false);
		assertMissingProperty(
				"<mineral kind=\"gold\" exposed=\"false\" />",
				MineralEvent.class, "dc", false);
		assertMissingProperty("<mineral dc=\"0\" kind=\"gold\" />",
				MineralEvent.class, "exposed", false);
		assertMissingProperty(
				"<mineral kind=\"kind\" exposed=\"true\" dc=\"0\" />",
				MineralEvent.class, "id", true);
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
					+ kind, new StoneEvent(kind, 8, 1), StoneEvent.class); // NOPMD
			assertSerialization("Second StoneEvent test, reflection, kind: "
					+ kind, new StoneEvent(kind, 15, 2), StoneEvent.class); // NOPMD
		}
		final StoneEvent three = new StoneEvent(StoneKind.Marble, 10, 3);
		final String xml = three.toXML().replace("kind", "stone");
		assertEquals("Deserialization of deprecated stone idiom, reflection",
				three, reader.readXML(new StringReader(xml), StoneEvent.class,
						true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialization of deprecated stone idiom, non-reflection",
				three, reader.readXML(new StringReader(xml), StoneEvent.class,
						false, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(xml, StoneEvent.class, "stone", true);
		assertUnwantedChild(
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>",
				StoneEvent.class, false);
		assertMissingProperty("<stone kind=\"stone\" />",
				StoneEvent.class, "dc", false);
		assertMissingProperty("<stone dc=\"10\" />",
				StoneEvent.class, KIND_PROPERTY, false);
		assertMissingProperty("<stone kind=\"kind\" dc=\"0\" />",
				StoneEvent.class, "id", true);
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
								+ status + " and size " + size,
						new TownEvent(status, size, 10, "one", 1), TownEvent.class); // NOPMD
				assertSerialization(
						"Second TownEvent serialization test, reflection, status "
								+ status + " and size " + size,
						new TownEvent(status, size, 40, "two", 2), TownEvent.class); // NOPMD
			}
		}
		final TownEvent three = new TownEvent(TownStatus.Active, TownSize.Small, 30, "", 3);
		assertSerialization(
				"Serialization of TownEvent without a name, reflection",
				three, TownEvent.class, new Warning(Warning.Action.Ignore));
		assertMissingProperty(three.toXML(), TownEvent.class, "name", true);
		assertMissingProperty("<town status=\"active\" size=\"small\"/>", TownEvent.class,
				"dc", false);
		assertMissingProperty("<town dc=\"0\" status=\"active\" />",
				TownEvent.class, "size", false);
		assertMissingProperty("<town dc=\"0\" size=\"small\" />",
				TownEvent.class, STATUS_PROPERTY, false);
		assertMissingProperty(
				"<town dc=\"0\" size=\"small\" status=\"active\" name=\"name\" />",
				TownEvent.class, "id", true);
	}
}
