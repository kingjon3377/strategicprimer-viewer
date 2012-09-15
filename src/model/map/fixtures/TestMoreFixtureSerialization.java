package model.map.fixtures;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.mobile.Worker;
import model.map.fixtures.mobile.worker.Job;
import model.map.fixtures.mobile.worker.Skill;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;

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
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testGroveSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Grove serialization, reflection",
				new Grove(true, true, "firstGrove", 1, FAKE_FILENAME),
				Grove.class);
		assertSerialization("Second test of Grove serialization, reflection",
				new Grove(true, false, "secondGrove", 2, FAKE_FILENAME),
				Grove.class);
		assertSerialization("Third test of Grove serialization, reflection",
				new Grove(false, true, "thirdGrove", 3, FAKE_FILENAME),
				Grove.class);
		assertSerialization("Fourth test of Grove serialization, reflection",
				new Grove(false, false, "four", 4, FAKE_FILENAME), Grove.class);
		assertUnwantedChild(
				"<grove wild=\"true\" kind=\"kind\"><troll /></grove>",
				Grove.class, false);
		assertMissingProperty("<grove />", Grove.class, "cultivated", false);
		assertMissingProperty("<grove wild=\"false\" />", Grove.class, "kind",
				false);
		assertDeprecatedProperty(
				"<grove cultivated=\"true\" tree=\"tree\" id=\"0\" />", Grove.class,
				"tree", true);
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
				new Meadow("firstMeadow", true, true, 1, FieldStatus.Fallow,
						FAKE_FILENAME), Meadow.class);
		assertSerialization("Second test of Meadow serialization, reflection",
				new Meadow("secondMeadow", true, false, 2, FieldStatus.Seeding,
						FAKE_FILENAME), Meadow.class);
		assertSerialization("Third test of Meadow serialization, reflection",
				new Meadow("three", false, true, 3, FieldStatus.Growing,
						FAKE_FILENAME), Meadow.class);
		assertSerialization("Fourth test of Meadow serialization, reflection",
				new Meadow("four", false, false, 4, FieldStatus.Bearing,
						FAKE_FILENAME), Meadow.class);
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
				new Mine("one", TownStatus.Active, 1, FAKE_FILENAME),
				Mine.class);
		assertSerialization("Second test of Mine serialization, reflection",
				new Mine("two", TownStatus.Abandoned, 2, FAKE_FILENAME),
				Mine.class);
		assertSerialization("Third test of Mine serialization, reflection",
				new Mine("three", TownStatus.Burned, 3, FAKE_FILENAME),
				Mine.class);
		final Mine four = new Mine("four", TownStatus.Ruined, 4, FAKE_FILENAME);
		assertSerialization("Fourth test of Mine serialization, reflection",
				four, Mine.class);
		final String oldKindProperty = "product"; // NOPMD
		assertDeprecatedDeserialization(
				"Deprecated Mine idiom",
				four,
				createSerializedForm(four, true).replace(KIND_PROPERTY,
						oldKindProperty), Mine.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deprecated Mine idiom",
				four,
				createSerializedForm(four, false).replace(KIND_PROPERTY,
						oldKindProperty), Mine.class, oldKindProperty);
		assertUnwantedChild(
				"<mine kind=\"gold\" status=\"active\"><troll /></mine>",
				Mine.class, false);
		assertMissingProperty("<mine status=\"active\"/>", Mine.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<mine kind=\"gold\"/>", Mine.class,
				STATUS_PROPERTY, false);
		assertMissingProperty("<mine kind=\"kind\" status=\"active\" />",
				Mine.class, "id", true);
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
				new Shrub("one", 1, FAKE_FILENAME), Shrub.class);
		final Shrub two = new Shrub("two", 2, FAKE_FILENAME);
		assertSerialization("Second test of Shrub serialization, reflection",
				two, Shrub.class);
		final String oldKindProperty = "shrub"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of mangled shrub, reflection",
				two,
				createSerializedForm(two, true).replace(KIND_PROPERTY,
						oldKindProperty), Shrub.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of mangled shrub, reflection",
				two,
				createSerializedForm(two, false).replace(KIND_PROPERTY,
						oldKindProperty), Shrub.class, oldKindProperty);
		assertUnwantedChild("<shrub kind=\"shrub\"><troll /></shrub>",
				Shrub.class, false);
		assertMissingProperty("<shrub />", Shrub.class, KIND_PROPERTY, false);
		assertMissingProperty("<shrub kind=\"kind\" />", Shrub.class, "id",
				true);
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
		for (final TownStatus status : TownStatus.values()) {
			final Village one = new Village(status, "villageOne", 1, // NOPMD
					FAKE_FILENAME);
			assertSerialization("First Village serialization test, " + status,
					one, Village.class);
			final Village two = new Village(status, "villageTwo", 2, // NOPMD
					FAKE_FILENAME);
			assertSerialization("2nd Village serialization test,  " + status,
					two, Village.class);
		}
		final Village three = new Village(TownStatus.Abandoned, "", 3,
				FAKE_FILENAME);
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
	}

	/**
	 * Test that a Unit should have an owner, and other errors and warnings.
	 * TODO: Combine this method with the main Unit tests.
	 *
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testUnitWarnings() throws XMLStreamException,
			SPFormatException, IOException { // NOPMD
		assertMissingProperty("<unit name=\"name\" />", Unit.class, "owner", true);
		assertMissingProperty("<unit owner=\"\" name=\"name\" />", Unit.class, "owner", true);
		assertMissingProperty("<unit owner=\"1\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertMissingProperty(
				"<unit owner=\"1\" kind=\"\" name=\"name\" id=\"0\" />",
				Unit.class, KIND_PROPERTY, true);
		assertUnwantedChild("<unit><unit /></unit>", Unit.class, false);
		final Unit one = new Unit(new Player(1, "", FAKE_FILENAME), "unitType",
				"unitName", 1, FAKE_FILENAME);
		final String oldKindProperty = "type"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				one,
				createSerializedForm(one, true).replace(KIND_PROPERTY,
						oldKindProperty), Unit.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialize properly with deprecated use of 'type' for unit kind",
				one,
				createSerializedForm(one, false).replace(KIND_PROPERTY,
						oldKindProperty), Unit.class, oldKindProperty);
		assertMissingProperty("<unit owner=\"2\" kind=\"unit\" />", Unit.class,
				NAME_PROPERTY, true);
		assertSerialization(
				"Deserialize unit with no kind properly, reflection", new Unit(
						new Player(2, "", FAKE_FILENAME), "", NAME_PROPERTY, 2,
						FAKE_FILENAME), Unit.class, new Warning(
						Warning.Action.Ignore));
		assertMissingPropertyDeserialization(
				"Deserialize unit with no owner properly", new Unit(new Player(
						-1, "", FAKE_FILENAME), "kind", "unitThree", 3,
						FAKE_FILENAME),
				"<unit kind=\"kind\" name=\"unitThree\" id=\"3\" />",
				Unit.class, "owner");
		final Unit four = new Unit(new Player(3, "", FAKE_FILENAME),
				"unitKind", "", 4, FAKE_FILENAME);
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
		final Unit one = new Unit(new Player(1, "", FAKE_FILENAME), "unitType",
				"unitName", 1, FAKE_FILENAME);
		one.addMember(new Animal("animal", false, true, "wild", 2, FAKE_FILENAME));
		assertSerialization("Unit can have an animal as a member", one, Unit.class);
		one.addMember(new Worker("worker", FAKE_FILENAME, 3));
		assertSerialization("Unit can have a worker as a member", one, Unit.class);
		one.addMember(new Worker("second", FAKE_FILENAME, 4, new Job("job", 0, FAKE_FILENAME,
				new Skill("skill", 1, 2, FAKE_FILENAME))));
		assertSerialization("Worker can have jobs", one, Unit.class);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestMoreFixtureSerialization";
	}
}
