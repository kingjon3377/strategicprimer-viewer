package model.map.fixtures;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

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
public final class TestYetMoreDeserializationErrorChecking {
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
	 * Test that a StoneEvent doesn't have any children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testSphinxChildReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader(
				"<sphinx><troll /></sphinx>"),
				Sphinx.class, true, warner);
	}
}
