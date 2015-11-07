package controller.map.cxml;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.HasImage;
import model.map.HasKind;
import model.map.IMutablePlayerCollection;
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
import model.map.fixtures.mobile.Unit;
import util.ArraySet;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class CompactMobileReader extends
		AbstractCompactReader<MobileFixture> {
	/**
	 * Mapping from tags to enum-tags.
	 */
	private static final Map<String, MobileType> MAP = new HashMap<>(
			MobileType.values().length);
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS;
	/**
	 * Map from types to tags.
	 *
	 * FIXME: This is brittle and doesn't work well with extensible classes.
	 */
	private static final Map<Class<? extends MobileFixture>, String> TAG_MAP;
	/**
	 * Singleton object.
	 */
	public static final CompactMobileReader READER = new CompactMobileReader();

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
		 * Sphinx.
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
		 *
		 * @param tagString The tag.
		 */
		private MobileType(final String tagString) {
			tag = tagString;
		}
	}

	static {
		final Set<String> suppTagsTemp = new ArraySet<>();
		for (final MobileType mtype : MobileType.values()) {
			MAP.put(mtype.tag, mtype);
			suppTagsTemp.add(mtype.tag);
		}
		SUPP_TAGS = Collections.unmodifiableSet(suppTagsTemp);
		TAG_MAP = new HashMap<>();
		TAG_MAP.put(Animal.class, "animal");
		TAG_MAP.put(Centaur.class, "centaur");
		TAG_MAP.put(Djinn.class, "djinn");
		TAG_MAP.put(Dragon.class, "dragon");
		TAG_MAP.put(Fairy.class, "fairy");
		TAG_MAP.put(Giant.class, "giant");
		TAG_MAP.put(Griffin.class, "griffin");
		TAG_MAP.put(Minotaur.class, "minotaur");
		TAG_MAP.put(Ogre.class, "ogre");
		TAG_MAP.put(Phoenix.class, "phoenix");
		TAG_MAP.put(Simurgh.class, "simurgh");
		TAG_MAP.put(Sphinx.class, "sphinx");
		TAG_MAP.put(Troll.class, "troll");
		TAG_MAP.put(Unit.class, "unit");
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
			final IteratorWrapper<XMLEvent> stream,
			final IMutablePlayerCollection players, final Warning warner,
			final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "animal", "centaur", "djinn", "dragon", "fairy",
				"giant", "griffin", "minotaur", "ogre", "phoenix", "simurgh",
				"sphinx", "troll", "unit");
		// ESCA-JAVA0177:
		final MobileFixture retval; // NOPMD
		final MobileType type = MAP.get(element.getName().getLocalPart());
		if (type == null) {
			throw new IllegalStateException("Tag turned into a null type.");
		}
		switch (type) {
		case UnitType:
			return CompactUnitReader.READER.read(element, stream, players, // NOPMD
					warner, idFactory);
		case AnimalType:
			retval = createAnimal(element,
					getOrGenerateID(element, warner, idFactory));
			break;
		case CentaurType:
			retval = new Centaur(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		case DragonType:
			retval = new Dragon(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		case FairyType:
			retval = new Fairy(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		case GiantType:
			retval = new Giant(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		default:
			retval = readSimple(type,
					getOrGenerateID(element, warner, idFactory));
			break;
		}
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		if (retval instanceof HasImage) {
			((HasImage) retval).setImage(getParameter(element, "image", ""));
		}
		return retval;
	}

	/**
	 * @param element the current tag
	 * @return the value of its 'kind' parameter
	 * @throws SPFormatException on SP format error---if the parameter is
	 *         missing, e.g.
	 */
	private static String getKind(final StartElement element)
			throws SPFormatException {
		return getParameter(element, "kind");
	}

	/**
	 * Create an animal.
	 *
	 * @param element the tag we're reading
	 * @param idNum the ID number to give it
	 * @return the parsed animal
	 * @throws SPFormatException on SP format error
	 */
	private static Animal createAnimal(final StartElement element,
			final int idNum) throws SPFormatException {
		return new Animal(
				getKind(element),
				hasParameter(element, "traces"),
				Boolean.parseBoolean(getParameter(element, "talking", "false")),
				getParameter(element, "status", "wild"), idNum);
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj The object to write.
	 * @param indent The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final MobileFixture obj,
			final int indent) throws IOException {
		if (obj instanceof Unit) {
			CompactUnitReader.READER.write(ostream, (Unit) obj, indent);
		} else if (obj instanceof Animal) {
			ostream.append(indent(indent));
			ostream.append("<animal kind=\"");
			ostream.append(((Animal) obj).getKind());
			if (((Animal) obj).isTraces()) {
				ostream.append("\" traces=\"");
			}
			if (((Animal) obj).isTalking()) {
				ostream.append("\" talking=\"true");
			}
			if (!"wild".equals(((Animal) obj).getStatus())) {
				ostream.append("\" status=\"");
				ostream.append(((Animal) obj).getStatus());
			}
			ostream.append("\" id=\"");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append('"').append(imageXML((Animal) obj)).append(" />\n");
		} else {
			ostream.append(indent(indent));
			ostream.append('<');
			ostream.append(TAG_MAP.get(obj.getClass()));
			if (obj instanceof HasKind) {
				ostream.append(" kind=\"");
				ostream.append(((HasKind) obj).getKind());
				ostream.append('"');
			}
			ostream.append(" id=\"");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append('"');
			if (obj instanceof HasImage) {
				ostream.append(imageXML((HasImage) obj));
			}
			ostream.append(" />\n");
		}
	}

	/**
	 * This is part of the switch statement in read() split off to reduce
	 * calculated complexity.
	 *
	 * @param type the type being read
	 * @param idNum the ID # to give it.
	 * @return the thing being read.
	 */
	private static MobileFixture readSimple(final MobileType type, final int idNum) {
		final MobileFixture retval; // NOPMD
		switch (type) {
		case DjinnType:
			retval = new Djinn(idNum);
			break;
		case GriffinType:
			retval = new Griffin(idNum);
			break;
		case MinotaurType:
			retval = new Minotaur(idNum);
			break;
		case OgreType:
			retval = new Ogre(idNum);
			break;
		case PhoenixType:
			retval = new Phoenix(idNum);
			break;
		case SimurghType:
			retval = new Simurgh(idNum);
			break;
		case SphinxType:
			retval = new Sphinx(idNum);
			break;
		case TrollType:
			retval = new Troll(idNum);
			break;
		default:
			throw new IllegalArgumentException("Shouldn't get here");
		}
		return retval;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactMobileReader";
	}
}
