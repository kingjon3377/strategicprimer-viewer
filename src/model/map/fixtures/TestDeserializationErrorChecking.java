package model.map.fixtures;



import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.Tile;

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
}
