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
import controller.map.ISPReader;
import controller.map.SPFormatException;

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
		reader = createReader();
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
				reader, new Grove(true, true, "firstGrove", 1), Grove.class);
		assertSerialization("Second test of Grove serialization, reflection",
				reader, new Grove(true, false, "secondGrove", 2), Grove.class);
		assertSerialization("Third test of Grove serialization, reflection",
				reader, new Grove(false, true, "thirdGrove", 3), Grove.class);
		assertSerialization("Fourth test of Grove serialization, reflection",
				reader, new Grove(false, false, "four", 4), Grove.class);
		assertUnwantedChild(reader, "<grove wild=\"true\" kind=\"kind\"><troll /></grove>", Grove.class, false);
		assertMissingProperty(reader, "<grove />", Grove.class, "wild", false);
		assertMissingProperty(reader, "<grove wild=\"false\" />", Grove.class, "kind", false);
		assertDeprecatedProperty(reader, "<grove wild=\"true\" tree=\"tree\" id=\"0\" />", Grove.class, "tree", true);
		assertMissingProperty(reader, "<grove wild=\"true\" kind=\"kind\" />", Grove.class, "id", true);
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
				reader, new Meadow("firstMeadow", true, true, 1), Meadow.class);
		assertSerialization("Second test of Meadow serialization, reflection",
				reader, new Meadow("secondMeadow", true, false, 2), Meadow.class);
		assertSerialization("Third test of Meadow serialization, reflection",
				reader, new Meadow("three", false, true, 3), Meadow.class);
		assertSerialization("Fourth test of Meadow serialization, reflection",
				reader, new Meadow("four", false, false, 4), Meadow.class);
		assertUnwantedChild(reader, "<meadow kind=\"flax\" cultivated=\"false\"><troll /></meadow>",
				Meadow.class, false);
		assertMissingProperty(reader, "<meadow cultivated=\"false\" />",
				Meadow.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<meadow kind=\"flax\" />",
				Meadow.class, "cultivated", false);
		assertMissingProperty(reader,
				"<field kind=\"kind\" cultivated=\"true\" />", Meadow.class,
				"id", true);
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
				reader, new Mine("one", TownStatus.Active, 1), Mine.class);
		assertSerialization("Second test of Mine serialization, reflection",
				reader, new Mine("two", TownStatus.Abandoned, 2), Mine.class);
		assertSerialization("Third test of Mine serialization, reflection",
				reader, new Mine("three", TownStatus.Burned, 3), Mine.class);
		final Mine four = new Mine("four", TownStatus.Ruined, 4);
		assertSerialization("Fourth test of Mine serialization, reflection",
				reader, four, Mine.class);
		final String xml = four.toXML().replace(KIND_PROPERTY, "product");
		assertEquals("Deprecated Mine idiom, reflection", four, reader.readXML(
				new StringReader(xml), Mine.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deprecated Mine idiom, non-reflection", four, reader.readXML(
				new StringReader(xml), Mine.class, false, new Warning(Warning.Action.Ignore)));
		assertDeprecatedProperty(reader, xml, Mine.class, "product", true);
		assertUnwantedChild(reader, "<mine kind=\"gold\" status=\"active\"><troll /></mine>",
				Mine.class, false);
		assertMissingProperty(reader, "<mine status=\"active\"/>",
				Mine.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<mine kind=\"gold\"/>",
				Mine.class, STATUS_PROPERTY, false);
		assertMissingProperty(reader,
				"<mine kind=\"kind\" status=\"active\" />", Mine.class, "id",
				true);
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
				reader, new Shrub("one", 1), Shrub.class);
		final Shrub two = new Shrub("two", 2);
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
		assertUnwantedChild(reader, "<shrub kind=\"shrub\"><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty(reader, "<shrub />", Shrub.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<shrub kind=\"kind\" />", Shrub.class, "id", true);
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
			final Village one = new Village(status, "villageOne", 1); // NOPMD
			assertSerialization("First Village serialization test, reflection, "
					+ status, reader, one, Village.class);
			final Village two = new Village(status, "villageTwo", 2); // NOPMD
			assertSerialization("Second Village serialization test, reflection, "
					+ status, reader, two, Village.class);
		}
		final Village three = new Village(TownStatus.Abandoned, "", 3);
		assertEquals(
				"Serialization of village with no or empty name does The Right Thing, reflection",
				three, reader.readXML(new StringReader(three.toXML()), Village.class,
						true, new Warning(Warning.Action.Ignore)));
		assertEquals(
				"Serialization of village with no or empty name does The Right Thing, non-reflection",
				three, reader.readXML(new StringReader(three.toXML()), Village.class,
						false, new Warning(Warning.Action.Ignore)));
		assertMissingProperty(reader, three.toXML(), Village.class, "name", true);
		assertUnwantedChild(reader, "<village status=\"active\"><village /></village>",
				Village.class, false);
		assertMissingProperty(reader, "<village />", Village.class, STATUS_PROPERTY,
				false);
		assertMissingProperty(reader,
				"<village name=\"name\" status=\"active\" />", Village.class,
				"id", true);
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
		assertMissingProperty(reader, "<unit owner=\"1\" name=\"name\" id=\"0\" />", Unit.class,
				KIND_PROPERTY, true);
		assertMissingProperty(reader, "<unit owner=\"1\" kind=\"\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertUnwantedChild(reader, "<unit><unit /></unit>", Unit.class, false);
		final Unit one = new Unit(new Player(1, ""), "unitType", "unitName", 1);
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
		final Unit two = new Unit(new Player(2, ""), "", "name", 2);
		assertEquals("Deserialize unit with no kind properly, reflection", two,
				reader.readXML(new StringReader(two.toXML()), Unit.class, true,
						new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with no kind properly, non-reflection", two,
				reader.readXML(new StringReader(two.toXML()), Unit.class, false,
						new Warning(Warning.Action.Ignore)));
		final Unit three = new Unit(new Player(-1, ""), "kind", "unitThree", 3);
		assertEquals("Deserialize unit with no owner properly, reflection",
				three, reader.readXML(new StringReader(
						"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />"),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with no owner properly, non-reflection",
				three, reader.readXML(new StringReader(
						"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />"),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
		final Unit four = new Unit(new Player(3, ""), "unitKind", "", 4);
		assertEquals("Deserialize unit with no name properly, reflection",
				four, reader.readXML(new StringReader(four.toXML()),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with no name properly, non-reflection",
				four, reader.readXML(new StringReader(four.toXML()),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with empty name properly, reflection",
				four, reader.readXML(new StringReader(
						"<unit owner=\"3\" kind=\"unitKind\" name=\"\" id=\"4\" />"),
						Unit.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Deserialize unit with empty name properly, non-reflection",
				four, reader.readXML(new StringReader(
						"<unit owner=\"3\" kind=\"unitKind\" name=\"\" id=\"4\"/>"),
						Unit.class, false, new Warning(Warning.Action.Ignore)));
		assertMissingProperty(reader,
				"<unit owner=\"1\" kind=\"kind\" name=\"name\" />", Unit.class,
				"id", true);
	}
}
