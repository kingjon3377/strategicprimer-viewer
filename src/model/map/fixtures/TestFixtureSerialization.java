package model.map.fixtures;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.XMLWritable;

import org.junit.Before;
import org.junit.Test;

import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;
/**
 * A class to test serialization of TileFixtures.
 * @author Jonathan Lovelace
 */
public final class TestFixtureSerialization {
	/**
	 * Constructor.
	 */
	public TestFixtureSerialization() {
		setUp();
	}
	/**
	 * Set-up method.
	 */
	@Before
	public void setUp() {
		reader = new SimpleXMLReader();
	}
	/**
	 * The XML reader we'll use to test.
	 */
	private SimpleXMLReader reader;
	/**
	 * Test the serialization of Animal.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testAnimalSerialization() throws XMLStreamException, SPFormatException {
		final Animal one = new Animal("animalOne", false, false);
		assertEquals("First test of Animal serialization, reflection", one,
				helpSerialization(one, Animal.class, true));
		assertEquals("First test of Animal serialization, non-reflection", one,
				helpSerialization(one, Animal.class, false));
		final Animal two = new Animal("animalTwo", false, true);
		assertEquals("Second test of Animal serialization, reflection", two,
				helpSerialization(two, Animal.class, true));
		assertEquals("Second test of Animal serialization, non-reflection", two,
				helpSerialization(two, Animal.class, false));
		final Animal three = new Animal("animalThree", true, false);
		assertEquals("Third test of Animal serialization, reflection", three,
				helpSerialization(three, Animal.class, true));
		assertEquals("Third test of Animal serialization, non-reflection", three,
				helpSerialization(three, Animal.class, false));
		final Animal four = new Animal("animalFour", true, true);
		assertEquals("Fourth test of Animal serialization, reflection", four,
				helpSerialization(four, Animal.class, true));
		assertEquals("Fourth test of Animal serialization, non-reflection", four,
				helpSerialization(four, Animal.class, false));
	}
	/**
	 * Test the serialization of CacheFixture.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testCacheSerialization() throws XMLStreamException, SPFormatException {
		final CacheFixture one = new CacheFixture("kindOne", "contentsOne");
		assertEquals("First test of Cache serialization, reflection", one,
				helpSerialization(one, CacheFixture.class, true));
		assertEquals("First test of Cache serialization, non-reflection", one,
				helpSerialization(one, CacheFixture.class, false));
		final CacheFixture two = new CacheFixture("kindTwo", "contentsTwo");
		assertEquals("Second test of Cache serialization, reflection", two,
				helpSerialization(two, CacheFixture.class, true));
		assertEquals("Second test of Cache serialization, non-reflection", two,
				helpSerialization(two, CacheFixture.class, false));
	}
	/**
	 * Test the serialization of Centaurs.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testCentaurSerialization() throws XMLStreamException, SPFormatException {
		final Centaur one = new Centaur("one");
		assertEquals("First test of Centaur serialization, reflection", one,
				helpSerialization(one, Centaur.class, true));
		assertEquals("First test of Centaur serialization, non-reflection", one,
				helpSerialization(one, Centaur.class, false));
		final Centaur two = new Centaur("two");
		assertEquals("Second test of Centaur serialization, reflection", two,
				helpSerialization(two, Centaur.class, true));
		assertEquals("Second test of Centaur serialization, non-reflection", two,
				helpSerialization(two, Centaur.class, false));
	}
	/**
	 * Test the serialization of Dragons.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testDragonSerialization() throws XMLStreamException, SPFormatException {
		final Dragon one = new Dragon("");
		assertEquals("First test of Dragon serialization, reflection", one,
				helpSerialization(one, Dragon.class, true));
		assertEquals("First test of Dragon serialization, non-reflection", one,
				helpSerialization(one, Dragon.class, false));
		final Dragon two = new Dragon("two");
		assertEquals("Second test of Dragon serialization, reflection", two,
				helpSerialization(two, Dragon.class, true));
		assertEquals("Second test of Dragon serialization, non-reflection", two,
				helpSerialization(two, Dragon.class, false));
	}
	/**
	 * Test the serialization of Fairies.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testFairySerialization() throws XMLStreamException, SPFormatException {
		final Fairy one = new Fairy("one");
		assertEquals("First test of Fairy serialization, reflection", one,
				helpSerialization(one, Fairy.class, true));
		assertEquals("First test of Fairy serialization, non-reflection", one,
				helpSerialization(one, Fairy.class, false));
		final Fairy two = new Fairy("two");
		assertEquals("Second test of Fairy serialization, reflection", two,
				helpSerialization(two, Fairy.class, true));
		assertEquals("Second test of Fairy serialization, non-reflection", two,
				helpSerialization(two, Fairy.class, false));
	}
	/**
	 * Test the serialization of Forests.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testForestSerialization() throws XMLStreamException, SPFormatException {
		final Forest one = new Forest("one", false);
		assertEquals("First test of Forest serialization, reflection", one,
				helpSerialization(one, Forest.class, true));
		assertEquals("First test of Forest serialization, non-reflection", one,
				helpSerialization(one, Forest.class, false));
		final Forest two = new Forest("two", true);
		assertEquals("Second test of Forest serialization, reflection", two,
				helpSerialization(two, Forest.class, true));
		assertEquals("Second test of Forest serialization, non-reflection", two,
				helpSerialization(two, Forest.class, false));
	}
	/**
	 * Test the serialization of simple (no-parameter) fixtures.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testSimpleSerialization() throws XMLStreamException, SPFormatException {
		final Djinn djinn = new Djinn();
		assertEquals("Test of Djinn serialization, reflection", djinn,
				helpSerialization(djinn, Djinn.class, true));
		assertEquals("Test of Djinn serialization, non-reflection", djinn,
				helpSerialization(djinn, Djinn.class, false));
		final Griffin griffin = new Griffin();
		assertEquals("Test of Griffin serialization, reflection", griffin, 
				helpSerialization(griffin, Griffin.class, true));
		assertEquals("Test of Griffin serialization, non-reflection", griffin, 
				helpSerialization(griffin, Griffin.class, false));
	}
	/**
	 * A helper method to simplify test boiler plate code.
	 * @param <T> The type of the object
	 * @param orig the object to serialize
	 * @param type the type of the object
	 * @param reflection whether to use reflection
	 * @return the result of deserializing the serialized form 
	 * @throws SPFormatException on SP XML problem
	 * @throws XMLStreamException on XML reading problem
	 */
	private <T extends XMLWritable> T helpSerialization(final T orig,
			final Class<T> type, final boolean reflection)
			throws XMLStreamException, SPFormatException {
		return reader.readXML(new StringReader(orig.toXML()), type, reflection);
	}
}
