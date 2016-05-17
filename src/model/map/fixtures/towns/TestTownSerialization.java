package model.map.fixtures.towns;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.stream.XMLStreamException;
import model.map.BaseTestFixtureSerialization;
import model.map.HasMutableImage;
import model.map.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.Warning;

/**
 * A class to test the serialization of town fixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
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
@SuppressWarnings("ClassHasNoToStringMethod")
@RunWith(Parameterized.class)
public final class TestTownSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String NAME_PROPERTY = "name";
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";
	/**
	 * @param sz the size of the town to use for the test
	 * @param st the status of the town to use for the test
	 */
	public TestTownSerialization(final TownSize sz, final TownStatus st) {
		size = sz;
		status = st;
	}
	/**
	 * The size of the town being used for the test.
	 */
	private final TownSize size;
	/**
	 * The status of the town being used for the test.
	 */
	private final TownStatus status;
	/**
	 * @return a list of values to use for tests
	 */
	@SuppressWarnings("ObjectAllocationInLoop")
	@Parameterized.Parameters
	public static Collection<Object[]> generateData() {
		final Collection<Object[]> retval =
				new ArrayList<>(TownSize.values().length * TownStatus.values().length);
		for (final TownSize size : TownSize.values()) {
			for (final TownStatus status : TownStatus.values()) {
				retval.add(new Object[]{ size, status });
			}
		}
		return retval;
	}
	/**
	 * Test serialization of CityEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testCitySerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assert status != null;
		assert size != null;
		final Player owner = new Player(-1, "");
		assertSerialization(
				"First City serialization test, status " + status + ", size " + size,
				new City(status, size, 10, "oneCity", 0, owner));
		assertSerialization(
				"Second City serialization test, status " + status + ", size " + size,
				new City(status, size, 40, "twoCity", 1, owner));
		final HasMutableImage thirdCity = new City(status, size, 30, "", 3, owner);
		assertSerialization("Serialization of CityEvent without a name", thirdCity,
				Warning.Ignore);
		assertMissingProperty(createSerializedForm(thirdCity, true), City.class,
				NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(thirdCity, false), City.class,
				NAME_PROPERTY, true);
		assertMissingProperty("<city status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\" />", City.class, "id",
				true);
		assertUnwantedChild("<city status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\"><troll /></city>",
				City.class, false);
		assertMissingProperty("<city status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\" id=\"0\" />", City.class,
				"owner", true);
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
		assert status != null;
		assert size != null;
		final Player owner = new Player(-1, "");
		assertSerialization(
				"Fortification serialization: status " + status + ", size " + size,
				new Fortification(status, size, 10, "one", 1, owner));
		assertSerialization(
				"Fortification serialization test, status " + status + " and size " +
						size, new Fortification(status, size, 40, "two", 2, owner));
		final HasMutableImage thirdFort =
				new Fortification(status, size, 30, "", 3, owner);
		assertSerialization("Serialization of FortificationEvent without a name",
				thirdFort, Warning.Ignore);
		assertMissingProperty(createSerializedForm(thirdFort, true),
				Fortification.class, NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(thirdFort, false),
				Fortification.class, NAME_PROPERTY, true);
		assertMissingProperty("<fortification status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\" />",
				Fortification.class, "id", true);
		assertUnwantedChild("<fortification status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\"><troll /></fortification>",
				Fortification.class, false);
		assertMissingProperty("<fortification status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\"" + " dc=\"0\" id=\"0\"/>",
				Fortification.class, "owner", true);
		assertImageSerialization("Fortification image property is preserved", thirdFort);
	}

	/**
	 * Test serialization of TownEvents.
	 *
	 * @throws SPFormatException  on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testTownSerialization()
			throws XMLStreamException, SPFormatException, IOException {
		assert status != null;
		assert size != null;
		final Player owner = new Player(-1, "");
		assertSerialization(
				"First Town serialization test, status " + status + " and size " + size,
				new Town(status, size, 10, "one", 1, owner));
		assertSerialization(
				"Second Town serialization test, status " + status + " and size " + size,
				new Town(status, size, 40, "two", 2, owner));
		final HasMutableImage thirdTown = new Town(status, size, 30, "", 3, owner);
		assertSerialization("Serialization of TownEvent without a name", thirdTown,
				Warning.Ignore);
		assertMissingProperty(createSerializedForm(thirdTown, true), Town.class,
				NAME_PROPERTY, true);
		assertMissingProperty(createSerializedForm(thirdTown, false), Town.class,
				NAME_PROPERTY, true);
		assertMissingProperty("<town status=\"" + status + "\" size=\"" + size + "\"/>",
				Town.class, "dc", false);
		assertMissingProperty("<town dc=\"0\" status=\"" + status + "\" />",
				Town.class, "size", false);
		assertMissingProperty("<town dc=\"0\" size=\"" + size + "\" />", Town.class,
				STATUS_PROPERTY, false);
		assertMissingProperty("<town dc=\"0\" size=\"" + size + "\" status=\"" + status +
									"\" name=\"name\" />", Town.class, "id", true);
		assertUnwantedChild("<town status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\">" + "<troll /></town>",
				Town.class, false);
		assertMissingProperty("<town status=\"" + status + "\" size=\"" + size +
									"\" name=\"name\" dc=\"0\" id=\"0\" />", Town.class,
				"owner", true);
		assertImageSerialization("Town image property is preserved", thirdTown);
	}
}
