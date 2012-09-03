package controller.map.cxml;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import util.ArraySet;
import util.IteratorWrapper;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A reader for tiles, including rivers.
 * @author Jonathan Lovelace
 *
 */
public final class CompactMobileReader extends CompactReaderSuperclass implements CompactReader<MobileFixture> {
	/**
	 * Singleton.
	 */
	private CompactMobileReader() {
		// Singleton.
	}
	/**
	 * Enumeration of the types we know how to handle.
	 */
	private static enum MobileType {
		/**
		 * Animal.
		 */
		AnimalType("animal"),
		/**
		 * Centaur.
		 */
		CentaurType("centaur"),
		/**
		 * Djinn.
		 */
		DjinnType("djinn"),
		/**
		 * Dragon.
		 */
		DragonType("dragon"),
		/**
		 * Fairy.
		 */
		FairyType("fairy"),
		/**
		 * Giant.
		 */
		GiantType("giant"),
		/**
		 * Griffin.
		 */
		GriffinType("griffin"),
		/**
		 * Minotaur.
		 */
		MinotaurType("minotaur"),
		/**
		 * Ogre.
		 */
		OgreType("ogre"),
		/**
		 * Phoenix.
		 */
		PhoenixType("phoenix"),
		/**
		 * Simurgh.
		 */
		SimurghType("simurgh"),
		/**
		 *Sphinx.
		 */
		SphinxType("sphinx"),
		/**
		 * Troll.
		 */
		TrollType("troll"),
		/**
		 * Unit. (Handled by a different reader, but might get directed here by
		 * mistake, so we 'handle' it anyway.
		 */
		UnitType("unit");
		/**
		 * The tag.
		 */
		public final String tag;
		/**
		 * Constructor.
		 * @param tagString The tag.
		 */
		MobileType(final String tagString) {
			tag = tagString;
		}
	}
	/**
	 * Mapping from tags to enum-tags.
	 */
	private static final Map<String, MobileType> MAP = new HashMap<String, MobileType>(MobileType.values().length);
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS;
	static {
		final Set<String> suppTagsTemp = new ArraySet<String>();
		for (MobileType mt : MobileType.values()) {
			MAP.put(mt.tag, mt);
			suppTagsTemp.add(mt.tag);
		}
		SUPP_TAGS = Collections.unmodifiableSet(suppTagsTemp);
	}
	/**
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPP_TAGS.contains(tag);
	}
	/**
	 * Singleton object.
	 */
	public static final CompactMobileReader READER = new CompactMobileReader();
	/**
	 *
	 * @param element the XML element to parse
	 * @param stream the stream to read more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public MobileFixture read(final StartElement element,
			final IteratorWrapper<XMLEvent> stream, final PlayerCollection players,
			final Warning warner, final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "animal", "centaur", "djinn", "dragon", "fairy",
				"giant", "griffin", "minotaur", "ogre", "phoenix", "simurgh",
				"sphinx", "troll", "unit");
		// ESCA-JAVA0177:
		final MobileFixture retval; // NOPMD
		switch (MAP.get(element.getName().getLocalPart())) {
		case UnitType:
			return CompactUnitReader.READER.read(element, stream, players, // NOPMD
					warner, idFactory);
		case AnimalType:
			retval = createAnimal(element,
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case CentaurType:
			retval = new Centaur(getKind(element),
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case DjinnType:
			retval = new Djinn(getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case DragonType:
			retval = new Dragon(getKind(element),
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case FairyType:
			retval = new Fairy(getKind(element),
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case GiantType:
			retval = new Giant(getKind(element),
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case GriffinType:
			retval = new Griffin(
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case MinotaurType:
			retval = new Minotaur(getOrGenerateID(element, warner,
					idFactory), getFile(stream));
			break;
		case OgreType:
			retval = new Ogre(getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case PhoenixType:
			retval = new Phoenix(
					getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case SimurghType:
			retval = new Simurgh(getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case SphinxType:
			retval = new Sphinx(getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		case TrollType:
			retval = new Troll(getOrGenerateID(element, warner, idFactory),
					getFile(stream));
			break;
		default:
			throw new IllegalArgumentException("Shouldn't get here");
		}
		spinUntilEnd(element.getName(), stream);
		return retval;
	}
	/**
	 * @param element the current tag
	 * @return the value of its 'kind' parameter
	 * @throws SPFormatException on SP format error---if the parameter is missing, e.g.
	 */
	private String getKind(final StartElement element) throws SPFormatException {
		return getParameter(element, "kind");
	}
	/**
	 * Create an animal.
	 * @param element the tag we're reading
	 * @param id the ID number to give it
	 * @param file the file it was loaded from
	 * @return the parsed animal
	 * @throws SPFormatException on SP format error
	 */
	private Animal createAnimal(final StartElement element, final int id, // NOPMD
			final String file) throws SPFormatException {
		return new Animal(getKind(element), hasParameter(element,
				"traces"), Boolean.parseBoolean(getParameter(element,
				"talking", "false")), id, file);
	}
}

