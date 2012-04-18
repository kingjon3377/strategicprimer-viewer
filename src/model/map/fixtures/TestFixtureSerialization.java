package model.map.fixtures; // NOPMD

import static org.junit.Assert.assertEquals;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
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
public final class TestFixtureSerialization extends
		BaseTestFixtureSerialization {
	/**
	 * Extracted constant.
	 */
	private static final String KIND_PROPERTY = "kind";
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
	 * Test the serialization of Animal, including catching format errors.
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
	 * Test the serialization of CacheFixture, including catching format errors.
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
	 * Test the serialization of Centaurs, including catching format errors.
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
		// TODO: errors.
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
		// TODO: errors
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
		// TODO: errors
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
		// TODO: errors
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
		// TODO: errors
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
		// TODO: errors
	}

	/**
	 * Test the serialization of simple (no-parameter) fixtures, including format errors.
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
		assertUnwantedChild(reader, "<djinn><troll /></djinn>", Djinn.class,
				false, false);
		assertUnwantedChild(reader, "<djinn><troll /></djinn>", Djinn.class,
				true, false);
		final Griffin griffin = new Griffin();
		assertEquals("Test of Griffin serialization, reflection", griffin,
				helpSerialization(reader, griffin, Griffin.class, true));
		assertEquals("Test of Griffin serialization, non-reflection", griffin,
				helpSerialization(reader, griffin, Griffin.class, false));
		// TODO: errors
		final Hill hill = new Hill();
		assertEquals("Test of Hill serialization, reflection", hill,
				helpSerialization(reader, hill, Hill.class, true));
		assertEquals("Test of Hill serialization, non-reflection", hill,
				helpSerialization(reader, hill, Hill.class, false));
		// TODO: errors
		final Minotaur minotaur = new Minotaur();
		assertEquals("Test of Minotaur serialization, reflection", minotaur,
				helpSerialization(reader, minotaur, Minotaur.class, true));
		assertEquals("Test of Minotaur serialization, non-reflection",
				minotaur,
				helpSerialization(reader, minotaur, Minotaur.class, false));
		assertUnwantedChild(reader, "<minotaur><troll /></minotaur>",
				Minotaur.class, false, false);
		assertUnwantedChild(reader, "<minotaur><troll /></minotaur>",
				Minotaur.class, true, false);
		final Mountain mountain = new Mountain();
		assertEquals("Test of Mountain serialization, reflection", mountain,
				helpSerialization(reader, mountain, Mountain.class, true));
		assertEquals("Test of Mountain serialization, non-reflection",
				mountain,
				helpSerialization(reader, mountain, Mountain.class, false));
		assertUnwantedChild(reader, "<mountain><troll /></mountain>",
				Mountain.class, false, false);
		assertUnwantedChild(reader, "<mountain><troll /></mountain>",
				Mountain.class, true, false);
		final Oasis oasis = new Oasis();
		assertEquals("Test of Oasis serialization, reflection", oasis,
				helpSerialization(reader, oasis, Oasis.class, true));
		assertEquals("Test of Oasis serialization, non-reflection", oasis,
				helpSerialization(reader, oasis, Oasis.class, false));
		assertUnwantedChild(reader, "<oasis><troll /></oasis>",
				Oasis.class, false, false);
		assertUnwantedChild(reader, "<oasis><troll /></oasis>",
				Oasis.class, true, false);
		final Ogre ogre = new Ogre();
		assertEquals("Test of Ogre serialization, reflection", ogre,
				helpSerialization(reader, ogre, Ogre.class, true));
		assertEquals("Test of Ogre serialization, non-reflection", ogre,
				helpSerialization(reader, ogre, Ogre.class, false));
		assertUnwantedChild(reader, "<ogre><troll /></ogre>",
				Ogre.class, false, false);
		assertUnwantedChild(reader, "<ogre><troll /></ogre>",
				Ogre.class, true, false);
		final Phoenix phoenix = new Phoenix();
		assertEquals("Test of Phoenix serialization, reflection", phoenix,
				helpSerialization(reader, phoenix, Phoenix.class, true));
		assertEquals("Test of Phoenix serialization, non-reflection", phoenix,
				helpSerialization(reader, phoenix, Phoenix.class, false));
		assertUnwantedChild(reader, "<phoenix><troll /></phoenix>",
				Phoenix.class, false, false);
		assertUnwantedChild(reader, "<phoenix><troll /></phoenix>",
				Phoenix.class, true, false);
		final Sandbar sandbar = new Sandbar();
		assertEquals("Test of Sandbar serialization, reflection", sandbar,
				helpSerialization(reader, sandbar, Sandbar.class, true));
		assertEquals("Test of Sandbar serialization, non-reflection", sandbar,
				helpSerialization(reader, sandbar, Sandbar.class, false));
		assertUnwantedChild(reader, "<sandbar><troll /></sandbar>",
				Sandbar.class, false, false);
		assertUnwantedChild(reader, "<sandbar><troll /></sandbar>",
				Sandbar.class, true, false);
		final Simurgh simurgh = new Simurgh();
		assertEquals("Test of Simurgh serialization, reflection", simurgh,
				helpSerialization(reader, simurgh, Simurgh.class, true));
		assertEquals("Test of Simurgh serialization, non-reflection", simurgh,
				helpSerialization(reader, simurgh, Simurgh.class, false));
		assertUnwantedChild(reader, 
				"<simurgh><troll /></simurgh>",
				Simurgh.class, false, false);
		assertUnwantedChild(reader, 
				"<simurgh><troll /></simurgh>",
				Simurgh.class, true, false);
		final Sphinx sphinx = new Sphinx();
		assertEquals("Test of Sphinx serialization, reflection", sphinx,
				helpSerialization(reader, sphinx, Sphinx.class, true));
		assertEquals("Test of Sphinx serialization, non-reflection", sphinx,
				helpSerialization(reader, sphinx, Sphinx.class, false));
		assertUnwantedChild(reader, 
				"<sphinx><troll /></sphinx>",
				Sphinx.class, false, false);
		assertUnwantedChild(reader, 
				"<sphinx><troll /></sphinx>",
				Sphinx.class, true, false);
		final Troll troll = new Troll();
		assertEquals("Test of Troll serialization, reflection", troll,
				helpSerialization(reader, troll, Troll.class, true));
		assertEquals("Test of Troll serialization, non-reflection", troll,
				helpSerialization(reader, troll, Troll.class, false));
		assertUnwantedChild(reader, "<troll><troll /></troll>", Troll.class,
				false, false);
		assertUnwantedChild(reader, "<troll><troll /></troll>", Troll.class,
				true, false);
	}

}
