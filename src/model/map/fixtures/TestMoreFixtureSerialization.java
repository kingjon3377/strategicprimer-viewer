package model.map.fixtures;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.events.TownStatus;

import org.junit.Test;

import util.Warning;
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
	private static final String NAME_PROPERTY = "name";
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";
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
				new Grove(true, true, "firstGrove", 1), Grove.class);
		assertSerialization("Second test of Grove serialization, reflection",
				new Grove(true, false, "secondGrove", 2), Grove.class);
		assertSerialization("Third test of Grove serialization, reflection",
				new Grove(false, true, "thirdGrove", 3), Grove.class);
		assertSerialization("Fourth test of Grove serialization, reflection",
				new Grove(false, false, "four", 4), Grove.class);
		assertUnwantedChild("<grove wild=\"true\" kind=\"kind\"><troll /></grove>", Grove.class, false);
		assertMissingProperty("<grove />", Grove.class, "wild", false);
		assertMissingProperty("<grove wild=\"false\" />", Grove.class, "kind", false);
		assertDeprecatedProperty("<grove wild=\"true\" tree=\"tree\" id=\"0\" />", Grove.class, "tree", true);
		assertMissingProperty("<grove wild=\"true\" kind=\"kind\" />", Grove.class, "id", true);
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
				new Meadow("firstMeadow", true, true, 1), Meadow.class);
		assertSerialization("Second test of Meadow serialization, reflection",
				new Meadow("secondMeadow", true, false, 2), Meadow.class);
		assertSerialization("Third test of Meadow serialization, reflection",
				new Meadow("three", false, true, 3), Meadow.class);
		assertSerialization("Fourth test of Meadow serialization, reflection",
				new Meadow("four", false, false, 4), Meadow.class);
		assertUnwantedChild("<meadow kind=\"flax\" cultivated=\"false\"><troll /></meadow>",
				Meadow.class, false);
		assertMissingProperty("<meadow cultivated=\"false\" />",
				Meadow.class, KIND_PROPERTY, false);
		assertMissingProperty("<meadow kind=\"flax\" />",
				Meadow.class, "cultivated", false);
		assertMissingProperty(
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
				new Mine("one", TownStatus.Active, 1), Mine.class);
		assertSerialization("Second test of Mine serialization, reflection",
				new Mine("two", TownStatus.Abandoned, 2), Mine.class);
		assertSerialization("Third test of Mine serialization, reflection",
				new Mine("three", TownStatus.Burned, 3), Mine.class);
		final Mine four = new Mine("four", TownStatus.Ruined, 4);
		assertSerialization("Fourth test of Mine serialization, reflection",
				four, Mine.class);
		final String xml = four.toXML().replace(KIND_PROPERTY, "product");
		assertDeprecatedDeserialization("Deprecated Mine idiom", four, xml,
				Mine.class, "product");
		assertUnwantedChild("<mine kind=\"gold\" status=\"active\"><troll /></mine>",
				Mine.class, false);
		assertMissingProperty("<mine status=\"active\"/>",
				Mine.class, KIND_PROPERTY, false);
		assertMissingProperty("<mine kind=\"gold\"/>",
				Mine.class, STATUS_PROPERTY, false);
		assertMissingProperty(
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
				new Shrub("one", 1), Shrub.class);
		final Shrub two = new Shrub("two", 2);
		assertSerialization("Second test of Shrub serialization, reflection",
				two, Shrub.class);
		final String xml = two.toXML().replace(KIND_PROPERTY, "shrub");
		assertDeprecatedDeserialization(
				"Deserialization of mangled shrub, reflection", two, xml,
				Shrub.class, "shrub");
		assertUnwantedChild("<shrub kind=\"shrub\"><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty("<shrub />", Shrub.class, KIND_PROPERTY, false);
		assertMissingProperty("<shrub kind=\"kind\" />", Shrub.class, "id", true);
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
				one, TextFixture.class);
		final TextFixture two = new TextFixture("two", 2);
		assertSerialization("Second test of TextFixture serialization, reflection",
				two, TextFixture.class);
		final TextFixture three = new TextFixture("three", 10);
		assertSerialization("Third test of TextFixture serialization, reflection",
				three, TextFixture.class);
		assertUnwantedChild("<text turn=\"1\"><troll /></text>",
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
					+ status, one, Village.class);
			final Village two = new Village(status, "villageTwo", 2); // NOPMD
			assertSerialization("Second Village serialization test, reflection, "
					+ status, two, Village.class);
		}
		final Village three = new Village(TownStatus.Abandoned, "", 3);
		assertMissingPropertyDeserialization(
				"Serialization of village with no or empty name does The Right Thing",
				three, three.toXML(), Village.class, NAME_PROPERTY);
		assertUnwantedChild("<village status=\"active\"><village /></village>",
				Village.class, false);
		assertMissingProperty("<village />", Village.class, STATUS_PROPERTY,
				false);
		assertMissingProperty(
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
		assertMissingProperty("<unit />", Unit.class, "owner", true);
		assertMissingProperty("<unit owner=\"\" />", Unit.class,
				"owner", true);
		assertMissingProperty("<unit owner=\"1\" name=\"name\" id=\"0\" />", Unit.class,
				KIND_PROPERTY, true);
		assertMissingProperty("<unit owner=\"1\" kind=\"\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertUnwantedChild("<unit><unit /></unit>", Unit.class, false);
		final Unit one = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		final String oneXMLMangled = one.toXML().replace(KIND_PROPERTY, "type");
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				one, oneXMLMangled,
						Unit.class, "type");
		assertMissingProperty("<unit owner=\"2\" kind=\"unit\" />", Unit.class, NAME_PROPERTY, true);
		assertSerialization(
				"Deserialize unit with no kind properly, reflection", new Unit(
						new Player(2, ""), "", NAME_PROPERTY, 2), Unit.class,
				new Warning(Warning.Action.Ignore));
		assertMissingPropertyDeserialization(
				"Deserialize unit with no owner properly", new Unit(new Player(
						-1, ""), "kind", "unitThree", 3),
				"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />",
				Unit.class, "owner");
		final Unit four = new Unit(new Player(3, ""), "unitKind", "", 4);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", four, four.toXML(),
				Unit.class, NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with empty name properly", four,
				"<unit owner=\"3\" kind=\"unitKind\" name=\"\" id=\"4\" />",
				Unit.class, NAME_PROPERTY);
		assertMissingProperty(
				"<unit owner=\"1\" kind=\"kind\" name=\"name\" />", Unit.class,
				"id", true);
	}
}
