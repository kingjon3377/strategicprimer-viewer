package model.map.fixtures;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.Player;
import model.map.Tile;
import model.map.events.StoneEvent;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A class to still further check that deserialization code correctly rejects
 * erroneous SP map XML.
 * 
 * @author Jonathan Lovelace
 * 
 */
//ESCA-JAVA0136:
public final class TestYetMoreDeserializationErrorChecking { //NOPMD
	/**
	 * Constructor, to appease static-analysis plugins.
	 */
	public TestYetMoreDeserializationErrorChecking() {
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
	 * Test that a text node shouldn't have child nodes.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTextChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<text turn=\"1\"><troll /></text>"),
				TextFixture.class, false, warner);
	}
	/**
	 * Test that a text node shouldn't have child nodes.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testTextChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<text turn=\"1\"><troll /></text>"),
				TextFixture.class, true, warner);
	}
	/**
	 * Test that a StoneEvent doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testStoneChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>"),
				StoneEvent.class, false, warner);
	}
	/**
	 * Test that a StoneEvent doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testStoneChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<stone kind=\"stone\" dc=\"10\"><troll /></stone>"),
				StoneEvent.class, true, warner);
	}
	/**
	 * Test that a StoneEvent must have a DC.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testStoneDC() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<stone kind=\"stone\" />"),
				StoneEvent.class, false, warner);
	}
	/**
	 * Test that a StoneEvent must have a DC.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testStoneDCReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<stone kind=\"stone\" />"),
				StoneEvent.class, true, warner);
	}
	/**
	 * Test that a StoneEvent must have a DC.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testStoneKind() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<stone dc=\"10\" />"),
				StoneEvent.class, false, warner);
	}
	/**
	 * Test that a StoneEvent must have a DC.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testStoneKindReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<stone dc=\"10\" />"),
				StoneEvent.class, true, warner);
	}
	/**
	 * Test that a Sphinx doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSphinxChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<sphinx><troll /></sphinx>"),
				Sphinx.class, false, warner);
	}
	/**
	 * Test that a Sphinx doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSphinxChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<sphinx><troll /></sphinx>"),
				Sphinx.class, true, warner);
	}
	/**
	 * Test that a Simurgh doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSimurghChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<simurgh><troll /></simurgh>"),
				Simurgh.class, false, warner);
	}
	/**
	 * Test that a Simurgh doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSimurghChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<simurgh><troll /></simurgh>"),
				Simurgh.class, true, warner);
	}
	/**
	 * Test that a Shrub can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testShrubChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<shrub><troll /></shrub>"),
				Shrub.class, false, warner);
	}
	/**
	 * Test that a Shrub can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testShrubChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<shrub><troll /></shrub>"),
				Shrub.class, true, warner);
	}
	/**
	 * Test that a Shrub must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testShrubKind() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<shrub />"), Shrub.class, false, warner);
	}
	/**
	 * Test that a Shrub must have a kind.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testShrubKindReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<shrub />"), Shrub.class, false, warner);
	}
	/**
	 * Test that a Sandbar can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSandbarChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<sandbar><troll /></sandbar>"),
				Sandbar.class, false, warner);
	}
	/**
	 * Test that a Sandbar can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSandbarChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<sandbar><troll /></sandbar>"),
				Sandbar.class, true, warner);
	}
	/**
	 * Test that a River can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testRiverChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<tile row=\"1\" column=\"1\" kind=\"plains\"><lake><troll /></lake></tile>"),
				Tile.class, false, warner);
	}
	/**
	 * Test that a River can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testRiverChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<tile row=\"1\" column=\"1\" kind=\"plains\"><lake><troll /></lake></tile>"),
				Tile.class, true, warner);
	}
	/**
	 * Test that a river must have a direction.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testRiverDirection() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader(
						"<tile row=\"1\" column=\"\" kind=\"plains\"><river /></tile>"),
				Tile.class, false, warner);
	}
	/**
	 * Test that a river must have a direction.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testRiverDirectionReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(
				new StringReader(
						"<tile row=\"1\" column=\"\" kind=\"plains\"><river /></tile>"),
				Tile.class, true, warner);
	}
	/**
	 * Test that a Player can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testPlayerChild() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<player><troll /></player>"),
				Player.class, false, warner);
	}
	/**
	 * Test that a Player can't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testPlayerChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<player><troll /></player>"),
				Player.class, true, warner);
	}
	/**
	 * Test that a Player must have a number.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testPlayerNumber() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<player code_name=\"one\" />"),
				Player.class, false, warner);
	}
	/**
	 * Test that a Player must have a number.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testPlayerNumberReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<player code_name=\"one\" />"),
				Player.class, true, warner);
	}
	/**
	 * Test that a Player must have a code name.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testPlayerName() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<player number=\"1\" />"),
				Player.class, false, warner);
	}
	/**
	 * Test that a Player must have a code name.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testPlayerNameReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<player number=\"1\" />"),
				Player.class, true, warner);
	}
}
