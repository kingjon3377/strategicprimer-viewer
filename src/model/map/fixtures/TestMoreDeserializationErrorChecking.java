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
//ESCA-JAVA0136:
public final class TestMoreDeserializationErrorChecking { // NOPMD
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
	/**
	 * Test that a tile objects to non-fixture child.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileChild() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader(
						"<tile row=\"0\" column=\"0\" kind=\"plains\"><tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>"),
				Tile.class, false, warner);
	}
	/**
	 * Test that a tile objects to non-fixture child.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTileChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader(
						"<tile row=\"0\" column=\"0\" kind=\"plains\"><tile row=\"1\" column=\"1\" kind=\"plains\" /></tile>"),
				Tile.class, true, warner);
	}
	/**
	 * Test that a Minotaur can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMinotaurChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<minotaur><troll /></minotaur>"),
				Minotaur.class, false, warner);
	}
	/**
	 * Test that a Minotaur can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMinotaurChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<minotaur><troll /></minotaur>"),
				Minotaur.class, true, warner);
	}
	/**
	 * Test that a Mountain can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMountainChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mountain><troll /></mountain>"),
				Ogre.class, false, warner);
	}
	/**
	 * Test that a Mountain can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMountainChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mountain><troll /></mountain>"),
				Mountain.class, true, warner);
	}
	/**
	 * Test that a Mine can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMineChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mine><troll /></mine>"),
				Mine.class, false, warner);
	}
	/**
	 * Test that a Mine can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMineChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mine><troll /></mine>"),
				Mine.class, true, warner);
	}
	/**
	 * Test that a Mine must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMineKind() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mine status=\"active\"/>"),
				Mine.class, false, warner);
	}
	/**
	 * Test that a Mine must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMineKindReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mine status=\"active\" />"),
				Mine.class, true, warner);
	}
	/**
	 * Test that a Mine must have a status.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMineStatus() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mine kind=\"gold\"/>"),
				Mine.class, false, warner);
	}
	/**
	 * Test that a Mine must have a status.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMineStatusReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<mine kind=\"gold\" />"),
				Mine.class, true, warner);
	}
	/**
	 * Test that a Meadow can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMeadowChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<meadow><troll /></meadow>"),
				Mine.class, false, warner);
	}
	/**
	 * Test that a Meadow can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMeadowChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<meadow><troll /></meadow>"),
				Meadow.class, true, warner);
	}
	/**
	 * Test that a Meadow must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMeadowKind() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<meadow cultivated=\"false\" />"),
				Meadow.class, false, warner);
	}
	/**
	 * Test that a Meadow must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMeadowKindReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<meadow cultivated=\"false\" />"),
				Meadow.class, true, warner);
	}
	/**
	 * Test that a Meadow must have a 'cultivated' property.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMeadowCultivated() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<meadow kind=\"flax\" />"),
				Meadow.class, false, warner);
	}
	/**
	 * Test that a Meadow must have a 'cultivated' property.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testMeadowCultivatedReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<meadow kind=\"flax\" />"),
				Meadow.class, true, warner);
	}
	/**
	 * Test that an Animal can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testAnimalChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<animal><troll /></animal>"),
				Animal.class, false, warner);
	}
	/**
	 * Test that an Animal can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testAnimalChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<animal><troll /></animal>"),
				Animal.class, true, warner);
	}
}
