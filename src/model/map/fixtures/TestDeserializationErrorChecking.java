package model.map.fixtures;



import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.Tile;
import model.map.events.AbstractTownEvent;
import model.map.events.BattlefieldEvent;
import model.map.events.CaveEvent;
import model.map.events.MineralEvent;
import model.map.events.StoneEvent;

import org.junit.Before;
import org.junit.Test;

import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;
/**
 * A class to test that deserialization code correctly rejects erroneous SP map XML.
 * TODO: Move the helper methods into the other serialization tests' superclass and these tests into the proper subclasses.
 * @author Jonathan Lovelace
 *
 */
//ESCA-JAVA0136:
public final class TestDeserializationErrorChecking extends // NOPMD
		BaseTestFixtureSerialization { // NOPMD
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
