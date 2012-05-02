package controller.map.readerng;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.TileFixture;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnsupportedTagException;

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
		TAGS.put("animal", new AnimalReader().represents());
		TAGS.put("cache", new CacheReader().represents());
		TAGS.put("centaur", new CentaurReader().represents());
		TAGS.put("djinn", new DjinnReader().represents());
		TAGS.put("dragon", new DragonReader().represents());
		TAGS.put("fairy", new FairyReader().represents());
		TAGS.put("forest", new ForestReader().represents());
		TAGS.put("fortress", new FortressReader().represents());
		TAGS.put("giant", new GiantReader().represents());
		TAGS.put("griffin", new GriffinReader().represents());
		TAGS.put("ground", new GroundReader().represents());
		TAGS.put("grove", new GroveReader().represents());
		TAGS.put("orchard", new GroveReader().represents());
		TAGS.put("hill", new HillReader().represents());
		TAGS.put("meadow", new MeadowReader().represents());
		TAGS.put("field", new MeadowReader().represents());
		TAGS.put("mine", new MineReader().represents());
		TAGS.put("minotaur", new MinotaurReader().represents());
		TAGS.put("mountain", new MountainReader().represents());
		TAGS.put("oasis", new OasisReader().represents());
		TAGS.put("ogre", new OgreReader().represents());
		TAGS.put("phoenix", new PhoenixReader().represents());
		TAGS.put("sandbar", new SandbarReader().represents());
		TAGS.put("shrub", new ShrubReader().represents());
		TAGS.put("simurgh", new SimurghReader().represents());
		TAGS.put("sphinx", new SphinxReader().represents());
		TAGS.put("text", new TextReader().represents());
		TAGS.put("troll", new TrollReader().represents());
		TAGS.put("unit", new UnitReader().represents());
		TAGS.put("village", new VillageReader().represents());
		TAGS.put("battlefield", new BattlefieldReader().represents());
		TAGS.put("cave", new CaveReader().represents());
		TAGS.put("city", new CityReader().represents());
		TAGS.put("fortification", new FortificationReader().represents());
		TAGS.put("mineral", new MineralReader().represents());
		TAGS.put("stone", new StoneReader().represents());
		TAGS.put("town", new TownReader().represents());
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
	 * @param warner the Warning instance to use for warnings
	 * @return the fixture the element represents
	 * @throws SPFormatException if it's not one we know how to parse
	 */
	@Override
	public TileFixture parse(final StartElement element,
			final Iterable<XMLEvent> stream, final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		if (supports(element.getName().getLocalPart())) {
			return ReaderFactory.createReader(
					TAGS.get(element.getName().getLocalPart()
							.toLowerCase(Locale.ENGLISH))).parse(element,
					stream, players, warner);
		} else {
			throw new UnsupportedTagException(element.getName().getLocalPart(),
					element.getLocation().getLineNumber());
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
