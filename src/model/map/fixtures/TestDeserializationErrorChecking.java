package model.map.fixtures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.Player;
import model.map.Tile;
import model.map.events.AbstractTownEvent;
import model.map.events.BattlefieldEvent;
import model.map.events.CaveEvent;
import model.map.events.MineralEvent;
import model.map.events.StoneEvent;

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
 * TODO: Move the helper methods into the other serialization tests' superclass and these tests into the proper subclasses.
 * @author Jonathan Lovelace
 *
 */
//ESCA-JAVA0136:
public final class TestDeserializationErrorChecking { // NOPMD
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Extracted constant.
	 */
	private static final String STATUS_PROPERTY = "status";

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
		assertMissingProperty(reader, "<village />", Village.class, STATUS_PROPERTY,
				false, false);
		assertMissingProperty(reader, "<village />", Village.class, STATUS_PROPERTY,
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
	 * Test that a town must have a DC, a size, and a status.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testTownErrors() throws XMLStreamException, SPFormatException {
		assertMissingProperty(reader, "<town />", AbstractTownEvent.class,
				"dc", false, false);
		assertMissingProperty(reader, "<town />", AbstractTownEvent.class,
				"dc", true, false);
		assertMissingProperty(reader, "<town dc=\"0\" status=\"active\" />",
				AbstractTownEvent.class, "size", false, false);
		assertMissingProperty(reader, "<town dc=\"0\" status=\"active\" />",
				AbstractTownEvent.class, "size", true, false);
		assertMissingProperty(reader, "<town dc=\"0\" size=\"small\" />",
				AbstractTownEvent.class, STATUS_PROPERTY, false, false);
		assertMissingProperty(reader, "<town dc=\"0\" size=\"small\" />",
				AbstractTownEvent.class, STATUS_PROPERTY, true, false);
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
		assertMissingProperty(reader, "<centaur />", Centaur.class, KIND_PROPERTY,
				false, false);
		assertMissingProperty(reader, "<centaur />", Centaur.class, KIND_PROPERTY,
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
	
	/**
	 * Test that a tile must have a row, a column, and a kind, and objects to
	 * non-fixture children.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testTileErrors() throws XMLStreamException, SPFormatException {
		assertMissingProperty(reader, "<tile column=\"0\" kind=\"plains\" />",
				Tile.class, "row", false, false);
		assertMissingProperty(reader, "<tile column=\"0\" kind=\"plains\" />",
				Tile.class, "row", true, false);
		assertMissingProperty(reader, "<tile row=\"0\" kind=\"plains\" />",
				Tile.class, "column", false, false);
		assertMissingProperty(reader, "<tile row=\"0\" kind=\"plains\" />",
				Tile.class, "column", true, false);
		assertMissingProperty(reader, "<tile row=\"0\" column=\"0\" />",
				Tile.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<tile row=\"0\" column=\"0\" />",
				Tile.class, KIND_PROPERTY, true, false);
		assertUnwantedChild(
				reader,
				"<tile row=\"0\" column=\"0\" kind=\"plains\"><tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>",
				Tile.class, false, false);
		assertUnwantedChild(
				reader,
				"<tile row=\"0\" column=\"0\" kind=\"plains\"><tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>",
				Tile.class, true, false);
	}
	/**
	 * Test that a Minotaur can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testMinotaurChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<minotaur><troll /></minotaur>",
				Minotaur.class, false, false);
		assertUnwantedChild(reader, "<minotaur><troll /></minotaur>",
				Minotaur.class, true, false);
	}
	/**
	 * Test that a Mountain can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testMountainChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<mountain><troll /></mountain>",
				Mountain.class, false, false);
		assertUnwantedChild(reader, "<mountain><troll /></mountain>",
				Mountain.class, true, false);
	}
	/**
	 * Test that a Mine can't have any children and must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testMineErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<mine><troll /></mine>",
				Mine.class, false, false);
		assertUnwantedChild(reader, "<mine><troll /></mine>",
				Mine.class, true, false);
		assertMissingProperty(reader, "<mine status=\"active\"/>",
				Mine.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<mine status=\"active\"/>",
				Mine.class, KIND_PROPERTY, true, false);
		assertMissingProperty(reader, "<mine kind=\"gold\"/>",
				Mine.class, STATUS_PROPERTY, false, false);
		assertMissingProperty(reader, "<mine kind=\"gold\"/>",
				Mine.class, STATUS_PROPERTY, true, false);
	}
	/**
	 * Test that a Meadow can't have any children and must have 'kind' and 'cultivated' properties.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testMeadowErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<meadow><troll /></meadow>",
				Meadow.class, false, false);
		assertUnwantedChild(reader, "<meadow><troll /></meadow>",
				Meadow.class, true, false);
		assertMissingProperty(reader, "<meadow cultivated=\"false\" />",
				Meadow.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<meadow cultivated=\"false\" />",
				Meadow.class, KIND_PROPERTY, true, false);
		assertMissingProperty(reader, "<meadow kind=\"flax\" />",
				Meadow.class, "cultivated", false, false);
		assertMissingProperty(reader, "<meadow kind=\"flax\" />",
				Meadow.class, "cultivated", true, false);
	}
	/**
	 * Test that an Animal can't have any children and must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testAnimalErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<animal><troll /></animal>",
				Animal.class, false, false);
		assertUnwantedChild(reader, "<animal><troll /></animal>",
				Animal.class, true, false);
		assertMissingProperty(reader, "<animal />",
				Animal.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<animal />",
				Animal.class, KIND_PROPERTY, true, false);
	}
	/**
	 * Test that a Battlefield can't have any children and must have a DC.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testBattlefieldErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<battlefield><troll /></battlefield>",
				BattlefieldEvent.class, false, false);
		assertUnwantedChild(reader, "<battlefield><troll /></battlefield>",
				BattlefieldEvent.class, true, false);
		assertMissingProperty(reader, "<battlefield />",
				BattlefieldEvent.class, "dc", false, false);
		assertMissingProperty(reader, "<battlefield />",
				BattlefieldEvent.class, "dc", true, false);
	}
	
	/**
	 * Test that a Cache can't have any children and must have 'kind' and
	 * 'contents' properties.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testCacheChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<cache><troll /></cache>",
				CacheFixture.class, false, false);
		assertUnwantedChild(reader, "<cache><troll /></cache>",
				CacheFixture.class, true, false);
		assertMissingProperty(reader, "<cache contents=\"contents\" />",
				CacheFixture.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<cache contents=\"contents\" />",
				CacheFixture.class, KIND_PROPERTY, true, false);
		assertMissingProperty(reader, "<cache kind=\"kind\" />",
				CacheFixture.class, "contents", false, false);
		assertMissingProperty(reader, "<cache kind=\"kind\" />",
				CacheFixture.class, "contents", true, false);
	}
	/**
	 * Test that a text node shouldn't have child nodes.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testTextChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<text turn=\"1\"><troll /></text>",
				TextFixture.class, false, false);
		assertUnwantedChild(reader, "<text turn=\"1\"><troll /></text>",
				TextFixture.class, true, false);
	}
	/**
	 * Test that a StoneEvent doesn't have any children and must have a DC and a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testStoneErrors() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, 
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>",
				StoneEvent.class, false, false);
		assertUnwantedChild(reader, 
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>",
				StoneEvent.class, true, false);
		assertMissingProperty(reader, "<stone kind=\"stone\" />",
				StoneEvent.class, "dc", false, false);
		assertMissingProperty(reader, "<stone kind=\"stone\" />",
				StoneEvent.class, "dc", true, false);
		assertMissingProperty(reader, "<stone dc=\"10\" />",
				StoneEvent.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<stone dc=\"10\" />",
				StoneEvent.class, KIND_PROPERTY, true, false);
	}
	/**
	 * Test that a Sphinx doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testSphinxChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, 
				"<sphinx><troll /></sphinx>",
				Sphinx.class, false, false);
		assertUnwantedChild(reader, 
				"<sphinx><troll /></sphinx>",
				Sphinx.class, true, false);
	}
	/**
	 * Test that a Simurgh doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testSimurghChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, 
				"<simurgh><troll /></simurgh>",
				Simurgh.class, false, false);
		assertUnwantedChild(reader, 
				"<simurgh><troll /></simurgh>",
				Simurgh.class, true, false);
	}
	/**
	 * Test that a Shrub can't have any children and must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testShrubChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<shrub><troll /></shrub>",
				Shrub.class, false, false);
		assertUnwantedChild(reader, "<shrub><troll /></shrub>",
				Shrub.class, true, false);
		assertMissingProperty(reader, "<shrub />", Shrub.class, KIND_PROPERTY, false, false);
		assertMissingProperty(reader, "<shrub />", Shrub.class, KIND_PROPERTY, true, false);
	}
	/**
	 * Test that a Sandbar can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testSandbarChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<sandbar><troll /></sandbar>",
				Sandbar.class, false, false);
		assertUnwantedChild(reader, "<sandbar><troll /></sandbar>",
				Sandbar.class, true, false);
	}
	/**
	 * Test that a River can't have any children and must have a direction.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testRiverChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<tile row=\"1\" column=\"1\" kind=\"plains\"><lake><troll /></lake></tile>",
				Tile.class, false, false);
		assertUnwantedChild(reader, "<tile row=\"1\" column=\"1\" kind=\"plains\"><lake><troll /></lake></tile>",
				Tile.class, true, false);
		assertMissingProperty(reader, "<tile row=\"1\" column=\"\" kind=\"plains\"><river /></tile>",
				Tile.class, "direction", false, false);
		assertMissingProperty(reader, "<tile row=\"1\" column=\"\" kind=\"plains\"><river /></tile>",
				Tile.class, "direction", true, false);
	}
	/**
	 * Test that a Player can't have any children and must have a number and a name.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testPlayerChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<player><troll /></player>",
				Player.class, false, false);
		assertUnwantedChild(reader, "<player><troll /></player>",
				Player.class, true, false);
		assertMissingProperty(reader, "<player code_name=\"one\" />",
				Player.class, "number", false, false);
		assertMissingProperty(reader, "<player code_name=\"one\" />",
				Player.class, "number", true, false);
		assertMissingProperty(reader, "<player number=\"1\" />",
				Player.class, "code_name", false, false);
		assertMissingProperty(reader, "<player number=\"1\" />",
				Player.class, "code_name", true, false);
	}
	/**
	 * Test that a Phoenix can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testPhoenixChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<phoenix><troll /></phoenix>",
				Phoenix.class, false, false);
		assertUnwantedChild(reader, "<phoenix><troll /></phoenix>",
				Phoenix.class, true, false);
	}
	/**
	 * Test that an Ogre can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testOgreChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<ogre><troll /></ogre>",
				Ogre.class, false, false);
		assertUnwantedChild(reader, "<ogre><troll /></ogre>",
				Ogre.class, true, false);
	}
	/**
	 * Test that an Oasis can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test
	public void testOasisChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<oasis><troll /></oasis>",
				Oasis.class, false, false);
		assertUnwantedChild(reader, "<oasis><troll /></oasis>",
				Oasis.class, true, false);
	}
	
	/**
	 * Test that a MineralEvent can't have any children and must have 'kind',
	 * 'dc', and 'exposed' properties.
	 * 
	 * @throws SPFormatException
	 *             always
	 * @throws XMLStreamException
	 *             never
	 */
	@Test
	public void testMineralChild() throws XMLStreamException, SPFormatException {
		assertUnwantedChild(reader, "<mineral><troll /></mineral>",
				MineralEvent.class, false, false);
		assertUnwantedChild(reader, "<mineral><troll /></mineral>",
				MineralEvent.class, true, false);
		assertMissingProperty(reader, 
				"<mineral dc=\"0\" exposed=\"false\" />", MineralEvent.class,
				KIND_PROPERTY, false, false);
		assertMissingProperty(reader, 
				"<mineral dc=\"0\" exposed=\"false\" />", MineralEvent.class,
				KIND_PROPERTY, true, false);
		assertMissingProperty(reader, 
				"<mineral kind=\"gold\" exposed=\"false\" />", MineralEvent.class,
				"dc", false, false);
		assertMissingProperty(reader, 
				"<mineral kind=\"gold\" exposed=\"false\" />", MineralEvent.class,
				"dc", true, false);
		assertMissingProperty(reader, 
				"<mineral dc=\"0\" kind=\"gold\" />", MineralEvent.class,
				"exposed", false, false);
		assertMissingProperty(reader, 
				"<mineral dc=\"0\" kind=\"gold\" />", MineralEvent.class,
				"exposed", true, false);
	}
}
