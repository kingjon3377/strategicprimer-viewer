package model.map.fixtures;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.events.TownStatus;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.ISPReader;
import controller.map.simplexml.SimpleXMLReader;

/**
 * Another class to test serialization of TileFixtures.
 * 
 * @author Jonathan Lovelace
 */
public final class TestMoreFixtureSerialization extends
		BaseTestFixtureSerialization {
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
	private ISPReader reader;

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
		assertSerialization("First test of Grove serialization, reflection",
				reader, new Grove(true, true, "firstGrove"), Grove.class);
		assertSerialization("Second test of Grove serialization, reflection",
				reader, new Grove(true, false, "secondGrove"), Grove.class);
		assertSerialization("Third test of Grove serialization, reflection",
				reader, new Grove(false, true, "thirdGrove"), Grove.class);
		assertSerialization("Fourth test of Grove serialization, reflection",
				reader, new Grove(false, false, "four"), Grove.class);
		assertUnwantedChild(reader, "<grove><troll /></grove>", Grove.class, false);
		assertMissingProperty(reader, "<grove />", Grove.class, "wild", false);
		assertMissingProperty(reader, "<grove wild=\"false\" />", Grove.class, "kind", false);
		assertDeprecatedProperty(reader, "<grove wild=\"true\" tree=\"tree\" />", Grove.class, "tree", true);
	}

	/**
	 * Test serialization of Meadows, including error-checking.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testMeadowSerialization() throws XMLStreamException,
			SPFormatException {
		assertSerialization("First test of Meadow serialization, reflection",
				reader, new Meadow("firstMeadow", true, true), Meadow.class);
		assertSerialization("Second test of Meadow serialization, reflection",
				reader, new Meadow("secondMeadow", true, false), Meadow.class);
		assertSerialization("Third test of Meadow serialization, reflection",
				reader, new Meadow("three", false, true), Meadow.class);
		assertSerialization("Fourth test of Meadow serialization, reflection",
				reader, new Meadow("four", false, false), Meadow.class);
		assertUnwantedChild(reader, "<meadow><troll /></meadow>",
				Meadow.class, false);
		assertMissingProperty(reader, "<meadow cultivated=\"false\" />",
				Meadow.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<meadow kind=\"flax\" />",
				Meadow.class, "cultivated", false);
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
		assertSerialization("First test of Mine serialization, reflection",
				reader, new Mine("one", TownStatus.Active), Mine.class);
		assertSerialization("Second test of Mine serialization, reflection",
				reader, new Mine("two", TownStatus.Abandoned), Mine.class);
		assertSerialization("Third test of Mine serialization, reflection",
				reader, new Mine("three", TownStatus.Burned), Mine.class);
		final Mine four = new Mine("four", TownStatus.Ruined);
		assertSerialization("Fourth test of Mine serialization, reflection",
				reader, four, Mine.class);
		final String xml = four.toXML().replace(KIND_PROPERTY, "product");
		assertEquals("Deprecated Mine idiom, reflection", four, reader.readXML(
				new StringReader(xml), Mine.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deprecated Mine idiom, non-reflection", four, reader.readXML(
				new StringReader(xml), Mine.class, false, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, xml, Mine.class, "product", true);
		assertUnwantedChild(reader, "<mine><troll /></mine>",
				Mine.class, false);
		assertMissingProperty(reader, "<mine status=\"active\"/>",
				Mine.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<mine kind=\"gold\"/>",
				Mine.class, STATUS_PROPERTY, false);
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
		assertSerialization("First test of Shrub serialization, reflection",
				reader, new Shrub("one"), Shrub.class);
		final Shrub two = new Shrub("two");
		assertSerialization("Second test of Shrub serialization, reflection",
				reader, two, Shrub.class);
		final String xml = two.toXML().replace(KIND_PROPERTY, "shrub");
		assertEquals("Deserialization of mangled shrub, reflection", two,
				reader.readXML(new StringReader(xml), Shrub.class, true,
						new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialization of mangled shrub, non-reflection", two,
				reader.readXML(new StringReader(xml), Shrub.class, true,
						new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, xml, Shrub.class, "shrub", true);
		assertUnwantedChild(reader, "<shrub><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty(reader, "<shrub />", Shrub.class, KIND_PROPERTY, false);
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
		assertSerialization("First test of TextFixture serialization, reflection",
				reader, one, TextFixture.class);
		final TextFixture two = new TextFixture("two", 2);
		assertSerialization("Second test of TextFixture serialization, reflection",
				reader, two, TextFixture.class);
		final TextFixture three = new TextFixture("three", 10);
		assertSerialization("Third test of TextFixture serialization, reflection",
				reader, three, TextFixture.class);
		assertUnwantedChild(reader, "<text turn=\"1\"><troll /></text>",
				TextFixture.class, false);
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
			assertSerialization("First Village serialization test, reflection, "
					+ status, reader, one, Village.class);
			final Village two = new Village(status, "villageTwo"); // NOPMD
			assertSerialization("Second Village serialization test, reflection, "
					+ status, reader, two, Village.class);
		}
		final Village three = new Village(TownStatus.Abandoned, "");
		assertEquals(
				"Serialization of village with no or empty name does The Right Thing, reflection",
				three, reader.readXML(new StringReader(three.toXML()), Village.class,
						true, new Warning(Warning.Action.Ignore)));
		assertEquals(
				"Serialization of village with no or empty name does The Right Thing, non-reflection",
				three, reader.readXML(new StringReader(three.toXML()), Village.class,
						false, new Warning(Warning.Action.Ignore)));
		assertMissingProperty(reader, three.toXML(), Village.class, "name", true);
		assertUnwantedChild(reader, "<village><village /></village>",
				Village.class, false);
		assertMissingProperty(reader, "<village />", Village.class, STATUS_PROPERTY,
				false);
	}
	
	/**
	 * Test that a Unit should have an owner, and other errors and warnings.
	 * TODO: Combine this method with the main Unit tests.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testUnitWarnings() throws XMLStreamException, SPFormatException { // NOPMD
		assertMissingProperty(reader, "<unit />", Unit.class, "owner", true);
		assertMissingProperty(reader, "<unit owner=\"\" />", Unit.class,
				"owner", true);
		assertMissingProperty(reader, "<unit owner=\"1\" />", Unit.class,
				KIND_PROPERTY, true);
		assertMissingProperty(reader, "<unit owner=\"1\" kind=\"\" />",
				Unit.class, KIND_PROPERTY, true);
		assertUnwantedChild(reader, "<unit><unit /></unit>", Unit.class, false);
		final Unit one = new Unit(new Player(1, ""), "unitType", "unitName");
		final String oneXMLMangled = one.toXML().replace(KIND_PROPERTY, "type");
		assertEquals(
				"Deserialize properly with deprecated use of 'type' for unit kind, non-reflection",
				one, reader.readXML(
						new StringReader(oneXMLMangled),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
		assertEquals(
				"Deserialize properly with deprecated use of 'type' for unit kind, reflection",
				one, reader.readXML(
						new StringReader(oneXMLMangled),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, oneXMLMangled, Unit.class, "type", true);
		assertMissingProperty(reader, "<unit owner=\"2\" kind=\"unit\" />", Unit.class, "name", true);
		final Unit two = new Unit(new Player(2, ""), "", "name");
		assertEquals("Deserialize unit with no kind properly, reflection", two,
				reader.readXML(new StringReader(two.toXML()), Unit.class, true,
						new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with no kind properly, non-reflection", two,
				reader.readXML(new StringReader(two.toXML()), Unit.class, false,
						new Warning(Warning.Action.Ignore)));
		final Unit three = new Unit(new Player(-1, ""), "kind", "unitThree");
		assertEquals("Deserialize unit with no owner properly, reflection",
				three, reader.readXML(new StringReader(
						"<unit kind=\"kind\" name=\"unitThree\" />"),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with no owner properly, non-reflection",
				three, reader.readXML(new StringReader(
						"<unit kind=\"kind\" name=\"unitThree\" />"),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
		final Unit four = new Unit(new Player(3, ""), "unitKind", "");
		assertEquals("Deserialize unit with no name properly, reflection",
				four, reader.readXML(new StringReader(four.toXML()),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with no name properly, non-reflection",
				four, reader.readXML(new StringReader(four.toXML()),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with empty name properly, reflection",
				four, reader.readXML(new StringReader(
						"<unit owner=\"3\" kind=\"unitKind\" name=\"\" />"),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with empty name properly, non-reflection",
				four, reader.readXML(new StringReader(
						"<unit owner=\"3\" kind=\"unitKind\" name=\"\" />"),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
	}
}
