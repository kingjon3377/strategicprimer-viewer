package model.map.fixtures; // NOPMD

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;

import org.junit.Before;
import org.junit.Test;

import util.Warning;
import controller.map.ISPReader;
import controller.map.SPFormatException;

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
				new Animal("animalOne", false, false, 0), Animal.class);
		assertSerialization("Second test of Animal serialization", reader,
				new Animal("animalTwo", false, true, 1), Animal.class);
		assertSerialization("Third test of Animal serialization", reader,
				new Animal("animalThree", true, false, 2), Animal.class);
		final Animal four = new Animal("animalFour", true, true, 3);
		assertSerialization("Fourth test of Animal serialization", reader,
				four, Animal.class);
		assertUnwantedChild(reader, "<animal kind=\"animal\"><troll /></animal>",
				Animal.class, false);
		assertMissingProperty(reader, "<animal />",
				Animal.class, KIND_PROPERTY, false);
		assertEquals("Forward-looking XML in re talking, reflection",
				new Animal("animalFive", false, false, 3),
				reader.readXML(new StringReader(
						"<animal kind=\"animalFive\" talking=\"false\" id=\"3\" />"),
						Animal.class, true, new Warning(Warning.Action.Ignore)));
		assertEquals("Forward-looking XML in re talking, non-reflection",
				new Animal("animalFive", false, false, 4),
				reader.readXML(new StringReader(
						"<animal kind=\"animalFive\" talking=\"false\" id=\"4\" />"),
						Animal.class, false, new Warning(Warning.Action.Ignore)));
		assertMissingProperty(reader,
				"<animal kind=\"animalSix\" talking=\"true\" />", Animal.class,
				"id", true);
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
				new CacheFixture("kindOne", "contentsOne", 1), CacheFixture.class);
		assertSerialization("Second test of Cache serialization", reader,
				new CacheFixture("kindTwo", "contentsTwo", 2), CacheFixture.class);
		assertUnwantedChild(reader, "<cache kind=\"kind\" contents=\"cont\"><troll /></cache>",
				CacheFixture.class, false);
		assertMissingProperty(reader, "<cache contents=\"contents\" />",
				CacheFixture.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<cache kind=\"kind\" />",
				CacheFixture.class, "contents", false);
		assertMissingProperty(reader,
				"<cache kind=\"ind\" contents=\"contents\" />",
				CacheFixture.class, "id", true);
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
				new Centaur("firstCentaur", 0), Centaur.class);
		assertSerialization("Second test of Centaur serialization", reader,
				new Centaur("secondCentaur", 1), Centaur.class);
		assertUnwantedChild(reader, "<centaur kind=\"forest\"><troll /></centaur>",
				Centaur.class, false);
		assertMissingProperty(reader, "<centaur />", Centaur.class, KIND_PROPERTY,
				false);
		assertMissingProperty(reader, "<centaur kind=\"kind\" />",
				Centaur.class, "id", true);
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
				new Dragon("", 1), Dragon.class);
		assertSerialization("Second test of Dragon serialization",
				reader, new Dragon("secondDragon", 2), Dragon.class);
		assertUnwantedChild(reader, "<dragon kind=\"ice\"><hill /></dragon>", Dragon.class, false);
		assertMissingProperty(reader, "<dragon />", Dragon.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<dragon kind=\"kind\" />", Dragon.class, "id", true);
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
				reader, new Fairy("oneFairy", 1), Fairy.class);
		assertSerialization("Second test of Fairy serialization",
				reader, new Fairy("twoFairy", 2), Fairy.class);
		assertUnwantedChild(reader, "<fairy kind=\"great\"><hill /></fairy>", Fairy.class, false);
		assertMissingProperty(reader, "<fairy />", Fairy.class, KIND_PROPERTY, false);
		assertMissingProperty(reader, "<fairy kind=\"kind\" />", Fairy.class, "id", true);
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
				new Fortress(firstPlayer, "one", 1), Fortress.class);
		assertSerialization("Second test of Fortress serialization", reader,
				new Fortress(firstPlayer, "two", 2), Fortress.class);
		final Player secondPlayer = new Player(2, "");
		assertSerialization("Third test of Fortress serialization", reader,
				new Fortress(secondPlayer, "three", 3), Fortress.class);
		assertSerialization(
				"Fourth test of Fortress serialization", reader,
				new Fortress(secondPlayer, "four", 4), Fortress.class);
		final Fortress five = new Fortress(secondPlayer, "five", 5);
		five.addUnit(new Unit(secondPlayer, "unitOne", "unitTwo", 1));
		assertSerialization("Fifth test of Fortress serialization", reader,
				five, Fortress.class);
		assertUnwantedChild(reader, "<fortress><hill /></fortress>", Fortress.class, false);
		assertMissingProperty(reader, "<fortress />", Fortress.class, "owner", true);
		assertMissingProperty(reader, "<fortress owner=\"1\" />", Fortress.class, "name", true);
		assertMissingProperty(reader, "<fortress owner=\"1\" name=\"name\" />",
				Fortress.class, "id", true);
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
				reader, new Giant("one", 1), Giant.class);
		assertSerialization("Second test of Giant serialization",
				reader, new Giant("two", 2), Giant.class);
		assertUnwantedChild(reader, "<giant kind=\"hill\"><hill /></giant>", Giant.class,
				false);
		assertMissingProperty(reader, "<giant />", Giant.class, KIND_PROPERTY,
				false);
		assertMissingProperty(reader, "<giant kind=\"kind\" />", Giant.class, "id", true);
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
				new Djinn(1), Djinn.class);
		assertSerialization("Test of Djinn serialization", reader,
				new Djinn(2), Djinn.class);
		assertUnwantedChild(reader, "<djinn><troll /></djinn>", Djinn.class,
				false);
		assertMissingProperty(reader, "<djinn />", Djinn.class, "id", true);
		assertSerialization("Test of Griffin serialization",
				reader, new Griffin(1), Griffin.class);
		assertSerialization("Test of Griffin serialization",
				reader, new Griffin(2), Griffin.class);
		assertUnwantedChild(reader, "<griffin><djinn /></griffin>",
				Griffin.class, false);
		assertMissingProperty(reader, "<griffin />", Griffin.class, "id", true);
		assertSerialization("Test of Hill serialization", reader,
				new Hill(1), Hill.class);
		assertSerialization("Test of Hill serialization", reader,
				new Hill(2), Hill.class);
		assertUnwantedChild(reader, "<hill><griffin /></hill>", Hill.class, false);
		assertMissingProperty(reader, "<hill />", Hill.class, "id", true);
		assertSerialization("Test of Minotaur serialization",
				reader, new Minotaur(1), Minotaur.class);
		assertSerialization("Test of Minotaur serialization", reader,
				new Minotaur(2), Minotaur.class);
		assertUnwantedChild(reader, "<minotaur><troll /></minotaur>",
				Minotaur.class, false);
		assertMissingProperty(reader, "<minotaur />", Minotaur.class, "id", true);
		assertSerialization("Test of Mountain serialization",
				reader, new Mountain(), Mountain.class);
		assertUnwantedChild(reader, "<mountain><troll /></mountain>",
				Mountain.class, false);
		assertSerialization("Test of Oasis serialization", reader,
				new Oasis(1), Oasis.class);
		assertSerialization("Test of Oasis serialization", reader,
				new Oasis(2), Oasis.class);
		assertUnwantedChild(reader, "<oasis><troll /></oasis>", Oasis.class,
				false);
		assertMissingProperty(reader, "<oasis />", Oasis.class, "id", true);
		assertSerialization("Test of Ogre serialization", reader,
				new Ogre(1), Ogre.class);
		assertSerialization("Test of Ogre serialization", reader,
				new Ogre(2), Ogre.class);
		assertUnwantedChild(reader, "<ogre><troll /></ogre>", Ogre.class,
				false);
		assertMissingProperty(reader, "<ogre />", Ogre.class, "id", true);
		assertSerialization("Test of Phoenix serialization",
				reader, new Phoenix(1), Phoenix.class);
		assertSerialization("Test of Phoenix serialization",
				reader, new Phoenix(2), Phoenix.class);
		assertUnwantedChild(reader, "<phoenix><troll /></phoenix>",
				Phoenix.class, false);
		assertMissingProperty(reader, "<phoenix />", Phoenix.class, "id", true);
		assertSerialization("Test of Sandbar serialization",
				reader, new Sandbar(1), Sandbar.class);
		assertSerialization("Test of Sandbar serialization",
				reader, new Sandbar(2), Sandbar.class);
		assertUnwantedChild(reader, "<sandbar><troll /></sandbar>",
				Sandbar.class, false);
		assertMissingProperty(reader, "<sandbar />", Sandbar.class, "id", true);
		assertSerialization("Test of Simurgh serialization",
				reader, new Simurgh(1), Simurgh.class);
		assertSerialization("Test of Simurgh serialization",
				reader, new Simurgh(2), Simurgh.class);
		assertUnwantedChild(reader, "<simurgh><troll /></simurgh>",
				Simurgh.class, false);
		assertMissingProperty(reader, "<simurgh />", Simurgh.class, "id", true);
		assertSerialization("Test of Sphinx serialization", reader,
				new Sphinx(1), Sphinx.class);
		assertSerialization("Test of Sphinx serialization", reader,
				new Sphinx(2), Sphinx.class);
		assertUnwantedChild(reader, "<sphinx><troll /></sphinx>", Sphinx.class,
				false);
		assertMissingProperty(reader, "<sphinx />", Sphinx.class, "id", true);
		assertSerialization("Test of Troll serialization", reader,
				new Troll(1), Troll.class);
		assertSerialization("Test of Troll serialization", reader,
				new Troll(2), Troll.class);
		assertUnwantedChild(reader, "<troll><troll /></troll>", Troll.class,
				false);
		assertMissingProperty(reader, "<troll />", Troll.class, "id", true);
	}

}
