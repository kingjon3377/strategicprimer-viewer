package model.map.fixtures;

import static org.junit.Assert.assertEquals;

import javax.xml.stream.XMLStreamException;

import model.map.Player;

import org.junit.Before;
import org.junit.Test;

import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * A class to test serialization of TileFixtures.
 * 
 * @author Jonathan Lovelace
 */
public final class TestFixtureSerialization extends BaseTestFixtureSerialization {
	/**
	 * Constructor.
	 */
	public TestFixtureSerialization() {
		super();
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
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testAnimalSerialization() throws XMLStreamException,
			SPFormatException {
		final Animal one = new Animal("animalOne", false, false);
		assertEquals("First test of Animal serialization, reflection", one,
				helpSerialization(reader, one, Animal.class, true));
		assertEquals("First test of Animal serialization, non-reflection", one,
				helpSerialization(reader, one, Animal.class, false));
		final Animal two = new Animal("animalTwo", false, true);
		assertEquals("Second test of Animal serialization, reflection", two,
				helpSerialization(reader, two, Animal.class, true));
		assertEquals("Second test of Animal serialization, non-reflection",
				two, helpSerialization(reader, two, Animal.class, false));
		final Animal three = new Animal("animalThree", true, false);
		assertEquals("Third test of Animal serialization, reflection", three,
				helpSerialization(reader, three, Animal.class, true));
		assertEquals("Third test of Animal serialization, non-reflection",
				three, helpSerialization(reader, three, Animal.class, false));
		final Animal four = new Animal("animalFour", true, true);
		assertEquals("Fourth test of Animal serialization, reflection", four,
				helpSerialization(reader, four, Animal.class, true));
		assertEquals("Fourth test of Animal serialization, non-reflection",
				four, helpSerialization(reader, four, Animal.class, false));
	}

	/**
	 * Test the serialization of CacheFixture.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testCacheSerialization() throws XMLStreamException,
			SPFormatException {
		final CacheFixture one = new CacheFixture("kindOne", "contentsOne");
		assertEquals("First test of Cache serialization, reflection", one,
				helpSerialization(reader, one, CacheFixture.class, true));
		assertEquals("First test of Cache serialization, non-reflection", one,
				helpSerialization(reader, one, CacheFixture.class, false));
		final CacheFixture two = new CacheFixture("kindTwo", "contentsTwo");
		assertEquals("Second test of Cache serialization, reflection", two,
				helpSerialization(reader, two, CacheFixture.class, true));
		assertEquals("Second test of Cache serialization, non-reflection", two,
				helpSerialization(reader, two, CacheFixture.class, false));
	}

	/**
	 * Test the serialization of Centaurs.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testCentaurSerialization() throws XMLStreamException,
			SPFormatException {
		final Centaur one = new Centaur("firstCentaur");
		assertEquals("First test of Centaur serialization, reflection", one,
				helpSerialization(reader, one, Centaur.class, true));
		assertEquals("First test of Centaur serialization, non-reflection",
				one, helpSerialization(reader, one, Centaur.class, false));
		final Centaur two = new Centaur("secondCentaur");
		assertEquals("Second test of Centaur serialization, reflection", two,
				helpSerialization(reader, two, Centaur.class, true));
		assertEquals("Second test of Centaur serialization, non-reflection",
				two, helpSerialization(reader, two, Centaur.class, false));
	}

	/**
	 * Test the serialization of Dragons.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testDragonSerialization() throws XMLStreamException,
			SPFormatException {
		final Dragon one = new Dragon("");
		assertEquals("First test of Dragon serialization, reflection", one,
				helpSerialization(reader, one, Dragon.class, true));
		assertEquals("First test of Dragon serialization, non-reflection", one,
				helpSerialization(reader, one, Dragon.class, false));
		final Dragon two = new Dragon("secondDragon");
		assertEquals("Second test of Dragon serialization, reflection", two,
				helpSerialization(reader, two, Dragon.class, true));
		assertEquals("Second test of Dragon serialization, non-reflection",
				two, helpSerialization(reader, two, Dragon.class, false));
	}

	/**
	 * Test the serialization of Fairies.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testFairySerialization() throws XMLStreamException,
			SPFormatException {
		final Fairy one = new Fairy("oneFairy");
		assertEquals("First test of Fairy serialization, reflection", one,
				helpSerialization(reader, one, Fairy.class, true));
		assertEquals("First test of Fairy serialization, non-reflection", one,
				helpSerialization(reader, one, Fairy.class, false));
		final Fairy two = new Fairy("twoFairy");
		assertEquals("Second test of Fairy serialization, reflection", two,
				helpSerialization(reader, two, Fairy.class, true));
		assertEquals("Second test of Fairy serialization, non-reflection", two,
				helpSerialization(reader, two, Fairy.class, false));
	}

	/**
	 * Test the serialization of Forests.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testForestSerialization() throws XMLStreamException,
			SPFormatException {
		final Forest one = new Forest("firstForest", false);
		assertEquals("First test of Forest serialization, reflection", one,
				helpSerialization(reader, one, Forest.class, true));
		assertEquals("First test of Forest serialization, non-reflection", one,
				helpSerialization(reader, one, Forest.class, false));
		final Forest two = new Forest("secondForest", true);
		assertEquals("Second test of Forest serialization, reflection", two,
				helpSerialization(reader, two, Forest.class, true));
		assertEquals("Second test of Forest serialization, non-reflection",
				two, helpSerialization(reader, two, Forest.class, false));
	}

	/**
	 * Test the serialization of Fortresses.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testFortressSerialization() throws XMLStreamException,
			SPFormatException {
		// Can't give player names because our test environment doesn't let us
		// pass a set of players in
		final Player firstPlayer = new Player(1, "");
		final Fortress one = new Fortress(firstPlayer, "one");
		assertEquals("First test of Fortress serialization, reflection", one,
				helpSerialization(reader, one, Fortress.class, true));
		assertEquals("First test of Fortress serialization, non-reflection",
				one, helpSerialization(reader, one, Fortress.class, false));
		final Fortress two = new Fortress(firstPlayer, "two");
		assertEquals("Second test of Fortress serialization, reflection", two,
				helpSerialization(reader, two, Fortress.class, true));
		assertEquals("Second test of Fortress serialization, non-reflection",
				two, helpSerialization(reader, two, Fortress.class, false));
		final Player secondPlayer = new Player(2, "");
		final Fortress three = new Fortress(secondPlayer, "three");
		assertEquals("Third test of Fortress serialization, reflection", three,
				helpSerialization(reader, three, Fortress.class, true));
		assertEquals("Third test of Fortress serialization, non-reflection",
				three, helpSerialization(reader, three, Fortress.class, false));
		final Fortress four = new Fortress(secondPlayer, "four");
		assertEquals("Fourth test of Fortress serialization, reflection", four,
				helpSerialization(reader, four, Fortress.class, true));
		assertEquals("Fourth test of Fortress serialization, non-reflection",
				four, helpSerialization(reader, four, Fortress.class, false));
		final Fortress five = new Fortress(secondPlayer, "five");
		five.addUnit(new Unit(secondPlayer, "unitOne", "unitTwo"));
		assertEquals("Fifth test of Fortress serialization, reflection", five,
				helpSerialization(reader, five, Fortress.class, true));
		assertEquals("Fifth test of Fortress serialization, non-reflection",
				five, helpSerialization(reader, five, Fortress.class, false));
	}

	/**
	 * Test the serialization of Giants.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testGiantSerialization() throws XMLStreamException,
			SPFormatException {
		final Giant one = new Giant("one");
		assertEquals("First test of Giant serialization, reflection", one,
				helpSerialization(reader, one, Giant.class, true));
		assertEquals("First test of Giant serialization, non-reflection", one,
				helpSerialization(reader, one, Giant.class, false));
		final Giant two = new Giant("two");
		assertEquals("Second test of Giant serialization, reflection", two,
				helpSerialization(reader, two, Giant.class, true));
		assertEquals("Second test of Giant serialization, non-reflection", two,
				helpSerialization(reader, two, Giant.class, false));
	}

	/**
	 * Test the serialization of Ground Fixtures.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testGroundSerialization() throws XMLStreamException,
			SPFormatException {
		final Ground one = new Ground("one", true);
		assertEquals("First test of Ground serialization, reflection", one,
				helpSerialization(reader, one, Ground.class, true));
		assertEquals("First test of Ground serialization, non-reflection", one,
				helpSerialization(reader, one, Ground.class, false));
		final Ground two = new Ground("two", true);
		assertEquals("Second test of Ground serialization, reflection", two,
				helpSerialization(reader, two, Ground.class, true));
		assertEquals("Second test of Ground serialization, non-reflection",
				two, helpSerialization(reader, two, Ground.class, false));
		final Ground three = new Ground("three", false);
		assertEquals("Third test of Ground serialization, reflection", three,
				helpSerialization(reader, three, Ground.class, true));
		assertEquals("Third test of Ground serialization, non-reflection",
				three, helpSerialization(reader, three, Ground.class, false));
	}

	/**
	 * Test the serialization of simple (no-parameter) fixtures.
	 * 
	 * @throws SPFormatException
	 *             on XML format error
	 * @throws XMLStreamException
	 *             on XML reader error
	 */
	@Test
	public void testSimpleSerialization() throws XMLStreamException,
			SPFormatException {
		final Djinn djinn = new Djinn();
		assertEquals("Test of Djinn serialization, reflection", djinn,
				helpSerialization(reader, djinn, Djinn.class, true));
		assertEquals("Test of Djinn serialization, non-reflection", djinn,
				helpSerialization(reader, djinn, Djinn.class, false));
		final Griffin griffin = new Griffin();
		assertEquals("Test of Griffin serialization, reflection", griffin,
				helpSerialization(reader, griffin, Griffin.class, true));
		assertEquals("Test of Griffin serialization, non-reflection", griffin,
				helpSerialization(reader, griffin, Griffin.class, false));
	}

}
