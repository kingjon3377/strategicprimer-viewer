package model.map.fixtures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.events.AbstractTownEvent;
import model.map.events.CaveEvent;

import org.junit.Before;
import org.junit.Test;

import util.FatalWarning;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.simplexml.SimpleXMLReader;
/**
 * A class to test that deserialization code correctly rejects erroneous SP map XML.
 * @author Jonathan Lovelace
 *
 */
//ESCA-JAVA0136:
public final class TestDeserializationErrorChecking { // NOPMD
	/**
	 * Constructor, to appease static-analysis plugins.
	 */
	public TestDeserializationErrorChecking() {
		super();
		setUp();
	}
	/**
	 * Set up.
	 */
	@Before
	public void setUp() {
		reader = new SimpleXMLReader();
	}
	/**
	 * The reader we'll test.
	 */
	private SimpleXMLReader reader;
	
	/**
	 * Assert that reading the given XML will produce an UnwantedChildException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but fail with warnings made fatal.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param reflection
	 *            whether to use the reflection version or not
	 * @param warning
	 *            whether this is supposed to be a warning only
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public static void assertUnwantedChild(final SimpleXMLReader reader,
			final String xml, final Class<?> desideratum,
			final boolean reflection, final boolean warning)
			throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(new StringReader(xml), desideratum, reflection,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(new StringReader(xml), desideratum, reflection,
						new Warning(Warning.Action.Die));
				fail("We were expecting an UnwantedChildException");
			} catch (FatalWarning except) {
				assertTrue("Unwanted child",
						except.getCause() instanceof UnwantedChildException);
			}
		} else {
			try {
				reader.readXML(new StringReader(xml), desideratum, reflection,
						new Warning(Warning.Action.Ignore));
				fail("We were expecting an UnwantedChildException");
			} catch (UnwantedChildException except) {
				assertNotNull("Dummy check", except);
			}
		}
	}

	/**
	 * Assert that reading the given XML will give a MissingPropertyException.
	 * If it's only supposed to be a warning, assert that it'll pass with
	 * warnings disabled but object with them made fatal.
	 * 
	 * @param reader
	 *            the reader to do the reading
	 * @param xml
	 *            the XML to read
	 * @param desideratum
	 *            the class it would produce if it weren't erroneous
	 * @param property
	 *            the missing property
	 * @param reflection
	 *            whether to use the reflection version of the reader or not
	 * @param warning
	 *            whether this is supposed to be only a warning
	 * @throws SPFormatException
	 *             on unexpected SP format error
	 * @throws XMLStreamException
	 *             on XML format error
	 */
	public static void assertMissingProperty(final SimpleXMLReader reader,
			final String xml, final Class<?> desideratum,
			final String property, final boolean reflection,
			final boolean warning) throws XMLStreamException, SPFormatException {
		if (warning) {
			reader.readXML(new StringReader(xml), desideratum, reflection,
					new Warning(Warning.Action.Ignore));
			try {
				reader.readXML(new StringReader(xml), desideratum, reflection,
						new Warning(Warning.Action.Die));
				fail("We were expecting a MissingParameterException");
			} catch (FatalWarning except) {
				assertTrue("Missing property",
						except.getCause() instanceof MissingParameterException);
				assertEquals(
						"The missing property should be the one we're expecting",
						property, ((MissingParameterException) except
								.getCause()).getParam());
			}
		} else {
			try {
				reader.readXML(new StringReader(xml), desideratum, reflection,
						new Warning(Warning.Action.Ignore));
			} catch (MissingParameterException except) {
				assertEquals(
						"Missing property should be the one we're expecting",
						property, except.getParam());
			}
		}
	}

	/**
	 * Test that a Village shouldn't have children and must have a 'status'
	 * property.
	 * 
	 * @throws SPFormatException
	 *             never
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testVillageErrors() throws XMLStreamException,
			SPFormatException {
		assertUnwantedChild(reader, "<village><village /></village>",
				Village.class, false, false);
		assertUnwantedChild(reader, "<village><village /></village>",
				Village.class, true, false);
		assertMissingProperty(reader, "<village />", Village.class, "status",
				false, false);
		assertMissingProperty(reader, "<village />", Village.class, "status",
				true, false);
	}

	/**
	 * Test that a Unit mustn't have children.
	 * 
	 * @throws SPFormatException
	 *             never
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testUnitChildren() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<unit><unit /></unit>", Unit.class, false,
				false);
		assertUnwantedChild(reader, "<unit><unit /></unit>", Unit.class, true,
				false);
	}

	/**
	 * Test that a Troll shouldn't have children.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testTrollChildren() throws XMLStreamException,
			SPFormatException {
		assertUnwantedChild(reader, "<troll><troll /></troll>", Troll.class,
				false, false);
		assertUnwantedChild(reader, "<troll><troll /></troll>", Troll.class,
				true, false);
	}

	/**
	 * Test that a town must have a DC.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testTownDC() throws XMLStreamException, SPFormatException {
		assertMissingProperty(reader, "<town />", AbstractTownEvent.class,
				"dc", false, false);
		assertMissingProperty(reader, "<town />", AbstractTownEvent.class,
				"dc", true, false);
	}

	/**
	 * Test that a Cave can't have any children and must have a DC.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testCaveErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<cave><troll /></cave>", CaveEvent.class,
				false, false);
		assertUnwantedChild(reader, "<cave><troll /></cave>", CaveEvent.class,
				true, false);
		assertMissingProperty(reader, "<cave />", CaveEvent.class, "dc", false,
				false);
		assertMissingProperty(reader, "<cave />", CaveEvent.class, "dc", true,
				false);
	}

	/**
	 * Test that a Centaur can't have any children and must have a kind.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testCentaurErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<centaur><troll /></centaur>",
				Centaur.class, false, false);
		assertUnwantedChild(reader, "<centaur><troll /></centaur>",
				Centaur.class, true, false);
		assertMissingProperty(reader, "<centaur />", Centaur.class, "kind",
				false, false);
		assertMissingProperty(reader, "<centaur />", Centaur.class, "kind",
				true, false);
	}
	/**
	 * Test that a Djinn can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testDjinnChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<djinn><troll /></djinn>", Djinn.class,
				false, false);
		assertUnwantedChild(reader, "<djinn><troll /></djinn>", Djinn.class,
				true, false);
	}
}
