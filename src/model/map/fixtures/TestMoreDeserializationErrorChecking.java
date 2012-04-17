package model.map.fixtures;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.Tile;
import model.map.events.AbstractTownEvent;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A class to further test that deserialization code correctly rejects erroneous SP map XML.
 * @author Jonathan Lovelace
 *
 */
public final class TestMoreDeserializationErrorChecking {
	/**
	 * Constructor, to appease static-analysis plugins.
	 */
	public TestMoreDeserializationErrorChecking() {
		super();
		setUp();
	}
	/**
	 * Set up.
	 */
	@Before
	public void setUp() {
		reader = new SimpleXMLReader();
		warner = new Warning();
		warner.setFatal(true);
	}
	/**
	 * The reader we'll test.
	 */
	private SimpleXMLReader reader;
	/**
	 * The Warning instance we'll use.
	 */
	private Warning warner;
	/**
	 * Test that a town must have a size.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTownSize() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<town dc=\"0\" status=\"active\" />"),
				AbstractTownEvent.class, false, warner);
	}
	/**
	 * Test that a town must have a size.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTownSizeReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<town dc=\"0\" status=\"active\" />"),
				AbstractTownEvent.class, true, warner);
	}
	/**
	 * Test that a town must have a status.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTownStatus() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<town dc=\"0\" size=\"small\" />"),
				AbstractTownEvent.class, false, warner);
	}
	/**
	 * Test that a town must have a status.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTownStatusReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<town dc=\"0\" size=\"small\" />"),
				AbstractTownEvent.class, true, warner);
	}
	/**
	 * Test that a tile must have a row.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileRow() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader("<tile column=\"0\" kind=\"plains\" />"),
				Tile.class, false, warner);
	}
	/**
	 * Test that a tile must have a row.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileRowReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader("<tile column=\"0\" kind=\"plains\" />"),
				Tile.class, true, warner);
	}
	/**
	 * Test that a tile must have a column.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileColumn() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader("<tile row=\"0\" kind=\"plains\" />"),
				Tile.class, false, warner);
	}
	/**
	 * Test that a tile must have a column.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileColumnReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader("<tile row=\"0\" kind=\"plains\" />"),
				Tile.class, true, warner);
	}
	/**
	 * Test that a tile must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileKind() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader("<tile row=\"0\" column=\"0\" />"),
				Tile.class, false, warner);
	}
	/**
	 * Test that a tile must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileKindReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader("<tile row=\"0\" column=\"0\" />"),
				Tile.class, true, warner);
	}

}
