package model.map.fixtures.towns;

import controller.map.formatexceptions.SPFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import javax.xml.stream.XMLStreamException;
import model.map.BaseTestFixtureSerialization;
import model.map.HasMutableImage;
import model.map.Player;
import model.workermgmt.RaceFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * A class to test serialization of Villages.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
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
@RunWith(Parameterized.class)
public class TestVillageSerialization extends BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String NAME_PROPERTY = "name";
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";
	/**
	 * Extracted constant.
	 */
	private static final String OWNER_PROPERTY = "owner";
	/**
	 * @param villageStatus the status to use for the village in the test
	 * @param villageRace the race to use for the village in the test
	 */
	public TestVillageSerialization(final TownStatus villageStatus, final String villageRace) {
		status = villageStatus;
		race = villageRace;
	}
	/**
	 * The status to use for the village in the test.
	 */
	private final TownStatus status;
	/**
	 * The race to use for the village in the test.
	 */
	private final String race;
	@Parameters
	public static Collection<Object[]> generateData() {
		final TownStatus[] statuses = TownStatus.values();
		final Collection<String> races = new HashSet<>(RaceFactory.getRaces());
		final Collection<Object[]> retval = new ArrayList<>(statuses.length * races.size());
		for (TownStatus status : statuses) {
			for (String race : races) {
				retval.add(new Object[]{status, race});
			}
		}
		return retval;
	}
	/**
	 * Test Village serialization.
	 *
	 * @throws SPFormatException  on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException        on I/O error creating serialized form
	 */
	@Test
	public void testVillageSerialization() throws XMLStreamException,
														  SPFormatException,
														  IOException {
		final Player owner = new Player(-1, "");
		assert status != null;
		assert race != null;
		assertSerialization("First Village serialization test, " + status,
				new Village(status, "villageOne", 1, owner, race));
		assertSerialization("2nd Village serialization test,  " + status,
				new Village(status, "villageTwo", 2, owner, race));
		final HasMutableImage thirdVillage = new Village(status, "", 3, owner, race);
		assertMissingPropertyDeserialization(
				"Village serialization with no or empty name does The Right Thing",
				thirdVillage, createSerializedForm(thirdVillage, true), NAME_PROPERTY);
		assertMissingPropertyDeserialization(
				"Village serialization with no or empty name does The Right Thing",
				thirdVillage, createSerializedForm(thirdVillage, false), NAME_PROPERTY);
		assertUnwantedChild("<village status=\"" + status + "\"><village /></village>",
				Village.class, false);
		assertMissingProperty("<village />", Village.class, STATUS_PROPERTY,
				false);
		assertMissingProperty("<village name=\"name\" status=\"" + status + "\" />",
				Village.class, "id", true);
		assertMissingProperty(
				"<village name=\"name\" status=\"" + status + "\" id=\"0\" />",
				Village.class, OWNER_PROPERTY, true);
		assertImageSerialization("Village image property is preserved", thirdVillage);
	}
}
