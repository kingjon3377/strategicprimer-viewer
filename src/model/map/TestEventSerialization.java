package model.map;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.fixtures.resources.Battlefield;
import model.map.fixtures.resources.Cave;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.towns.CityEvent;
import model.map.fixtures.towns.FortificationEvent;
import model.map.fixtures.towns.TownEvent;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;

import org.junit.Test;

import util.Warning;
import controller.map.formatexceptions.SPFormatException;

/**
 * A class to test the serialization of Events.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TestEventSerialization extends BaseTestFixtureSerialization { // NOPMD
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
	 * Test serialization of CaveEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testCaveSerialization() throws XMLStreamException, SPFormatException,
			IOException {
		// ESCA-JAVA0076:
		assertSerialization("First CaveEvent serialization test, reflection",
				new Cave(10, 0), Cave.class);
		assertSerialization(
				"Second CaveEvent serialization test, reflection",
				new Cave(30, 1), Cave.class);
		assertUnwantedChild("<cave dc=\"10\"><troll /></cave>",
				Cave.class, false);
		assertMissingProperty("<cave />", Cave.class, "dc", false);
		assertMissingProperty("<cave dc=\"10\" />", Cave.class, "id", true);
	}

	/**
	 * Test serialization of CityEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testCitySerialization() throws XMLStreamException,
			SPFormatException, IOException {
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				// ESCA-JAVA0076:
				assertSerialization(
						"First CityEvent serialization test, reflection, status "
								+ status + ", size " + size, new CityEvent(// NOPMD
								status, size, 10, "oneCity", 0), CityEvent.class);
				// ESCA-JAVA0076:
				assertSerialization(
						"Second CityEvent serialization test, reflection, status "
								+ status + ", size " + size, new CityEvent(// NOPMD
								status, size, 40, "twoCity", 1), CityEvent.class);
			}
		}
		final CityEvent three = new CityEvent(TownStatus.Active,
				TownSize.Small, 30, "", 3);
		assertSerialization(
				"Serialization of CityEvent without a name, reflection", three,
				CityEvent.class, new Warning(Warning.Action.Ignore));
		assertMissingProperty(createSerializedForm(three, true),
				CityEvent.class, NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(three, false),
				CityEvent.class, NAME_PROPERTY, true);
		assertMissingProperty(
				"<city status=\"active\" size=\"small\" name=\"name\" dc=\"0\" />",
				CityEvent.class, "id", true);
		assertUnwantedChild(
				"<city status=\"active\" size=\"small\" name=\"name\" dc=\"0\"><troll /></city>",
				CityEvent.class, false);
	}

	/**
	 * Test serialization of FortificationEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testFortificationSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
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
		assertMissingProperty(createSerializedForm(three, true),
				FortificationEvent.class, NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(three, false),
				FortificationEvent.class, NAME_PROPERTY, true);
		assertMissingProperty(
				"<fortification status=\"active\" size=\"small\" name=\"name\" dc=\"0\" />",
				FortificationEvent.class, "id", true);
		assertUnwantedChild(
				"<fortification status=\"active\" size=\"small\" name=\"name\" dc=\"0\">"
						+ "<troll /></fortification>",
				FortificationEvent.class, false);
	}

	/**
	 * Test serialization of MineralEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testMineralSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization(
				"First MineralEvent serialization test, reflection",
				new MineralVein("one", true, 10, 1), MineralVein.class);
		final MineralVein two = new MineralVein("two", false, 35, 2);
		assertSerialization(
				"Second MineralEvent serialization test, reflection", two,
				MineralVein.class);
		final String oldKindProperty = "mineral"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of deprecated Mineral idiom",
				two,
				createSerializedForm(two, true).replace(KIND_PROPERTY,
						oldKindProperty), MineralVein.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated Mineral idiom",
				two,
				createSerializedForm(two, false).replace(KIND_PROPERTY,
						oldKindProperty), MineralVein.class, oldKindProperty);
		assertUnwantedChild(
				"<mineral kind=\"gold\" exposed=\"false\" dc=\"0\"><troll /></mineral>",
				MineralVein.class, false);
		assertMissingProperty("<mineral dc=\"0\" exposed=\"false\" />",
				MineralVein.class, KIND_PROPERTY, false);
		assertMissingProperty("<mineral kind=\"gold\" exposed=\"false\" />",
				MineralVein.class, "dc", false);
		assertMissingProperty("<mineral dc=\"0\" kind=\"gold\" />",
				MineralVein.class, "exposed", false);
		assertMissingProperty(
				"<mineral kind=\"kind\" exposed=\"true\" dc=\"0\" />",
				MineralVein.class, "id", true);
	}

	/**
	 * Test serialization of StoneEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testStoneSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		for (final StoneKind kind : StoneKind.values()) {
			assertSerialization("First StoneEvent test, reflection, kind: "
					+ kind, new StoneDeposit(kind, 8, 1), StoneDeposit.class); // NOPMD
			assertSerialization("Second StoneEvent test, reflection, kind: "
					+ kind, new StoneDeposit(kind, 15, 2), StoneDeposit.class); // NOPMD
		}
		final StoneDeposit three = new StoneDeposit(StoneKind.Marble, 10, 3);
		final String oldKindProperty = "stone"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of deprecated stone idiom",
				three,
				createSerializedForm(three, true).replace(KIND_PROPERTY,
						oldKindProperty), StoneDeposit.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated stone idiom",
				three,
				createSerializedForm(three, false).replace(KIND_PROPERTY,
						oldKindProperty), StoneDeposit.class, oldKindProperty);
		assertUnwantedChild(
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>",
				StoneDeposit.class, false);
		assertMissingProperty("<stone kind=\"stone\" />", StoneDeposit.class,
				"dc", false);
		assertMissingProperty("<stone dc=\"10\" />", StoneDeposit.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<stone kind=\"kind\" dc=\"0\" />",
				StoneDeposit.class, "id", true);
	}

	/**
	 * Test serialization of TownEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testTownSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		for (final TownStatus status : TownStatus.values()) {
			for (final TownSize size : TownSize.values()) {
				assertSerialization(
						"First TownEvent serialization test, reflection, status "
								+ status + " and size " + size, new TownEvent(// NOPMD
								status, size, 10, "one", 1), TownEvent.class);
				assertSerialization(
						"Second TownEvent serialization test, reflection, status "
								+ status + " and size " + size, new TownEvent(// NOPMD
								status, size, 40, "two", 2), TownEvent.class);
			}
		}
		final TownEvent three = new TownEvent(TownStatus.Active,
				TownSize.Small, 30, "", 3);
		assertSerialization(
				"Serialization of TownEvent without a name, reflection", three,
				TownEvent.class, new Warning(Warning.Action.Ignore));
		assertMissingProperty(createSerializedForm(three, true),
				TownEvent.class, NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(three, false),
				TownEvent.class, NAME_PROPERTY, true);
		assertMissingProperty("<town status=\"active\" size=\"small\"/>",
				TownEvent.class, "dc", false);
		assertMissingProperty("<town dc=\"0\" status=\"active\" />",
				TownEvent.class, "size", false);
		assertMissingProperty("<town dc=\"0\" size=\"small\" />",
				TownEvent.class, STATUS_PROPERTY, false);
		assertMissingProperty(
				"<town dc=\"0\" size=\"small\" status=\"active\" name=\"name\" />",
				TownEvent.class, "id", true);
		assertUnwantedChild(
				"<town status=\"active\" size=\"small\" name=\"name\" dc=\"0\"><troll /></town>",
				TownEvent.class, false);
	}
	/**
	 * First test of serialization of BattlefieldEvents.
	 *
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testBattlefieldSerialization() throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First BattlefieldEvent serialization test",
				new Battlefield(10, 0), Battlefield.class);
		assertSerialization("Second BattlefieldEvent serialization test",
				new Battlefield(30, 1), Battlefield.class);
		assertUnwantedChild("<battlefield dc=\"10\"><troll /></battlefield>",
				Battlefield.class, false);
		assertMissingProperty("<battlefield />", Battlefield.class, "dc",
				false);
		assertMissingProperty("<battlefield dc=\"10\" />",
				Battlefield.class, "id", true);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestEventSerialization";
	}
}
