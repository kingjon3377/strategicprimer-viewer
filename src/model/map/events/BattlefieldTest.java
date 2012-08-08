package model.map.events;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;

import org.junit.Test;

import controller.map.SPFormatException;

/**
 * A class to hold Battlefield tests, since the single-method version takes too
 * long to run.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class BattlefieldTest extends BaseTestFixtureSerialization {
	/**
	 * First test of serialization of BattlefieldEvents.
	 * 
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void one() throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("First BattlefieldEvent serialization test",
				new BattlefieldEvent(10, 0), BattlefieldEvent.class);
	}

	/**
	 * Second test of serialization of BattlefieldEvents.
	 * 
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void two() throws XMLStreamException, SPFormatException, IOException {
		assertSerialization("Second BattlefieldEvent serialization test",
				new BattlefieldEvent(30, 1), BattlefieldEvent.class);
	}

	/**
	 * Test of error-checking in serialization of BattlefieldEvents.
	 * 
	 * @throws SPFormatException on SP format problems
	 * @throws XMLStreamException on XML reading problems
	 */
	@Test
	public void three() throws XMLStreamException, SPFormatException {
		assertUnwantedChild("<battlefield dc=\"10\"><troll /></battlefield>",
				BattlefieldEvent.class, false);
		assertMissingProperty("<battlefield />", BattlefieldEvent.class, "dc",
				false);
		assertMissingProperty("<battlefield dc=\"10\" />",
				BattlefieldEvent.class, "id", true);
	}
}
