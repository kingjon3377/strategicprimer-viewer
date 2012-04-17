package model.map.fixtures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.events.TownStatus;

import org.junit.Before;
import org.junit.Test;

import util.FatalWarning;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * Another class to test serialization of TileFixtures.
 * 
 * @author Jonathan Lovelace
 */
public final class TestMoreFixtureSerialization extends
		BaseTestFixtureSerialization {
	/**
	 * Constructor.
	 */
	public TestMoreFixtureSerialization() {
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
	 * Test serialization of Groves.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testGroveSerialization() throws XMLStreamException,
			SPFormatException {
		final Grove one = new Grove(true, true, "firstGrove");
		assertEquals("First test of Grove serialization, reflection", one,
				helpSerialization(reader, one, Grove.class, true));
		assertEquals("First test of Grove serialization, non-reflection", one,
				helpSerialization(reader, one, Grove.class, false));
		final Grove two = new Grove(true, false, "secondGrove");
		assertEquals("Second test of Grove serialization, reflection", two,
				helpSerialization(reader, two, Grove.class, true));
		assertEquals("Second test of Grove serialization, non-reflection", two,
				helpSerialization(reader, two, Grove.class, false));
		final Grove three = new Grove(false, true, "thirdGrove");
		assertEquals("Third test of Grove serialization, reflection", three,
				helpSerialization(reader, three, Grove.class, true));
		assertEquals("Third test of Grove serialization, non-reflection",
				three, helpSerialization(reader, three, Grove.class, false));
		final Grove four = new Grove(false, false, "four");
		assertEquals("Fourth test of Grove serialization, reflection", four,
				helpSerialization(reader, four, Grove.class, true));
		assertEquals("Fourth test of Grove serialization, non-reflection",
				four, helpSerialization(reader, four, Grove.class, false));
	}

	/**
	 * Test serialization of Meadows.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testMeadowSerialization() throws XMLStreamException,
			SPFormatException {
		final Meadow one = new Meadow("firstMeadow", true, true);
		assertEquals("First test of Meadow serialization, reflection", one,
				helpSerialization(reader, one, Meadow.class, true));
		assertEquals("First test of Meadow serialization, non-reflection", one,
				helpSerialization(reader, one, Meadow.class, false));
		final Meadow two = new Meadow("secondMeadow", true, false);
		assertEquals("Second test of Meadow serialization, reflection", two,
				helpSerialization(reader, two, Meadow.class, true));
		assertEquals("Second test of Meadow serialization, non-reflection",
				two, helpSerialization(reader, two, Meadow.class, false));
		final Meadow three = new Meadow("three", false, true);
		assertEquals("Third test of Meadow serialization, reflection", three,
				helpSerialization(reader, three, Meadow.class, true));
		assertEquals("Third test of Meadow serialization, non-reflection",
				three, helpSerialization(reader, three, Meadow.class, false));
		final Meadow four = new Meadow("four", false, false);
		assertEquals("Fourth test of Meadow serialization, reflection", four,
				helpSerialization(reader, four, Meadow.class, true));
		assertEquals("Fourth test of Meadow serialization, non-reflection",
				four, helpSerialization(reader, four, Meadow.class, false));
	}

	/**
	 * Test serialization of Mines.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testMineSerialization() throws XMLStreamException,
			SPFormatException {
		final Mine one = new Mine("one", TownStatus.Active);
		assertEquals("First test of Mine serialization, reflection", one,
				helpSerialization(reader, one, Mine.class, true));
		assertEquals("First test of Mine serialization, non-reflection", one,
				helpSerialization(reader, one, Mine.class, false));
		final Mine two = new Mine("two", TownStatus.Abandoned);
		assertEquals("Second test of Mine serialization, reflection", two,
				helpSerialization(reader, two, Mine.class, true));
		assertEquals("Second test of Mine serialization, non-reflection", two,
				helpSerialization(reader, two, Mine.class, false));
		final Mine three = new Mine("three", TownStatus.Burned);
		assertEquals("Third test of Mine serialization, reflection", three,
				helpSerialization(reader, three, Mine.class, true));
		assertEquals("Third test of Mine serialization, non-reflection", three,
				helpSerialization(reader, three, Mine.class, false));
		final Mine four = new Mine("four", TownStatus.Ruined);
		assertEquals("Fourth test of Mine serialization, reflection", four,
				helpSerialization(reader, four, Mine.class, true));
		assertEquals("Fourth test of Mine serialization, non-reflection", four,
				helpSerialization(reader, four, Mine.class, false));
		final String xml = four.toXML().replace("kind", "product"); // NOPMD
		assertEquals("Deprecated Mine idiom, reflection", four, reader.readXML(
				new StringReader(xml), Mine.class, true, Warning.INSTANCE));
		assertEquals("Deprecated Mine idiom, non-reflection", four, reader.readXML(
				new StringReader(xml), Mine.class, false, Warning.INSTANCE));
		try {
			reader.readXML(new StringReader(xml), Mine.class, false, warner());
			fail("Should warn about deprecated Mine idiom");
		} catch (FatalWarning except) {
			assertTrue("Warned about deprecated Mine idiom",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(xml), Mine.class, true, warner());
			fail("Should warn about deprecated Mine idiom");
		} catch (FatalWarning except) {
			assertTrue("Warned about deprecated Mine idiom",
					except.getCause() instanceof SPFormatException);
		}
	}

	/**
	 * Test serialization of Shrubs.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testShrubSerialization() throws XMLStreamException,
			SPFormatException {
		final Shrub one = new Shrub("one");
		assertEquals("First test of Shrub serialization, reflection", one,
				helpSerialization(reader, one, Shrub.class, true));
		assertEquals("First test of Shrub serialization, non-reflection", one,
				helpSerialization(reader, one, Shrub.class, false));
		final Shrub two = new Shrub("two");
		assertEquals("Second test of Shrub serialization, reflection", two,
				helpSerialization(reader, two, Shrub.class, true));
		assertEquals("Second test of Shrub serialization, non-reflection", two,
				helpSerialization(reader, two, Shrub.class, false));
		final String xml = two.toXML().replace("kind", "shrub");
		assertEquals("Deserialization of mangled shrub, reflection", two,
				reader.readXML(new StringReader(xml), Shrub.class, true,
						Warning.INSTANCE));
		assertEquals("Deserialization of mangled shrub, non-reflection", two,
				reader.readXML(new StringReader(xml), Shrub.class, true,
						Warning.INSTANCE));
		try {
			reader.readXML(new StringReader(xml), Shrub.class, false, warner());
			fail("Should have warned about depreated shrub idiom");
		} catch (FatalWarning except) {
			assertTrue("Warning about deprecated shrub idiom, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(xml), Shrub.class, true, warner());
			fail("Should have warned about depreated shrub idiom");
		} catch (FatalWarning except) {
			assertTrue("Warning about deprecated shrub idiom, reflection",
					except.getCause() instanceof SPFormatException);
		}
	}

	/**
	 * Test serialization of TextFixtures.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testTextSerialization() throws XMLStreamException,
			SPFormatException {
		final TextFixture one = new TextFixture("one", -1);
		assertEquals("First test of TextFixture serialization, reflection",
				one, helpSerialization(reader, one, TextFixture.class, true));
		assertEquals("First test of TextFixture serialization, non-reflection",
				one, helpSerialization(reader, one, TextFixture.class, false));
		final TextFixture two = new TextFixture("two", 2);
		assertEquals("Second test of TextFixture serialization, reflection",
				two, helpSerialization(reader, two, TextFixture.class, true));
		assertEquals(
				"Second test of TextFixture serialization, non-reflection",
				two, helpSerialization(reader, two, TextFixture.class, false));
		final TextFixture three = new TextFixture("three", 10);
		assertEquals("Third test of TextFixture serialization, reflection",
				three,
				helpSerialization(reader, three, TextFixture.class, true));
		assertEquals("Third test of TextFixture serialization, non-reflection",
				three,
				helpSerialization(reader, three, TextFixture.class, false));
	}

	/**
	 * Test Village serialization.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testVillageSerialization() throws XMLStreamException,
			SPFormatException {
		for (TownStatus status : TownStatus.values()) {
			final Village one = new Village(status, "villageOne"); // NOPMD
			assertEquals("First Village serialization test, reflection, "
					+ status, one,
					helpSerialization(reader, one, Village.class, true));
			assertEquals("First Village serialization test, non-reflection, "
					+ status, one,
					helpSerialization(reader, one, Village.class, false));
			final Village two = new Village(status, "villageTwo"); // NOPMD
			assertEquals("Second Village serialization test, reflection, "
					+ status, two,
					helpSerialization(reader, two, Village.class, true));
			assertEquals("Second Village serialization test, non-reflection, "
					+ status, two,
					helpSerialization(reader, two, Village.class, false));
		}
		final Village three = new Village(TownStatus.Abandoned, "");
		assertEquals(
				"Serialization of village with no or empty name does The Right Thing, reflection",
				three, reader.readXML(new StringReader(three.toXML()), Village.class,
						true, Warning.INSTANCE));
		assertEquals(
				"Serialization of village with no or empty name does The Right Thing, non-reflection",
				three, reader.readXML(new StringReader(three.toXML()), Village.class,
						false, Warning.INSTANCE));
		try {
			helpSerialization(reader, three, Village.class, true);
			fail("Expected warning on deserialization of Village without name");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Village without name, reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			helpSerialization(reader, three, Village.class, false);
			fail("Expected warning on deserialization of Village without name");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Village without name, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
	}
	/**
	 * Test that a Unit should have an owner.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testUnitWarnings() throws XMLStreamException, SPFormatException { // NOPMD
		try {
			reader.readXML(new StringReader("<unit />"), Unit.class, true, warner());
			fail("Expected objection to unit without owner");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit without owner, reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit />"), Unit.class, false, warner());
			fail("Expected objection to unit without owner");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit without owner, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit owner=\"\" />"), Unit.class, false, warner());
			fail("Expected objection to unit with empty owner");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit with empty owner, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit owner=\"\" />"), Unit.class, true, warner());
			fail("Expected objection to unit with empty owner");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit with empty owner, reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit owner=\"1\" />"), Unit.class, false, warner());
			fail("Expected objection to unit with no kind");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit with no kind, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit owner=\"1\" />"), Unit.class, true, warner());
			fail("Expected objection to unit with no kind");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit with no kind, reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit owner=\"1\" kind=\"\" />"), Unit.class, false, warner());
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit with empty kind, non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader("<unit owner=\"1\" kind=\"\" />"), Unit.class, true, warner());
		} catch (FatalWarning except) {
			assertTrue(
					"Warning on deserialization of Unit with empty kind, reflection",
					except.getCause() instanceof SPFormatException);
		}
	}
	/**
	 * Test more Unit warnings.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testMoreUnitWarnings() throws XMLStreamException, SPFormatException {
		final Unit one = new Unit(new Player(1, ""), "unitType", "unitName");
		final String oneXMLMangled = one.toXML().replace("kind", "type");
		assertEquals(
				"Deserialize properly with deprecated use of 'type' for unit kind, non-reflection",
				one, reader.readXML(
						new StringReader(oneXMLMangled),
						Unit.class, false, Warning.INSTANCE));
		assertEquals(
				"Deserialize properly with deprecated use of 'type' for unit kind, reflection",
				one, reader.readXML(
						new StringReader(oneXMLMangled),
						Unit.class, true, Warning.INSTANCE));
		try {
			reader.readXML(new StringReader(oneXMLMangled), Unit.class, false, warner());
			fail("Should have warned about deprecated use of 'type' for unit kind: non-reflection");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning about deprecated use of 'type' for unit kind: non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(oneXMLMangled), Unit.class, true, warner());
			fail("Should have warned about deprecated use of 'type' for unit kind: reflection");
		} catch (FatalWarning except) {
			assertTrue(
					"Warning about deprecated use of 'type' for unit kind: reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(
					"<unit owner=\"2\" kind=\"unit\" />"), Unit.class, false,
					warner());
			fail("Should have warned about missing 'name'");
		} catch (FatalWarning except) {
			assertTrue("Warning about missing unit 'name': non-reflection",
					except.getCause() instanceof SPFormatException);
		}
		try {
			reader.readXML(new StringReader(
					"<unit owner=\"2\" kind=\"unit\" />"), Unit.class, true,
					warner());
			fail("Should have warned about missing 'name'");
		} catch (FatalWarning except) {
			assertTrue("Warning about missing unit 'name': reflection",
					except.getCause() instanceof SPFormatException);
		}
		final Unit two = new Unit(new Player(2, ""), "", "name");
		assertEquals("Deserialize unit with no kind properly, reflection", two,
				reader.readXML(new StringReader(two.toXML()), Unit.class, true,
						Warning.INSTANCE));
		assertEquals("Deserialize unit with no kind properly, non-reflection", two,
				reader.readXML(new StringReader(two.toXML()), Unit.class, false,
						Warning.INSTANCE));
		final Unit three = new Unit(new Player(-1, ""), "kind", "unitThree");
		assertEquals("Deserialize unit with no owner properly, reflection",
				three, reader.readXML(new StringReader(
						"<unit kind=\"kind\" name=\"unitThree\" />"),
						Unit.class, true, Warning.INSTANCE));
		assertEquals("Deserialize unit with no owner properly, non-reflection",
				three, reader.readXML(new StringReader(
						"<unit kind=\"kind\" name=\"unitThree\" />"),
						Unit.class, false, Warning.INSTANCE));
		final Unit four = new Unit(new Player(3, ""), "unitKind", "");
		assertEquals("Deserialize unit with no name properly, reflection",
				four, reader.readXML(new StringReader(four.toXML()),
						Unit.class, true, Warning.INSTANCE));
		assertEquals("Deserialize unit with no name properly, non-reflection",
				four, reader.readXML(new StringReader(four.toXML()),
						Unit.class, false, Warning.INSTANCE));
		assertEquals("Deserialize unit with empty name properly, reflection",
				four, reader.readXML(new StringReader(
						"<unit owner=\"3\" kind=\"unitKind\" name=\"\" />"),
						Unit.class, true, Warning.INSTANCE));
		assertEquals("Deserialize unit with empty name properly, non-reflection",
				four, reader.readXML(new StringReader(
						"<unit owner=\"3\" kind=\"unitKind\" name=\"\" />"),
						Unit.class, false, Warning.INSTANCE));
	}
}
