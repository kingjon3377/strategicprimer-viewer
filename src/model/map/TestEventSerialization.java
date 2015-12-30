package model.map;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.towns.City;
import model.map.fixtures.towns.Fortification;
import model.map.fixtures.towns.Town;
import model.map.fixtures.towns.TownSize;
import model.map.fixtures.towns.TownStatus;
import org.junit.Test;
import util.NullCleaner;
import util.Warning;
import util.Warning.Action;

/**
 * A class to test the serialization of Events.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TestEventSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String NAME_PROPERTY = "name";
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Compiled pattern of it.
	 */
	private static final Pattern KIND_PATTERN =
			Pattern.compile(KIND_PROPERTY, Pattern.LITERAL);
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";

	/**
	 * Test serialization of CaveEvents.
	 *
	 * TODO: Randomly generate ID numbers, to avoid 'magic number' warnings.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testCaveSerialization() throws XMLStreamException,
			                                           SPFormatException, IOException {
		assertSerialization("First CaveEvent serialization test, reflection",
				new Cave(10, 0), Cave.class);
		assertSerialization("Second CaveEvent serialization test, reflection",
				new Cave(30, 1), Cave.class);
		assertUnwantedChild("<cave dc=\"10\"><troll /></cave>", Cave.class,
				false);
		assertMissingProperty("<cave />", Cave.class, "dc", false);
		assertMissingProperty("<cave dc=\"10\" />", Cave.class, "id", true);
		assertImageSerialization("Cave image property is preserved", new Cave(20, 2));
	}

	/**
	 * Test serialization of CityEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testCitySerialization() throws XMLStreamException,
			                                           SPFormatException, IOException {
		final Player owner = new Player(-1, "");
		for (final TownStatus status : TownStatus.values()) {
			assert status != null;
			for (final TownSize size : TownSize.values()) {
				assert size != null;
				assertSerialization(
						"First CityEvent serialization test, status " + status
								+ ", size " + size, new City(status, size, // NOPMD
										                            10, "oneCity", 0,
										                            owner), City.class);
				assertSerialization(
						"Second CityEvent serialization test, status " + status
								+ ", size " + size, new City(status, size, // NOPMD
										                            40, "twoCity", 1,
										                            owner), City.class);
			}
		}
		final City thirdCity = new City(TownStatus.Active, TownSize.Small, 30, "",
				                               3, owner);
		assertSerialization("Serialization of CityEvent without a name", thirdCity,
				City.class, new Warning(Action.Ignore));
		assertMissingProperty(createSerializedForm(thirdCity, true), City.class,
				NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(thirdCity, false), City.class,
				NAME_PROPERTY, true);
		assertMissingProperty(
				"<city status=\"active\" size=\"small\" name=\"name\" dc=\"0\" />",
				City.class, "id", true);
		assertUnwantedChild(
				"<city status=\"active\" size=\"small\" name=\"name\" dc=\"0\">"
						+ "<troll /></city>", City.class, false);
		assertMissingProperty("<city status=\"active\" size=\"small\" "
				                      + "name=\"name\" dc=\"0\" id=\"0\" />", City.class,
				"owner",
				true);
		assertImageSerialization("City image property is preserved", thirdCity);
	}

	/**
	 * Test serialization of FortificationEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testFortificationSerialization() throws XMLStreamException,
			                                                    SPFormatException,
			                                                    IOException {
		final Player owner = new Player(-1, "");
		for (final TownStatus status : TownStatus.values()) {
			assert status != null;
			for (final TownSize size : TownSize.values()) {
				assert size != null;
				assertSerialization("Fortification serialization test, status "
						                    + status + ", size " + size,
						new Fortification(// NOPMD
								                 status, size, 10, "one", 1, owner),
						Fortification.class);
				assertSerialization("Fortification serialization test, status "
						                    + status + " and size " + size,
						new Fortification(// NOPMD
								                 status, size, 40, "two", 2, owner),
						Fortification.class);
			}
		}
		final Fortification thirdFort = new Fortification(TownStatus.Active,
				                                                 TownSize.Small, 30, "",
				                                                 3, owner);
		assertSerialization(
				"Serialization of FortificationEvent without a name, reflection",
				thirdFort, Fortification.class, new Warning(Action.Ignore));
		assertMissingProperty(createSerializedForm(thirdFort, true),
				Fortification.class, NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(thirdFort, false),
				Fortification.class, NAME_PROPERTY, true);
		assertMissingProperty("<fortification status=\"active\" "
				                      + "size=\"small\" name=\"name\" dc=\"0\" />",
				Fortification.class, "id", true);
		assertUnwantedChild("<fortification status=\"active\" "
				                    + "size=\"small\" name=\"name\" dc=\"0\">"
				                    + "<troll /></fortification>", Fortification.class,
				false);
		assertMissingProperty(
				"<fortification status=\"active\" size=\"small\" name=\"name\""
						+ " dc=\"0\" id=\"0\"/>", Fortification.class, "owner",
				true);
		assertImageSerialization("Fortification image property is preserved", thirdFort);
	}

	/**
	 * Test serialization of MineralVeins.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testMineralSerialization() throws XMLStreamException,
			                                              SPFormatException,
			                                                  IOException {
		assertSerialization("First MineralEvent serialization test",
				new MineralVein("one", true, 10, 1), MineralVein.class);
		final MineralVein secondVein = new MineralVein("two", false, 35, 2);
		assertSerialization("Second MineralEvent serialization test", secondVein,
				MineralVein.class);
		final String oldKindProperty = "mineral"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of deprecated Mineral idiom", secondVein,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(secondVein, true))
								.replaceAll(
										Matcher.quoteReplacement(oldKindProperty))),
				MineralVein.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated Mineral idiom", secondVein,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(secondVein, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				MineralVein.class, oldKindProperty);
		assertUnwantedChild(
				"<mineral kind=\"gold\" exposed=\"false\" dc=\"0\">"
						+ "<troll /></mineral>", MineralVein.class, false);
		assertMissingProperty("<mineral dc=\"0\" exposed=\"false\" />",
				MineralVein.class, KIND_PROPERTY, false);
		assertMissingProperty("<mineral kind=\"gold\" exposed=\"false\" />",
				MineralVein.class, "dc", false);
		assertMissingProperty("<mineral dc=\"0\" kind=\"gold\" />",
				MineralVein.class, "exposed", false);
		assertMissingProperty(
				"<mineral kind=\"kind\" exposed=\"true\" dc=\"0\" />",
				MineralVein.class, "id", true);
		assertImageSerialization("Mineral image property is preserved", secondVein);
	}

	/**
	 * Test serialization of StoneDeposits.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testStoneSerialization() throws XMLStreamException,
			                                            SPFormatException, IOException {
		for (final StoneKind kind : StoneKind.values()) {
			assert kind != null;
			assertSerialization("First StoneDeposit test, kind: " + kind,
					new StoneDeposit(kind, 8, 1), StoneDeposit.class); // NOPMD
			assertSerialization("Second StoneDeposit test, kind: " + kind,
					new StoneDeposit(kind, 15, 2), StoneDeposit.class); // NOPMD
		}
		final StoneDeposit thirdDeposit = new StoneDeposit(StoneKind.Marble, 10, 3);
		final String oldKindProperty = "stone"; // NOPMD
		assertDeprecatedDeserialization(
				"Deserialization of deprecated stone idiom", thirdDeposit,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(thirdDeposit, true))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				StoneDeposit.class, oldKindProperty);
		assertDeprecatedDeserialization(
				"Deserialization of deprecated stone idiom", thirdDeposit,
				NullCleaner.assertNotNull(
						KIND_PATTERN.matcher(createSerializedForm(thirdDeposit, false))
								.replaceAll(Matcher.quoteReplacement(oldKindProperty))),
				StoneDeposit.class, oldKindProperty);
		assertUnwantedChild(
				"<stone kind=\"marble\" dc=\"10\"><troll /></stone>",
				StoneDeposit.class, false);
		assertMissingProperty("<stone kind=\"marble\" />", StoneDeposit.class,
				"dc", false);
		assertMissingProperty("<stone dc=\"10\" />", StoneDeposit.class,
				KIND_PROPERTY, false);
		assertMissingProperty("<stone kind=\"marble\" dc=\"0\" />",
				StoneDeposit.class, "id", true);
		assertImageSerialization("Stone image property is preserved", thirdDeposit);
	}

	/**
	 * Test serialization of TownEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testTownSerialization() throws XMLStreamException,
			                                           SPFormatException, IOException {
		final Player owner = new Player(-1, "");
		for (final TownStatus status : TownStatus.values()) {
			assert status != null;
			for (final TownSize size : TownSize.values()) {
				assert size != null;
				assertSerialization(
						"First TownEvent serialization test, reflection, status "
								+ status + " and size " + size, new Town(// NOPMD
										                                        status,
										                                        size, 10,
										                                        "one", 1,
										                                        owner),
						Town.class);
				assertSerialization(
						"Second TownEvent serialization test, reflection, status "
								+ status + " and size " + size, new Town(// NOPMD
										                                        status,
										                                        size, 40,
										                                        "two", 2,
										                                        owner),
						Town.class);
			}
		}
		final Town thirdTown = new Town(TownStatus.Active, TownSize.Small, 30, "",
				                               3, owner);
		assertSerialization(
				"Serialization of TownEvent without a name, reflection", thirdTown,
				Town.class, new Warning(Action.Ignore));
		assertMissingProperty(createSerializedForm(thirdTown, true), Town.class,
				NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(thirdTown, false), Town.class,
				NAME_PROPERTY, true);
		assertMissingProperty("<town status=\"active\" size=\"small\"/>",
				Town.class, "dc", false);
		assertMissingProperty("<town dc=\"0\" status=\"active\" />",
				Town.class, "size", false);
		assertMissingProperty("<town dc=\"0\" size=\"small\" />", Town.class,
				STATUS_PROPERTY, false);
		assertMissingProperty(
				"<town dc=\"0\" size=\"small\" status=\"active\" name=\"name\" />",
				Town.class, "id", true);
		assertUnwantedChild(
				"<town status=\"active\" size=\"small\" name=\"name\" dc=\"0\">"
						+ "<troll /></town>", Town.class, false);
		assertMissingProperty("<town status=\"active\" size=\"small\" "
				                      + "name=\"name\" dc=\"0\" id=\"0\" />", Town.class,
				"owner",
				true);
		assertImageSerialization("Town image property is preserved", thirdTown);
	}

	/**
	 * First test of serialization of BattlefieldEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testBattlefieldSerialization() throws XMLStreamException,
			                                                  SPFormatException,
			                                                  IOException {
		assertSerialization("First BattlefieldEvent serialization test",
				new Battlefield(10, 0), Battlefield.class);
		assertSerialization("Second BattlefieldEvent serialization test",
				new Battlefield(30, 1), Battlefield.class);
		assertUnwantedChild("<battlefield dc=\"10\"><troll /></battlefield>",
				Battlefield.class, false);
		assertMissingProperty("<battlefield />", Battlefield.class, "dc", false);
		assertMissingProperty("<battlefield dc=\"10\" />", Battlefield.class,
				"id", true);
		assertImageSerialization("Battlefield image property is preserved",
				new Battlefield(20, 2));
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TestEventSerialization";
	}
}
