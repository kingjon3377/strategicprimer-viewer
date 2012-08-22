package model.map.fixtures; // NOPMD

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.Player;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.Fortress;

import org.junit.Test;

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
	 * Test the serialization of Animal, including catching format errors.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testAnimalSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Animal serialization", new Animal(
				"animalOne", false, false, 0, FAKE_FILENAME), Animal.class);
		assertSerialization("Second test of Animal serialization", new Animal(
				"animalTwo", false, true, 1, FAKE_FILENAME), Animal.class);
		assertSerialization("Third test of Animal serialization", new Animal(
				"animalThree", true, false, 2, FAKE_FILENAME), Animal.class);
		final Animal four = new Animal("animalFour", true, true, 3,
				FAKE_FILENAME);
		assertSerialization("Fourth test of Animal serialization", four,
				Animal.class);
		assertUnwantedChild("<animal kind=\"animal\"><troll /></animal>",
				Animal.class, false);
		assertMissingProperty("<animal />", Animal.class, KIND_PROPERTY, false);
		assertForwardDeserialization(
				"Forward-looking XML in re talking, reflection", new Animal(
						"animalFive", false, false, 3, FAKE_FILENAME),
				"<animal kind=\"animalFive\" talking=\"false\" id=\"3\" />",
				Animal.class);
		assertMissingProperty("<animal kind=\"animalSix\" talking=\"true\" />",
				Animal.class, "id", true);
	}

	/**
	 * Test the serialization of CacheFixture, including catching format errors.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testCacheSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Cache serialization",
				new CacheFixture("kindOne", "contentsOne", 1, FAKE_FILENAME),
				CacheFixture.class);
		assertSerialization("Second test of Cache serialization",
				new CacheFixture("kindTwo", "contentsTwo", 2, FAKE_FILENAME),
				CacheFixture.class);
		assertUnwantedChild(
				"<cache kind=\"kind\" contents=\"cont\"><troll /></cache>",
				CacheFixture.class, false);
		assertMissingProperty("<cache contents=\"contents\" />",
				CacheFixture.class, KIND_PROPERTY, false);
		assertMissingProperty("<cache kind=\"kind\" />", CacheFixture.class,
				"contents", false);
		assertMissingProperty("<cache kind=\"ind\" contents=\"contents\" />",
				CacheFixture.class, "id", true);
	}

	/**
	 * Test the serialization of Centaurs, including catching format errors.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testCentaurSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Centaur serialization", new Centaur(
				"firstCentaur", 0, FAKE_FILENAME), Centaur.class);
		assertSerialization("Second test of Centaur serialization",
				new Centaur("secondCentaur", 1, FAKE_FILENAME), Centaur.class);
		assertUnwantedChild("<centaur kind=\"forest\"><troll /></centaur>",
				Centaur.class, false);
		assertMissingProperty("<centaur />", Centaur.class, KIND_PROPERTY,
				false);
		assertMissingProperty("<centaur kind=\"kind\" />", Centaur.class, "id",
				true);
	}

	/**
	 * Test the serialization of Dragons.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testDragonSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Dragon serialization", new Dragon(
				"", 1, FAKE_FILENAME), Dragon.class);
		assertSerialization("Second test of Dragon serialization", new Dragon(
				"secondDragon", 2, FAKE_FILENAME), Dragon.class);
		assertUnwantedChild("<dragon kind=\"ice\"><hill /></dragon>",
				Dragon.class, false);
		assertMissingProperty("<dragon />", Dragon.class, KIND_PROPERTY, false);
		assertMissingProperty("<dragon kind=\"kind\" />", Dragon.class, "id",
				true);
	}

	/**
	 * Test the serialization of Fairies.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testFairySerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Fairy serialization", new Fairy(
				"oneFairy", 1, FAKE_FILENAME), Fairy.class);
		assertSerialization("Second test of Fairy serialization", new Fairy(
				"twoFairy", 2, FAKE_FILENAME), Fairy.class);
		assertUnwantedChild("<fairy kind=\"great\"><hill /></fairy>",
				Fairy.class, false);
		assertMissingProperty("<fairy />", Fairy.class, KIND_PROPERTY, false);
		assertMissingProperty("<fairy kind=\"kind\" />", Fairy.class, "id",
				true);
	}

	/**
	 * Test the serialization of Forests.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testForestSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Forest serialization", new Forest(
				"firstForest", false, FAKE_FILENAME), Forest.class);
		assertSerialization("Second test of Forest serialization", new Forest(
				"secondForest", true, FAKE_FILENAME), Forest.class);
		assertUnwantedChild("<forest kind=\"trees\"><hill /></forest>",
				Forest.class, false);
		assertMissingProperty("<forest />", Forest.class, KIND_PROPERTY, false);
	}

	/**
	 * Test the serialization of Fortresses.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testFortressSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		// Can't give player names because our test environment doesn't let us
		// pass a set of players in
		final Player firstPlayer = new Player(1, "", FAKE_FILENAME);
		assertSerialization("First test of Fortress serialization",
				new Fortress(firstPlayer, "one", 1, FAKE_FILENAME),
				Fortress.class);
		assertSerialization("Second test of Fortress serialization",
				new Fortress(firstPlayer, "two", 2, FAKE_FILENAME),
				Fortress.class);
		final Player secondPlayer = new Player(2, "", FAKE_FILENAME);
		secondPlayer.setFile("string");
		assertSerialization("Third test of Fortress serialization",
				new Fortress(secondPlayer, "three", 3, FAKE_FILENAME),
				Fortress.class);
		assertSerialization("Fourth test of Fortress serialization",
				new Fortress(secondPlayer, "four", 4, FAKE_FILENAME),
				Fortress.class);
		final Fortress five = new Fortress(secondPlayer, "five", 5,
				FAKE_FILENAME);
		five.setFile("string");
		five.addUnit(setFileOnObject(new Unit(secondPlayer, "unitOne",
				"unitTwo", 1, FAKE_FILENAME)));
		assertSerialization("Fifth test of Fortress serialization", five,
				Fortress.class);
		assertUnwantedChild("<fortress><hill /></fortress>", Fortress.class,
				false);
		assertMissingProperty("<fortress />", Fortress.class, "owner", true);
		assertMissingProperty("<fortress owner=\"1\" />", Fortress.class,
				"name", true);
		assertMissingProperty("<fortress owner=\"1\" name=\"name\" />",
				Fortress.class, "id", true);
	}

	/**
	 * Test the serialization of Giants.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testGiantSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Giant serialization", new Giant(
				"one", 1, FAKE_FILENAME), Giant.class);
		assertSerialization("Second test of Giant serialization", new Giant(
				"two", 2, FAKE_FILENAME), Giant.class);
		assertUnwantedChild("<giant kind=\"hill\"><hill /></giant>",
				Giant.class, false);
		assertMissingProperty("<giant />", Giant.class, KIND_PROPERTY, false);
		assertMissingProperty("<giant kind=\"kind\" />", Giant.class, "id",
				true);
	}

	/**
	 * Test the serialization of Ground Fixtures.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testGroundSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("First test of Ground serialization", new Ground(
				"one", true, FAKE_FILENAME), Ground.class);
		assertSerialization("Second test of Ground serialization", new Ground(
				"two", true, FAKE_FILENAME), Ground.class);
		assertSerialization("Third test of Ground serialization", new Ground(
				"three", false, FAKE_FILENAME), Ground.class);
		assertUnwantedChild(
				"<ground kind=\"sand\" exposed=\"true\"><hill /></ground>",
				Ground.class, false);
		assertMissingProperty("<ground />", Ground.class, KIND_PROPERTY, false);
		assertMissingProperty("<ground kind=\"ground\" />", Ground.class,
				"exposed", false);
		assertDeprecatedProperty(
				"<ground ground=\"ground\" exposed=\"true\" />", Ground.class,
				"ground", true);
	}

	/**
	 * Test the serialization of simple (no-parameter) fixtures, including
	 * format errors.
	 *
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testSimpleSerialization() throws XMLStreamException,
			SPFormatException, IOException {
		assertSerialization("Test of Djinn serialization", new Djinn(1,
				FAKE_FILENAME), Djinn.class);
		assertSerialization("Test of Djinn serialization", new Djinn(2,
				FAKE_FILENAME), Djinn.class);
		assertUnwantedChild("<djinn><troll /></djinn>", Djinn.class, false);
		assertMissingProperty("<djinn />", Djinn.class, "id", true);
		assertSerialization("Test of Griffin serialization", new Griffin(1,
				FAKE_FILENAME), Griffin.class);
		assertSerialization("Test of Griffin serialization", new Griffin(2,
				FAKE_FILENAME), Griffin.class);
		assertUnwantedChild("<griffin><djinn /></griffin>", Griffin.class,
				false);
		assertMissingProperty("<griffin />", Griffin.class, "id", true);
		assertSerialization("Test of Hill serialization", new Hill(1,
				FAKE_FILENAME), Hill.class);
		assertSerialization("Test of Hill serialization", new Hill(2,
				FAKE_FILENAME), Hill.class);
		assertUnwantedChild("<hill><griffin /></hill>", Hill.class, false);
		assertMissingProperty("<hill />", Hill.class, "id", true);
		assertSerialization("Test of Minotaur serialization", new Minotaur(1,
				FAKE_FILENAME), Minotaur.class);
		assertSerialization("Test of Minotaur serialization", new Minotaur(2,
				FAKE_FILENAME), Minotaur.class);
		assertUnwantedChild("<minotaur><troll /></minotaur>", Minotaur.class,
				false);
		assertMissingProperty("<minotaur />", Minotaur.class, "id", true);
		assertSerialization("Test of Mountain serialization", new Mountain(
				FAKE_FILENAME), Mountain.class);
		assertUnwantedChild("<mountain><troll /></mountain>", Mountain.class,
				false);
		assertSerialization("Test of Oasis serialization", new Oasis(1,
				FAKE_FILENAME), Oasis.class);
		assertSerialization("Test of Oasis serialization", new Oasis(2,
				FAKE_FILENAME), Oasis.class);
		assertUnwantedChild("<oasis><troll /></oasis>", Oasis.class, false);
		assertMissingProperty("<oasis />", Oasis.class, "id", true);
		assertSerialization("Test of Ogre serialization", new Ogre(1,
				FAKE_FILENAME), Ogre.class);
		assertSerialization("Test of Ogre serialization", new Ogre(2,
				FAKE_FILENAME), Ogre.class);
		assertUnwantedChild("<ogre><troll /></ogre>", Ogre.class, false);
		assertMissingProperty("<ogre />", Ogre.class, "id", true);
		assertSerialization("Test of Phoenix serialization", new Phoenix(1,
				FAKE_FILENAME), Phoenix.class);
		assertSerialization("Test of Phoenix serialization", new Phoenix(2,
				FAKE_FILENAME), Phoenix.class);
		assertUnwantedChild("<phoenix><troll /></phoenix>", Phoenix.class,
				false);
		assertMissingProperty("<phoenix />", Phoenix.class, "id", true);
		assertSerialization("Test of Sandbar serialization", new Sandbar(1,
				FAKE_FILENAME), Sandbar.class);
		assertSerialization("Test of Sandbar serialization", new Sandbar(2,
				FAKE_FILENAME), Sandbar.class);
		assertUnwantedChild("<sandbar><troll /></sandbar>", Sandbar.class,
				false);
		assertMissingProperty("<sandbar />", Sandbar.class, "id", true);
		assertSerialization("Test of Simurgh serialization", new Simurgh(1,
				FAKE_FILENAME), Simurgh.class);
		assertSerialization("Test of Simurgh serialization", new Simurgh(2,
				FAKE_FILENAME), Simurgh.class);
		assertUnwantedChild("<simurgh><troll /></simurgh>", Simurgh.class,
				false);
		assertMissingProperty("<simurgh />", Simurgh.class, "id", true);
		assertSerialization("Test of Sphinx serialization", new Sphinx(1,
				FAKE_FILENAME), Sphinx.class);
		assertSerialization("Test of Sphinx serialization", new Sphinx(2,
				FAKE_FILENAME), Sphinx.class);
		assertUnwantedChild("<sphinx><troll /></sphinx>", Sphinx.class, false);
		assertMissingProperty("<sphinx />", Sphinx.class, "id", true);
		assertSerialization("Test of Troll serialization", new Troll(1,
				FAKE_FILENAME), Troll.class);
		assertSerialization("Test of Troll serialization", new Troll(2,
				FAKE_FILENAME), Troll.class);
		assertUnwantedChild("<troll><troll /></troll>", Troll.class, false);
		assertMissingProperty("<troll />", Troll.class, "id", true);
	}

}
