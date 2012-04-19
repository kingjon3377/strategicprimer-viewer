package model.map.fixtures; // NOPMD

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.ISPReader;

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
		reader = createReader();
	}

	/**
	 * The XML reader we'll use to test.
	 */
	private ISPReader reader;

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
		assertSerialization("First test of Animal serialization", reader,
				new Animal("animalOne", false, false), Animal.class);
		assertSerialization("Second test of Animal serialization", reader,
				new Animal("animalTwo", false, true), Animal.class);
		assertSerialization("Third test of Animal serialization", reader,
				new Animal("animalThree", true, false), Animal.class);
		final Animal four = new Animal("animalFour", true, true);
		assertSerialization("Fourth test of Animal serialization", reader,
				four, Animal.class);
		assertUnwantedChild(reader, "<animal kind=\"animal\"><troll /></animal>",
				Animal.class, false);
		assertMissingProperty(reader, "<animal />",
				Animal.class, KIND_PROPERTY, false);
		assertEquals("Forward-looking XML in re talking, reflection",
				new Animal("animalFive", false, false),
				reader.readXML(new StringReader(
						"<animal kind=\"animalFive\" talking=\"false\" />"),
						Animal.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Forward-looking XML in re talking, non-reflection",
				new Animal("animalFive", false, false),
				reader.readXML(new StringReader(
						"<animal kind=\"animalFive\" talking=\"false\" />"),
						Animal.class, false, new Warning(Warning.Action.Ignore)));
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
		assertSerialization("First test of Cache serialization", reader,
				new CacheFixture("kindOne", "contentsOne"), CacheFixture.class);
		assertSerialization("Second test of Cache serialization", reader,
				new CacheFixture("kindTwo", "contentsTwo"), CacheFixture.class);
		assertUnwantedChild(reader, "<cache kind=\"kind\" contents=\"cont\"><troll /></cache>",
				CacheFixture.class, false);
		assertMissingProperty(reader, "<cache contents=\"contents\" />",
				CacheFixture.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<cache kind=\"kind\" />",
				CacheFixture.class, "contents", false);
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
		assertSerialization("First test of Centaur serialization", reader,
				new Centaur("firstCentaur"), Centaur.class);
		assertSerialization("Second test of Centaur serialization", reader,
				new Centaur("secondCentaur"), Centaur.class);
		assertUnwantedChild(reader, "<centaur kind=\"forest\"><troll /></centaur>",
				Centaur.class, false);
		assertMissingProperty(reader, "<centaur />", Centaur.class, KIND_PROPERTY,
				false);
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
		assertSerialization("First test of Dragon serialization", reader,
				new Dragon(""), Dragon.class);
		assertSerialization("Second test of Dragon serialization",
				reader, new Dragon("secondDragon"), Dragon.class);
		assertUnwantedChild(reader, "<dragon kind=\"ice\"><hill /></dragon>", Dragon.class, false);
		assertMissingProperty(reader, "<dragon />", Dragon.class, KIND_PROPERTY, false);
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
		assertSerialization("First test of Fairy serialization",
				reader, new Fairy("oneFairy"), Fairy.class);
		assertSerialization("Second test of Fairy serialization",
				reader, new Fairy("twoFairy"), Fairy.class);
		assertUnwantedChild(reader, "<fairy kind=\"great\"><hill /></fairy>", Fairy.class, false);
		assertMissingProperty(reader, "<fairy />", Fairy.class, KIND_PROPERTY, false);
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
		assertSerialization("First test of Forest serialization",
				reader, new Forest("firstForest", false), Forest.class);
		assertSerialization("Second test of Forest serialization",
				reader, new Forest("secondForest", true), Forest.class);
		assertUnwantedChild(reader, "<forest kind=\"trees\"><hill /></forest>", Forest.class, false);
		assertMissingProperty(reader, "<forest />", Forest.class, KIND_PROPERTY, false);
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
		assertSerialization("First test of Fortress serialization", reader,
				new Fortress(firstPlayer, "one"), Fortress.class);
		assertSerialization("Second test of Fortress serialization", reader,
				new Fortress(firstPlayer, "two"), Fortress.class);
		final Player secondPlayer = new Player(2, "");
		assertSerialization("Third test of Fortress serialization", reader,
				new Fortress(secondPlayer, "three"), Fortress.class);
		assertSerialization(
				"Fourth test of Fortress serialization", reader,
				new Fortress(secondPlayer, "four"), Fortress.class);
		final Fortress five = new Fortress(secondPlayer, "five");
		five.addUnit(new Unit(secondPlayer, "unitOne", "unitTwo"));
		assertSerialization("Fifth test of Fortress serialization", reader,
				five, Fortress.class);
		assertUnwantedChild(reader, "<fortress><hill /></fortress>", Fortress.class, false);
		assertMissingProperty(reader, "<fortress />", Fortress.class, "owner", true);
		assertMissingProperty(reader, "<fortress owner=\"1\" />", Fortress.class, "name", true);
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
		assertSerialization("First test of Giant serialization",
				reader, new Giant("one"), Giant.class);
		assertSerialization("Second test of Giant serialization",
				reader, new Giant("two"), Giant.class);
		assertUnwantedChild(reader, "<giant kind=\"hill\"><hill /></giant>", Giant.class,
				false);
		assertMissingProperty(reader, "<giant />", Giant.class, KIND_PROPERTY,
				false);
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
		assertSerialization("First test of Ground serialization",
				reader, new Ground("one", true), Ground.class);
		assertSerialization("Second test of Ground serialization",
				reader, new Ground("two", true), Ground.class);
		assertSerialization("Third test of Ground serialization",
				reader, new Ground("three", false), Ground.class);
		assertUnwantedChild(reader, "<ground kind=\"sand\" exposed=\"true\"><hill /></ground>", Ground.class,
				false);
		assertMissingProperty(reader, "<ground />", Ground.class,
				KIND_PROPERTY, false);
		assertMissingProperty(reader, "<ground kind=\"ground\" />",
				Ground.class, "exposed", false);
		assertDeprecatedProperty(reader,
				"<ground ground=\"ground\" exposed=\"true\" />", Ground.class,
				"ground", true);
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
		assertSerialization("Test of Djinn serialization", reader,
				new Djinn(), Djinn.class);
		assertUnwantedChild(reader, "<djinn><troll /></djinn>", Djinn.class,
				false);
		assertSerialization("Test of Griffin serialization",
				reader, new Griffin(), Griffin.class);
		assertUnwantedChild(reader, "<griffin><djinn /></griffin>",
				Griffin.class, false);
		assertSerialization("Test of Hill serialization", reader,
				new Hill(), Hill.class);
		assertUnwantedChild(reader, "<hill><griffin /></hill>", Hill.class, false);
		assertSerialization("Test of Minotaur serialization",
				reader, new Minotaur(), Minotaur.class);
		assertUnwantedChild(reader, "<minotaur><troll /></minotaur>",
				Minotaur.class, false);
		assertSerialization("Test of Mountain serialization",
				reader, new Mountain(), Mountain.class);
		assertUnwantedChild(reader, "<mountain><troll /></mountain>",
				Mountain.class, false);
		assertSerialization("Test of Oasis serialization", reader,
				new Oasis(), Oasis.class);
		assertUnwantedChild(reader, "<oasis><troll /></oasis>", Oasis.class,
				false);
		assertSerialization("Test of Ogre serialization", reader,
				new Ogre(), Ogre.class);
		assertUnwantedChild(reader, "<ogre><troll /></ogre>", Ogre.class,
				false);
		assertSerialization("Test of Phoenix serialization",
				reader, new Phoenix(), Phoenix.class);
		assertUnwantedChild(reader, "<phoenix><troll /></phoenix>",
				Phoenix.class, false);
		assertSerialization("Test of Sandbar serialization",
				reader, new Sandbar(), Sandbar.class);
		assertUnwantedChild(reader, "<sandbar><troll /></sandbar>",
				Sandbar.class, false);
		assertSerialization("Test of Simurgh serialization",
				reader, new Simurgh(), Simurgh.class);
		assertUnwantedChild(reader, "<simurgh><troll /></simurgh>",
				Simurgh.class, false);
		assertSerialization("Test of Sphinx serialization", reader,
				new Sphinx(), Sphinx.class);
		assertUnwantedChild(reader, "<sphinx><troll /></sphinx>", Sphinx.class,
				false);
		assertSerialization("Test of Troll serialization", reader,
				new Troll(), Troll.class);
		assertUnwantedChild(reader, "<troll><troll /></troll>", Troll.class,
				false);
	}

}
