package model.map.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;

import org.junit.Before;
import org.junit.Test;

import util.FatalWarning;
import util.Warning;

import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A class to test the serialization of Events.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class TestEventSerialization extends BaseTestFixtureSerialization {
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
		reader = new SimpleXMLReader();
	}

	/**
	 * The XML reader we'll use to test.
	 */
	private SimpleXMLReader reader;

	/**
	 * Test serialization of BattlefieldEvents.
	 * 
	 * @throws SPFormatException
	 *             on SP format problems
	 * @throws XMLStreamException
	 *             on XML reading problems
	 */
	@Test
	public void testBattefieldSerialization() throws XMLStreamException,
			SPFormatException {
		final BattlefieldEvent one = new BattlefieldEvent(10);
		assertEquals("First BattlefieldEvent serialization test, reflection",
				one,
				helpSerialization(reader, one, BattlefieldEvent.class, true));
		assertEquals(
				"First BattlefieldEvent serialization test, non-reflection",
				one,
				helpSerialization(reader, one, BattlefieldEvent.class, false));
		final BattlefieldEvent two = new BattlefieldEvent(30);
		assertEquals("Second BattlefieldEvent serialization test, reflection",
				two,
				helpSerialization(reader, two, BattlefieldEvent.class, true));
		assertEquals(
				"Second BattlefieldEvent serialization test, non-reflection",
				two,
				helpSerialization(reader, two, BattlefieldEvent.class, false));
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
		final CaveEvent one = new CaveEvent(10);
		assertEquals("First CaveEvent serialization test, reflection", one,
				helpSerialization(reader, one, CaveEvent.class, true));
		assertEquals(
				"First BattlefieldEvent serialization test, non-reflection",
				one, helpSerialization(reader, one, CaveEvent.class, false));
		final CaveEvent two = new CaveEvent(30);
		assertEquals("Second BattlefieldEvent serialization test, reflection",
				two, helpSerialization(reader, two, CaveEvent.class, true));
		assertEquals(
				"Second BattlefieldEvent serialization test, non-reflection",
				two, helpSerialization(reader, two, CaveEvent.class, false));
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
				final CityEvent one = new CityEvent(status, size, 10, "one"); // NOPMD
				assertEquals(
						"First CityEvent serialization test, reflection, status "
								+ status + " and size " + size, one, // NOPMD
						helpSerialization(reader, one, CityEvent.class, true));
				assertEquals(
						"First CityEvent serialization test, non-reflection, status "
								+ status + " and size " + size, one,
						helpSerialization(reader, one, CityEvent.class, false));
				final CityEvent two = new CityEvent(status, size, 40, "two"); // NOPMD
				assertEquals(
						"Second CityEvent serialization test, reflection, status "
								+ status + " and size " + size, two,
						helpSerialization(reader, two, CityEvent.class, true));
				assertEquals(
						"Second CityEvent serialization test, non-reflection, status "
								+ status + " and size " + size, two,
						helpSerialization(reader, two, CityEvent.class, false));
			}
		}
		final CityEvent three = new CityEvent(TownStatus.Active, TownSize.Small, 30, "");
		assertEquals(
				"Serialization of CityEvent without a name, reflection",
				three,
				reader.readXML(new StringReader(three.toXML()), CityEvent.class, true, Warning.INSTANCE));
		assertEquals(
				"Serialization of CityEvent without a name, non-reflection",
				three,
				reader.readXML(new StringReader(three.toXML()), CityEvent.class, false, Warning.INSTANCE));
		try {
			helpSerialization(reader, three, CityEvent.class, true);
			fail("Should have warned about CityEvent without a name");
		} catch (FatalWarning except) {
			assertTrue("Warning about CityEvent without a name",
					except.getCause() instanceof SPFormatException);
		}
		try {
			helpSerialization(reader, three, CityEvent.class, false);
			fail("Should have warned about CityEvent without a name");
		} catch (FatalWarning except) {
			assertTrue("Warning about CityEvent without a name",
					except.getCause() instanceof SPFormatException);
		}
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
				final FortificationEvent one = new FortificationEvent(status, // NOPMD
						size, 10, "one");
				assertEquals(
						"First FortificationEvent serialization test, reflection, status "
								+ status + " and size " + size,
						one, // NOPMD
						helpSerialization(reader, one,
								FortificationEvent.class, true));
				assertEquals(
						"First FortificationEvent serialization test, non-reflection, status "
								+ status + " and size " + size,
						one,
						helpSerialization(reader, one,
								FortificationEvent.class, false));
				final FortificationEvent two = new FortificationEvent(status, // NOPMD
						size, 40, "two");
				assertEquals(
						"Second FortificationEvent serialization test, reflection, status "
								+ status + " and size " + size,
						two,
						helpSerialization(reader, two,
								FortificationEvent.class, true));
				assertEquals(
						"Second FortificationEvent serialization test, non-reflection, status "
								+ status + " and size " + size,
						two,
						helpSerialization(reader, two,
								FortificationEvent.class, false));
			}
		}
		final FortificationEvent three = new FortificationEvent(TownStatus.Active, TownSize.Small, 30, "");
		assertEquals(
				"Serialization of FortificationEvent without a name, reflection",
				three,
				reader.readXML(new StringReader(three.toXML()), FortificationEvent.class, true, Warning.INSTANCE));
		assertEquals(
				"Serialization of FortificationEvent without a name, non-reflection",
				three,
				reader.readXML(new StringReader(three.toXML()), FortificationEvent.class, false, Warning.INSTANCE));
		try {
			helpSerialization(reader, three, FortificationEvent.class, true);
			fail("Should have warned about FortificationEvent without a name");
		} catch (FatalWarning except) {
			assertTrue("Warning about FortificationEvent without a name",
					except.getCause() instanceof SPFormatException);
		}
		try {
			helpSerialization(reader, three, FortificationEvent.class, false);
			fail("Should have warned about FortificationEvent without a name");
		} catch (FatalWarning except) {
			assertTrue("Warning about FortificationEvent without a name",
					except.getCause() instanceof SPFormatException);
		}
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
		final MineralEvent one = new MineralEvent("one", true, 10);
		assertEquals("First MineralEvent serialization test, reflection", one,
				helpSerialization(reader, one, MineralEvent.class, true));
		assertEquals("First MineralEvent serialization test, non-reflection",
				one, helpSerialization(reader, one, MineralEvent.class, false));
		final MineralEvent two = new MineralEvent("two", false, 35);
		assertEquals("Second MineralEvent serialization test, reflection", two,
				helpSerialization(reader, two, MineralEvent.class, true));
		assertEquals("Second MineralEvent serialization test, non-reflection",
				two, helpSerialization(reader, two, MineralEvent.class, false));
		final String xml = two.toXML().replace("kind", "mineral");
		assertEquals("Deserialization of deprecated Mineral idiom, reflection",
				two, reader.readXML(new StringReader(xml), MineralEvent.class,
						true, Warning.INSTANCE));
		assertEquals("Deserialization of deprecated Mineral idiom, non-reflection",
				two, reader.readXML(new StringReader(xml), MineralEvent.class,
						false, Warning.INSTANCE));
		try {
			reader.readXML(new StringReader(xml), MineralEvent.class, true, warner());
			fail("Should warn about deprecated Mineral idiom");
		} catch (FatalWarning except) {
			assertTrue("Warned about deprecated Mineral idiom, reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(xml), MineralEvent.class, false, warner());
			fail("Should warn about deprecated Mineral idiom");
		} catch (FatalWarning except) {
			assertTrue("Warned about deprecated Mineral idiom, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
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
			final StoneEvent one = new StoneEvent(kind, 8); // NOPMD
			assertEquals("First StoneEvent test, reflection, kind: " + kind,
					one, helpSerialization(reader, one, StoneEvent.class, true));
			assertEquals(
					"First StoneEvent test, non-reflection, kind: " + kind,
					one,
					helpSerialization(reader, one, StoneEvent.class, false));
			final StoneEvent two = new StoneEvent(kind, 15); // NOPMD
			assertEquals("Second StoneEvent test, reflection, kind: " + kind,
					two, helpSerialization(reader, two, StoneEvent.class, true));
			assertEquals("Second StoneEvent test, non-reflection, kind: "
					+ kind, two,
					helpSerialization(reader, two, StoneEvent.class, false));
		}
		final StoneEvent three = new StoneEvent(StoneKind.Marble, 10);
		final String xml = three.toXML().replace("kind", "stone");
		assertEquals("Deserialization of deprecated stone idiom, reflection",
				three, reader.readXML(new StringReader(xml), StoneEvent.class,
						true, Warning.INSTANCE));
		assertEquals("Deserialization of deprecated stone idiom, non-reflection",
				three, reader.readXML(new StringReader(xml), StoneEvent.class,
						false, Warning.INSTANCE));
		try {
			reader.readXML(new StringReader(xml), StoneEvent.class, true, warner());
			fail("Should have warned about deprecated stone idiom");
		} catch (FatalWarning except) {
			assertTrue("Warning about deprecated stone idiom, reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(xml), StoneEvent.class, false, warner());
			fail("Should have warned about deprecated stone idiom");
		} catch (FatalWarning except) {
			assertTrue("Warning about deprecated stone idiom, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
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
				final TownEvent one = new TownEvent(status, size, 10, "one"); // NOPMD
				assertEquals(
						"First TownEvent serialization test, reflection, status "
								+ status + " and size " + size, one, // NOPMD
						helpSerialization(reader, one, TownEvent.class, true));
				assertEquals(
						"First TownEvent serialization test, non-reflection, status "
								+ status + " and size " + size, one,
						helpSerialization(reader, one, TownEvent.class, false));
				final TownEvent two = new TownEvent(status, size, 40, "two"); // NOPMD
				assertEquals(
						"Second TownEvent serialization test, reflection, status "
								+ status + " and size " + size, two,
						helpSerialization(reader, two, TownEvent.class, true));
				assertEquals(
						"Second TownEvent serialization test, non-reflection, status "
								+ status + " and size " + size, two,
						helpSerialization(reader, two, TownEvent.class, false));
			}
		}
		final TownEvent three = new TownEvent(TownStatus.Active, TownSize.Small, 30, "");
		assertEquals(
				"Serialization of TownEvent without a name, reflection",
				three,
				reader.readXML(new StringReader(three.toXML()), TownEvent.class, true, Warning.INSTANCE));
		assertEquals(
				"Serialization of TownEvent without a name, non-reflection",
				three,
				reader.readXML(new StringReader(three.toXML()), TownEvent.class, false, Warning.INSTANCE));
		try {
			helpSerialization(reader, three, TownEvent.class, true);
			fail("Should have warned about TownEvent without a name");
		} catch (FatalWarning except) {
			assertTrue("Warning about TownEvent without a name",
					except.getCause() instanceof SPFormatException);
		}
		try {
			helpSerialization(reader, three, TownEvent.class, false);
			fail("Should have warned about TownEvent without a name");
		} catch (FatalWarning except) {
			assertTrue("Warning about TownEvent without a name",
					except.getCause() instanceof SPFormatException);
		}
	}
}
