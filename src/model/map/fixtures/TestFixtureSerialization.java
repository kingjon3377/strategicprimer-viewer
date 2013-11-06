package model.map.fixtures; // NOPMD

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.BaseTestFixtureSerialization;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.SPMap;
import model.map.Tile;
import model.map.TileType;
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
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import model.map.fixtures.towns.Fortress;

import org.junit.Test;

import controller.map.formatexceptions.SPFormatException;

/**
 * A class to test serialization of TileFixtures.
 *
 * @author Jonathan Lovelace
 */
public final class TestFixtureSerialization extends
		BaseTestFixtureSerialization { // NOPMD
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
				"animalOne", false, false, "wild", 0), Animal.class);
		assertSerialization("Second test of Animal serialization", new Animal(
				"animalTwo", false, true, "semi-domesticated", 1), Animal.class);
		assertSerialization("Third test of Animal serialization", new Animal(
				"animalThree", true, false, "domesticated", 2), Animal.class);
		final Animal four = new Animal("animalFour", true, true, "status", 3);
		assertSerialization("Fourth test of Animal serialization", four,
				Animal.class);
		assertUnwantedChild("<animal kind=\"animal\"><troll /></animal>",
				Animal.class, false);
		assertMissingProperty("<animal />", Animal.class, KIND_PROPERTY, false);
		assertForwardDeserialization(
				"Forward-looking XML in re talking, reflection", new Animal(
						"animalFive", false, false, "wild", 3),
				"<animal kind=\"animalFive\" talking=\"false\" id=\"3\" />",
				Animal.class);
		assertMissingProperty("<animal kind=\"animalSix\" talking=\"true\" />",
				Animal.class, "id", true);
		assertForwardDeserialization("Explicit default status of animal",
				new Animal("animalSeven", false, false, "wild", 4),
				"<animal kind=\"animalSeven\" status=\"wild\" id=\"4\" />",
				Animal.class);
		assertImageSerialization("Animal image property is preserved", four,
				Animal.class);
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
				new CacheFixture("kindOne", "contentsOne", 1),
				CacheFixture.class);
		assertSerialization("Second test of Cache serialization",
				new CacheFixture("kindTwo", "contentsTwo", 2),
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
		assertImageSerialization("Cache image property is preserved",
				new CacheFixture("kindThree", "contentsThree", 3),
				CacheFixture.class);
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
				"firstCentaur", 0), Centaur.class);
		assertSerialization("Second test of Centaur serialization",
				new Centaur("secondCentaur", 1), Centaur.class);
		assertUnwantedChild("<centaur kind=\"forest\"><troll /></centaur>",
				Centaur.class, false);
		assertMissingProperty("<centaur />", Centaur.class, KIND_PROPERTY,
				false);
		assertMissingProperty("<centaur kind=\"kind\" />", Centaur.class, "id",
				true);
		assertImageSerialization("Centaur image property is preserved",
				new Centaur("thirdCentaur", 2), Centaur.class);
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
				"", 1), Dragon.class);
		assertSerialization("Second test of Dragon serialization", new Dragon(
				"secondDragon", 2), Dragon.class);
		assertUnwantedChild("<dragon kind=\"ice\"><hill /></dragon>",
				Dragon.class, false);
		assertMissingProperty("<dragon />", Dragon.class, KIND_PROPERTY, false);
		assertMissingProperty("<dragon kind=\"kind\" />", Dragon.class, "id",
				true);
		assertImageSerialization("Dragon image property is preserved",
				new Dragon("thirdDragon", 3), Dragon.class);
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
				"oneFairy", 1), Fairy.class);
		assertSerialization("Second test of Fairy serialization", new Fairy(
				"twoFairy", 2), Fairy.class);
		assertUnwantedChild("<fairy kind=\"great\"><hill /></fairy>",
				Fairy.class, false);
		assertMissingProperty("<fairy />", Fairy.class, KIND_PROPERTY, false);
		assertMissingProperty("<fairy kind=\"kind\" />", Fairy.class, "id",
				true);
		assertImageSerialization("Fairy image property is preserved",
				new Fairy("threeFairy", 3), Fairy.class);
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
				"firstForest", false), Forest.class);
		assertSerialization("Second test of Forest serialization", new Forest(
				"secondForest", true), Forest.class);
		assertUnwantedChild("<forest kind=\"trees\"><hill /></forest>",
				Forest.class, false);
		assertMissingProperty("<forest />", Forest.class, KIND_PROPERTY, false);
		assertImageSerialization("Forest image property is preserved",
				new Forest("thirdForest", true), Forest.class);
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
		final Player firstPlayer = new Player(1, "");
		assertSerialization("First test of Fortress serialization",
				new Fortress(firstPlayer, "one", 1), Fortress.class);
		assertSerialization("Second test of Fortress serialization",
				new Fortress(firstPlayer, "two", 2), Fortress.class);
		final Player secondPlayer = new Player(2, "");
		assertSerialization("Third test of Fortress serialization",
				new Fortress(secondPlayer, "three", 3), Fortress.class);
		assertSerialization("Fourth test of Fortress serialization",
				new Fortress(secondPlayer, "four", 4), Fortress.class);
		final Fortress five = new Fortress(secondPlayer, "five", 5);
		five.addUnit(new Unit(secondPlayer, "unitOne", "unitTwo", 1));
		assertSerialization("Fifth test of Fortress serialization", five,
				Fortress.class);
		assertUnwantedChild("<fortress><hill /></fortress>", Fortress.class,
				false);
		assertMissingProperty("<fortress />", Fortress.class, "owner", true);
		assertMissingProperty("<fortress owner=\"1\" />", Fortress.class,
				"name", true);
		assertMissingProperty("<fortress owner=\"1\" name=\"name\" />",
				Fortress.class, "id", true);
		assertImageSerialization("Fortress image property is preserved", five,
				Fortress.class);
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
				"one", 1), Giant.class);
		assertSerialization("Second test of Giant serialization", new Giant(
				"two", 2), Giant.class);
		assertUnwantedChild("<giant kind=\"hill\"><hill /></giant>",
				Giant.class, false);
		assertMissingProperty("<giant />", Giant.class, KIND_PROPERTY, false);
		assertMissingProperty("<giant kind=\"kind\" />", Giant.class, "id",
				true);
		assertImageSerialization("Giant image property is preserved",
				new Giant("three", 3), Giant.class);
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
				"one", true), Ground.class);
		assertSerialization("Second test of Ground serialization", new Ground(
				"two", true), Ground.class);
		assertSerialization("Third test of Ground serialization", new Ground(
				"three", false), Ground.class);
		final Point point = PointFactory.point(0, 0);
		final Tile tile = new Tile(TileType.Steppe);
		tile.addFixture(new Ground("four", true));
		final SPMap map = new SPMap(new MapDimensions(1, 1, 2));
		map.addTile(point, tile);
		assertSerialization("Test that reader handles ground as a fixture",
				map, SPMap.class);
		assertUnwantedChild(
				"<ground kind=\"sand\" exposed=\"true\"><hill /></ground>",
				Ground.class, false);
		assertMissingProperty("<ground />", Ground.class, KIND_PROPERTY, false);
		assertMissingProperty("<ground kind=\"ground\" />", Ground.class,
				"exposed", false);
		assertDeprecatedProperty(
				"<ground ground=\"ground\" exposed=\"true\" />", Ground.class,
				"ground", true);
		assertImageSerialization("Ground image property is preserved",
				new Ground("five", true), Ground.class);
	}
	/**
	 * Test that simple (no-parameter-except-ID) fixtures shouldn't have children.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 */
	@Test
	public void testSimpleSerializationNoChildren() throws XMLStreamException, SPFormatException {
		assertUnwantedChild("<djinn><troll /></djinn>", Djinn.class, false);
		assertUnwantedChild("<griffin><djinn /></griffin>", Griffin.class,
				false);
		assertUnwantedChild("<hill><griffin /></hill>", Hill.class, false);
		assertUnwantedChild("<minotaur><troll /></minotaur>", Minotaur.class,
				false);
		assertUnwantedChild("<mountain><troll /></mountain>", Mountain.class,
				false);
		assertUnwantedChild("<oasis><troll /></oasis>", Oasis.class, false);
		assertUnwantedChild("<ogre><troll /></ogre>", Ogre.class, false);
		assertUnwantedChild("<phoenix><troll /></phoenix>", Phoenix.class,
				false);
		assertUnwantedChild("<sandbar><troll /></sandbar>", Sandbar.class,
				false);
		assertUnwantedChild("<simurgh><troll /></simurgh>", Simurgh.class,
				false);
		assertUnwantedChild("<sphinx><troll /></sphinx>", Sphinx.class, false);
		assertUnwantedChild("<troll><troll /></troll>", Troll.class, false);
	}
	/**
	 * Test that serialization of simple (no-parameter) fixtures preserves image property.
	 * @throws SPFormatException on XML format error
	 * @throws XMLStreamException on XML reader error
	 * @throws IOException on I/O error creating serialized form
	 */
	@Test
	public void testSimpleImageSerialization() throws XMLStreamException, SPFormatException, IOException {
		assertImageSerialization("Djinn image property is preserved",
				new Djinn(3), Djinn.class);
		assertImageSerialization("Griffin image property is preserved",
				new Griffin(3), Griffin.class);
		assertImageSerialization("Hill image property is preserved",
				new Hill(3), Hill.class);
		assertImageSerialization("Minotaur image property is preserved",
				new Minotaur(3), Minotaur.class);
		assertImageSerialization("Mountain image property is preserved",
				new Mountain(), Mountain.class);
		assertImageSerialization("Oasis image property is preserved",
				new Oasis(3), Oasis.class);
		assertImageSerialization("Ogre image property is preserved",
				new Ogre(3), Ogre.class);
		assertImageSerialization("Phoenix image property is preserved",
				new Phoenix(3), Phoenix.class);
		assertImageSerialization("Sandbar image property is preserved",
				new Sandbar(3), Sandbar.class);
		assertImageSerialization("Simurgh image property is preserved",
				new Simurgh(3), Simurgh.class);
		assertImageSerialization("Sphinx image property is preserved",
				new Sphinx(3), Sphinx.class);
		assertImageSerialization("Troll image property is preserved",
				new Troll(3), Troll.class);
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
	public void testSimpleSerialization() throws XMLStreamException, // NOPMD
			SPFormatException, IOException {
		assertSerialization("Djinn serialization", new Djinn(1), Djinn.class);
		assertSerialization("Djinn serialization", new Djinn(2), Djinn.class);
		assertMissingProperty("<djinn />", Djinn.class, "id", true);
		assertSerialization("Griffin serialization", new Griffin(1),
				Griffin.class);
		assertSerialization("Griffin serialization", new Griffin(2),
				Griffin.class);
		assertMissingProperty("<griffin />", Griffin.class, "id", true);
		assertSerialization("Hill serialization", new Hill(1), Hill.class);
		assertSerialization("Hill serialization", new Hill(2), Hill.class);
		assertMissingProperty("<hill />", Hill.class, "id", true);
		assertSerialization("Minotaur serialization", new Minotaur(1),
				Minotaur.class);
		assertSerialization("Minotaur serialization", new Minotaur(2),
				Minotaur.class);
		assertMissingProperty("<minotaur />", Minotaur.class, "id", true);
		assertSerialization("Mountain serialization", new Mountain(),
				Mountain.class);
		assertSerialization("Oasis serialization", new Oasis(1), Oasis.class);
		assertSerialization("Oasis serialization", new Oasis(2), Oasis.class);
		assertMissingProperty("<oasis />", Oasis.class, "id", true);
		assertSerialization("Ogre serialization", new Ogre(1), Ogre.class);
		assertSerialization("Ogre serialization", new Ogre(2), Ogre.class);
		assertMissingProperty("<ogre />", Ogre.class, "id", true);
		assertSerialization("Phoenix serialization", new Phoenix(1),
				Phoenix.class);
		assertSerialization("Phoenix serialization", new Phoenix(2),
				Phoenix.class);
		assertMissingProperty("<phoenix />", Phoenix.class, "id", true);
		assertSerialization("Sandbar serialization", new Sandbar(1),
				Sandbar.class);
		assertSerialization("Sandbar serialization", new Sandbar(2),
				Sandbar.class);
		assertMissingProperty("<sandbar />", Sandbar.class, "id", true);
		assertSerialization("Simurgh serialization", new Simurgh(1),
				Simurgh.class);
		assertSerialization("Simurgh serialization", new Simurgh(2),
				Simurgh.class);
		assertMissingProperty("<simurgh />", Simurgh.class, "id", true);
		assertSerialization("Sphinx serialization", new Sphinx(1), Sphinx.class);
		assertSerialization("Sphinx serialization", new Sphinx(2), Sphinx.class);
		assertMissingProperty("<sphinx />", Sphinx.class, "id", true);
		assertSerialization("Troll serialization", new Troll(1), Troll.class);
		assertSerialization("Troll serialization", new Troll(2), Troll.class);
		assertMissingProperty("<troll />", Troll.class, "id", true);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TestFixtureSerialization";
	}
}
