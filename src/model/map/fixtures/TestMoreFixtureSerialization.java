package model.map.fixtures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.mobile.worker.WorkerStats;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;

import org.junit.Test;

import util.Warning;
import controller.map.formatexceptions.SPFormatException;

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
	private static final String OWNER_PROPERTY = "owner";
	/**
	 * The default race for a worker.
	 */
	private static final String DEFAULT_RACE = "human";
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
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testGroveSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Grove serialization, reflection",
				new Grove(true, true, "firstGrove", 1), Grove.class);
		assertSerialization("Second test of Grove serialization, reflection",
				new Grove(true, false, "secondGrove", 2), Grove.class);
		assertSerialization("Third test of Grove serialization, reflection",
				new Grove(false, true, "thirdGrove", 3), Grove.class);
		assertSerialization("Fourth test of Grove serialization, reflection",
				new Grove(false, false, "four", 4), Grove.class);
		assertUnwantedChild(
				"<grove wild=\"true\" kind=\"kind\"><troll /></grove>",
				Grove.class, false);
		assertMissingProperty("<grove />", Grove.class, "cultivated", false);
		assertMissingProperty("<grove wild=\"false\" />", Grove.class, "kind",
				false);
		assertDeprecatedProperty(
				"<grove cultivated=\"true\" tree=\"tree\" id=\"0\" />",
				Grove.class, "tree", true);
		assertMissingProperty("<grove cultivated=\"true\" kind=\"kind\" />",
				Grove.class, "id", true);
		assertDeprecatedProperty(
				"<grove wild=\"true\" kind=\"tree\" id=\"0\" />", Grove.class,
				"wild", true);
		super.assertEquivalentForms(
				"Assert that wild is the inverse of cultivated",
				"<grove wild=\"true\" kind=\"tree\" id=\"0\" />",
				"<grove cultivated=\"false\" kind=\"tree\" id=\"0\" />",
				Grove.class, Warning.Action.Ignore);
		assertImageSerialization("Grove image property is preserved",
				new Grove(false, false, "five", 5), Grove.class);
	}

	/**
	 * Test serialization of Meadows, including error-checking.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testMeadowSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Meadow serialization, reflection",
				new Meadow("firstMeadow", true, true, 1, FieldStatus.Fallow),
				Meadow.class);
		assertSerialization(
				"Second test of Meadow serialization, reflection",
				new Meadow("secondMeadow", true, false, 2, FieldStatus.Seeding),
				Meadow.class);
		assertSerialization("Third test of Meadow serialization, reflection",
				new Meadow("three", false, true, 3, FieldStatus.Growing),
				Meadow.class);
		assertSerialization("Fourth test of Meadow serialization, reflection",
				new Meadow("four", false, false, 4, FieldStatus.Bearing),
				Meadow.class);
		assertUnwantedChild(
				"<meadow kind=\"flax\" cultivated=\"false\"><troll /></meadow>",
				Meadow.class, false);
		assertMissingProperty("<meadow cultivated=\"false\" />", Meadow.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<meadow kind=\"flax\" />", Meadow.class,
				"cultivated", false);
		assertMissingProperty("<field kind=\"kind\" cultivated=\"true\" />",
				Meadow.class, "id", true);
		assertMissingProperty(
				"<field kind=\"kind\" cultivated=\"true\" id=\"0\" />",
				Meadow.class, "status", true);
		assertImageSerialization("Meadow image property is preserved",
				new Meadow("five", false, false, 5, FieldStatus.Fallow),
				Meadow.class);
	}

	/**
	 * Test serialization of Mines.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testMineSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Mine serialization, reflection",
				new Mine("one", TownStatus.Active, 1), Mine.class);
		assertSerialization("Second test of Mine serialization, reflection",
				new Mine("two", TownStatus.Abandoned, 2), Mine.class);
		assertSerialization("Third test of Mine serialization, reflection",
				new Mine("three", TownStatus.Burned, 3), Mine.class);
		final Mine four = new Mine("four", TownStatus.Ruined, 4);
		assertSerialization("Fourth test of Mine serialization, reflection",
				four, Mine.class);
		final String oldKindProperty = "product"; // NOPMD
		final String testDataFourA = createSerializedForm(four, true).replace(KIND_PROPERTY,
				oldKindProperty);
		assert testDataFourA != null;
		assertDeprecatedDeserialization("Deprecated Mine idiom", four,
				testDataFourA, Mine.class, oldKindProperty);
		final String testDataFour = createSerializedForm(four, false).replace(KIND_PROPERTY,
				oldKindProperty);
		assert testDataFour != null;
		assertDeprecatedDeserialization("Deprecated Mine idiom", four,
				testDataFour, Mine.class, oldKindProperty);
		assertUnwantedChild(
				"<mine kind=\"gold\" status=\"active\"><troll /></mine>",
				Mine.class, false);
		assertMissingProperty("<mine status=\"active\"/>", Mine.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<mine kind=\"gold\"/>", Mine.class,
				STATUS_PROPERTY, false);
		assertMissingProperty("<mine kind=\"kind\" status=\"active\" />",
				Mine.class, "id", true);
		assertImageSerialization("Mine image property is preserved", four,
				Mine.class);
	}

	/**
	 * Test serialization of Shrubs.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testShrubSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Shrub serialization, reflection",
				new Shrub("one", 1), Shrub.class);
		final Shrub two = new Shrub("two", 2);
		assertSerialization("Second test of Shrub serialization, reflection",
				two, Shrub.class);
		final String oldKindProperty = "shrub"; // NOPMD
		final String testDataTwoRefl = createSerializedForm(two, true).replace(KIND_PROPERTY,
				oldKindProperty);
		assert testDataTwoRefl != null;
		assertDeprecatedDeserialization(
				"Deserialization of mangled shrub, reflection", two,
				testDataTwoRefl, Shrub.class, oldKindProperty);
		final String testDataTwoNon = createSerializedForm(two, false).replace(KIND_PROPERTY,
				oldKindProperty);
		assert testDataTwoNon != null;
		assertDeprecatedDeserialization(
				"Deserialization of mangled shrub, reflection", two,
				testDataTwoNon, Shrub.class, oldKindProperty);
		assertUnwantedChild("<shrub kind=\"shrub\"><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty("<shrub />", Shrub.class, KIND_PROPERTY, false);
		assertMissingProperty("<shrub kind=\"kind\" />", Shrub.class, "id",
				true);
		assertImageSerialization("Shrub image property is preserved", two,
				Shrub.class);
	}

	/**
	 * Test serialization of TextFixtures.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testTextSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		final TextFixture one = new TextFixture("one", -1);
		assertSerialization(
				"First test of TextFixture serialization, reflection", one,
				TextFixture.class);
		final TextFixture two = new TextFixture("two", 2);
		assertSerialization(
				"Second test of TextFixture serialization, reflection", two,
				TextFixture.class);
		final TextFixture three = new TextFixture("three", 10);
		assertSerialization(
				"Third test of TextFixture serialization, reflection", three,
				TextFixture.class);
		assertUnwantedChild("<text turn=\"1\"><troll /></text>",
				TextFixture.class, false);
		assertImageSerialization("Text image property is preserved", three,
				TextFixture.class);
	}

	/**
	 * Test Village serialization.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testVillageSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		final Player owner = new Player(-1, "");
		for (final TownStatus status : TownStatus.values()) {
			if (status == null) {
				continue;
			}
			final Village one = new Village(status, "villageOne", 1, owner, // NOPMD
					"human");
			assertSerialization("First Village serialization test, " + status,
					one, Village.class);
			final Village two = new Village(status, "villageTwo", 2, owner, // NOPMD
					"dwarf");
			assertSerialization("2nd Village serialization test,  " + status,
					two, Village.class);
		}
		final Village three = new Village(TownStatus.Abandoned, "", 3, owner,
				"elf");
		assertMissingPropertyDeserialization(
				"Serialization of village with no or empty name does The Right Thing",
				three, createSerializedForm(three, true), Village.class,
				NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Serialization of village with no or empty name does The Right Thing",
				three, createSerializedForm(three, false), Village.class,
				NAME_PROPERTY);
		assertUnwantedChild("<village status=\"active\"><village /></village>",
				Village.class, false);
		assertMissingProperty("<village />", Village.class, STATUS_PROPERTY,
				false);
		assertMissingProperty("<village name=\"name\" status=\"active\" />",
				Village.class, "id", true);
		assertMissingProperty(
				"<village name=\"name\" status=\"active\" id=\"0\" />",
				Village.class, OWNER_PROPERTY, true);
		assertImageSerialization("Village image property is preserved", three,
				Village.class);
	}

	/**
	 * Test that a Unit should have an owner, and other errors and warnings.
	 *
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testUnitWarnings() throws XMLStreamException,
			SPFormatException, IOException { // NOPMD
		assertMissingProperty("<unit name=\"name\" />", Unit.class,
				OWNER_PROPERTY, true);
		assertMissingProperty("<unit owner=\"\" name=\"name\" />", Unit.class,
				OWNER_PROPERTY, true);
		assertMissingProperty("<unit owner=\"1\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertMissingProperty(
				"<unit owner=\"1\" kind=\"\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertUnwantedChild("<unit><unit /></unit>", Unit.class, false);
		final Unit one = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		final String oldKindProperty = "type"; // NOPMD
		final String testDataOld = createSerializedForm(one, true).replace(KIND_PROPERTY,
				oldKindProperty);
		assert testDataOld != null;
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				one,
				testDataOld, Unit.class, oldKindProperty);
		final String testDataNew = createSerializedForm(one, false).replace(KIND_PROPERTY,
				oldKindProperty);
		assert testDataNew != null;
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				one,
				testDataNew, Unit.class, oldKindProperty);
		assertMissingProperty("<unit owner=\"2\" kind=\"unit\" />", Unit.class,
				NAME_PROPERTY, true);
		assertSerialization(
				"Deserialize unit with no kind properly, reflection", new Unit(
						new Player(2, ""), "", NAME_PROPERTY, 2), Unit.class,
				new Warning(Warning.Action.Ignore));
		assertMissingPropertyDeserialization(
				"Deserialize unit with no owner properly", new Unit(new Player(
						-1, ""), "kind", "unitThree", 3),
				"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />",
				Unit.class, OWNER_PROPERTY);
		final Unit four = new Unit(new Player(3, ""), "unitKind", "", 4);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", four,
				createSerializedForm(four, true), Unit.class, NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with no name properly", four,
				createSerializedForm(four, false), Unit.class, NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Deserialize unit with empty name properly", four,
				"<unit owner=\"3\" kind=\"unitKind\" name=\"\" id=\"4\" />",
				Unit.class, NAME_PROPERTY);
		assertMissingProperty(
				"<unit owner=\"1\" kind=\"kind\" name=\"name\" />", Unit.class,
				"id", true);
	}

	/**
	 * Test unit-member serialization.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testUnitMemberSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		final Unit one = new Unit(new Player(1, ""), "unitType", "unitName", 1);
		one.addMember(new Animal("animal", false, true, "wild", 2));
		assertSerialization("Unit can have an animal as a member", one,
				Unit.class);
		one.addMember(new Worker("worker", DEFAULT_RACE, 3));
		assertSerialization("Unit can have a worker as a member", one,
				Unit.class);
		one.addMember(new Worker("second", "elf", 4, new Job("job", 0,
				new Skill("skill", 1, 2))));
		assertSerialization("Worker can have jobs", one, Unit.class);
		assertForwardDeserialization(
				"Explicit specification of default race works", new Worker(
						"third", DEFAULT_RACE, 5),
				"<worker name=\"third\" race=\"human\" id=\"5\" />",
				Worker.class);
		// ESCA-JAVA0076:
		assertDeprecatedDeserialization(
				"'miscellaneous' skill with level should be warned about",
				new Worker("fourth", DEFAULT_RACE, 6, new Job("fifth", 0,
						new Skill("miscellaneous", 1, 0))),
				"<worker name=\"fourth\" id=\"6\"><job name=\"fifth\" level=\"0\">"
						+ "<skill name=\"miscellaneous\" level=\"1\" hours=\"0\" /></job></worker>",
				Worker.class, "miscellaneous");
		assertSerialization(
				"but 'miscellaneous' skill without levels causes no warnings",
				new Worker("sixth", DEFAULT_RACE, 7, new Job("seventh", 0,
						new Skill("miscellaneous", 0, 20))), Worker.class,
				new Warning(Warning.Action.Die));
		assertSerialization("and levels in another skill cause no warnings",
				new Worker("fourth", DEFAULT_RACE, 8, new Job("fifth", 0,
						new Skill("odd-skill", 1, 0))), Worker.class,
				new Warning(Warning.Action.Die));
		final Worker two = new Worker("sixth", "dwarf", 9);
		two.setStats(new WorkerStats(0, 0, 1, 2, 3, 4, 5, 6));
		assertSerialization("Worker can have skills", two, Worker.class);
		assertImageSerialization("Worker image property is preserved", two,
				Worker.class);
	}

	/**
	 * Test serialization of units' orders.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testOrdersSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		final Player player = new Player(0, "");
		final Unit one = new Unit(player, "kind of unit", "name of unit", 2);
		final Unit two = new Unit(player, "kind of unit", "name of unit", 2);
		two.setOrders("some orders");
		assertEquals("Orders have no effect on equals", one, two);
		assertSerialization("Orders don't mess up deserialization", two,
				Unit.class, new Warning(Warning.Action.Die));
		assertTrue("Serialized form contains orders",
				createSerializedForm(two, true).contains("some orders"));
		assertTrue("Serialized form contains orders",
				createSerializedForm(two, false).contains("some orders"));

	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestMoreFixtureSerialization";
	}
}
