package controller.map.readerng;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.TileFixture;
import model.map.events.BattlefieldEvent;
import model.map.events.CaveEvent;
import model.map.events.CityEvent;
import model.map.events.FortificationEvent;
import model.map.events.MineralEvent;
import model.map.events.StoneEvent;
import model.map.events.TownEvent;
import model.map.fixtures.Animal;
import model.map.fixtures.CacheFixture;
import model.map.fixtures.Centaur;
import model.map.fixtures.Djinn;
import model.map.fixtures.Dragon;
import model.map.fixtures.Fairy;
import model.map.fixtures.Forest;
import model.map.fixtures.Fortress;
import model.map.fixtures.Giant;
import model.map.fixtures.Griffin;
import model.map.fixtures.Ground;
import model.map.fixtures.Grove;
import model.map.fixtures.Hill;
import model.map.fixtures.Meadow;
import model.map.fixtures.Mine;
import model.map.fixtures.Minotaur;
import model.map.fixtures.Mountain;
import model.map.fixtures.Oasis;
import model.map.fixtures.Ogre;
import model.map.fixtures.Phoenix;
import model.map.fixtures.Sandbar;
import model.map.fixtures.Shrub;
import model.map.fixtures.Simurgh;
import model.map.fixtures.Sphinx;
import model.map.fixtures.TextFixture;
import model.map.fixtures.Troll;
import model.map.fixtures.Unit;
import model.map.fixtures.Village;
import controller.map.SPFormatException;

/**
 * A reader for TileFixtures. It actually only figures out what kind of fixture,
 * then uses a more specialized reader. Unlike the SimpleXML "event" node that
 * used to be used, this is transparent to the callers.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class FixtureReader implements INodeReader<TileFixture> {
	/**
	 * A mapping from tags to the classes they represent.
	 */
	private static final Map<String, Class<? extends TileFixture>> TAGS = new HashMap<String, Class<? extends TileFixture>>();
	static {
		// FIXME: This information should probaby go in the Fixtures themselves,
		// for future-proofing, and let model-controller mixing go hang ...
		// FIXME: More immediately, use the readers' represents() to reduce the *apparent* coupling. 
		TAGS.put("animal", Animal.class);
		TAGS.put("cache", CacheFixture.class);
		TAGS.put("centaur", Centaur.class);
		TAGS.put("djinn", Djinn.class);
		TAGS.put("dragon", Dragon.class);
		TAGS.put("fairy", Fairy.class);
		TAGS.put("forest", Forest.class);
		TAGS.put("fortress", Fortress.class);
		TAGS.put("giant", Giant.class);
		TAGS.put("griffin", Griffin.class);
		TAGS.put("ground", Ground.class);
		TAGS.put("grove", Grove.class);
		TAGS.put("orchard", Grove.class);
		TAGS.put("hill", Hill.class);
		TAGS.put("meadow", Meadow.class);
		TAGS.put("field", Meadow.class);
		TAGS.put("mine", Mine.class);
		TAGS.put("minotaur", Minotaur.class);
		TAGS.put("mountain", Mountain.class);
		TAGS.put("oasis", Oasis.class);
		TAGS.put("ogre", Ogre.class);
		TAGS.put("phoenix", Phoenix.class);
		TAGS.put("sandbar", Sandbar.class);
		TAGS.put("shrub", Shrub.class);
		TAGS.put("simurgh", Simurgh.class);
		TAGS.put("sphinx", Sphinx.class);
		TAGS.put("text", TextFixture.class);
		TAGS.put("troll", Troll.class);
		TAGS.put("unit", Unit.class);
		TAGS.put("village", Village.class);
		TAGS.put("battlefield", BattlefieldEvent.class);
		TAGS.put("cave", CaveEvent.class);
		TAGS.put("city", CityEvent.class);
		TAGS.put("fortification", FortificationEvent.class);
		TAGS.put("mineral", MineralEvent.class);
		TAGS.put("stone", StoneEvent.class);
		TAGS.put("town", TownEvent.class);
	}
	/**
	 * @return the (super)type we'll return.
	 */
	@Override
	public Class<TileFixture> represents() {
		return TileFixture.class;
	}

	/**
	 * @param element the element
	 * @param stream the stream
	 * @param players the collection of players
	 * @return the fixture the element represents
	 * @throws SPFormatException if it's not one we know how to parse
	 */
	@Override
	public TileFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players)
			throws SPFormatException {
		if (supports(element.getName().getLocalPart())) {
			return ReaderFactory.createReader(
					TAGS.get(element.getName().getLocalPart()
							.toLowerCase(Locale.ENGLISH))).parse(element,
					stream, players);
		} else {
			throw new SPFormatException("Not a fixture we know how to parse", element.getLocation().getLineNumber());
		}
	}
	/**
	 * @param name the name of a tag (we'll lowercase it ourselves)
	 * @return whether that's the tag associated with a kind of fixture we support
	 */
	public static boolean supports(final String name) {
		return TAGS.containsKey(name.toLowerCase(Locale.ENGLISH));
	}
}
