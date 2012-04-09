package model.map.fixtures;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A class to test that deserialization code correctly rejects erroneous SP map XML.
 * @author Jonathan Lovelace
 *
 */
public final class TestDeserializationErrorChecking {
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
	 * Test that a Village shouldn't have children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testVillageChildren() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<village><village /></village>"),
				Village.class, false, warner);
	}
	/**
	 * Test that a Village shouldn't have children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testVillageChildrenReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<village><village /></village>"),
				Village.class, true, warner);
	}
	/**
	 * Test that a Village must have a status property.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testVillageStatus() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<village />"), Village.class, false, warner);
	}
	/**
	 * Test that a Village must have a status property.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testVillageStatusReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<village />"), Village.class, true, warner);
	}
	/**
	 * Test that a Unit mustn't have children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testUnitChildren() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<unit><unit /></unit>"), Unit.class, false, warner);
	}
	/**
	 * Test that a Unit mustn't have children.
	 * @throws SPFormatException always
	 * @throws XMLStreamException never
	 */
	@Test(expected = SPFormatException.class)
	public void testUnitChildrenReflection() throws XMLStreamException, SPFormatException {
		reader.readXML(new StringReader("<unit><unit /></unit>"), Unit.class, true, warner);
	}
}
