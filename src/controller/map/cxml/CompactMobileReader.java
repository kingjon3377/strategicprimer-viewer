package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
import model.map.fixtures.mobile.Unit;
import org.eclipse.jdt.annotation.NonNull;
import util.LineEnd;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for tiles, including rivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated CompactXML is deprecated in favor of FluidXML
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
public final class CompactMobileReader extends
		AbstractCompactReader<@NonNull MobileFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<@NonNull MobileFixture> READER =
			new CompactMobileReader();
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS;
	/**
	 * Map from types to tags.
	 */
	private static final Map<Class<? extends MobileFixture>, String> TAG_MAP;

	static {
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
		SUPP_TAGS = NullCleaner.assertNotNull(
				Collections.unmodifiableSet(new HashSet<>(TAG_MAP.values())));
	}

	/**
	 * Singleton.
	 */
	private CompactMobileReader() {
		// Singleton.
	}

	/**
	 * @param element the current tag
	 * @return the value of its 'kind' parameter
	 * @throws SPFormatException on SP format error---if the parameter is missing, e.g.
	 */
	private static String getKind(final StartElement element)
			throws SPFormatException {
		return getParameter(element, "kind");
	}

	/**
	 * Create an animal.
	 *
	 * @param element the tag we're reading
	 * @param idNum   the ID number to give it
	 * @return the parsed animal
	 * @throws SPFormatException on SP format error
	 */
	private static MobileFixture createAnimal(final StartElement element,
											  final int idNum) throws SPFormatException {
		return new Animal(
								 getKind(element),
								 hasParameter(element, "traces"),
								 Boolean.parseBoolean(
										 getParameter(element, "talking", "false")),
								 getParameter(element, "status", "wild"), idNum);
	}

	/**
	 * @param tag   the tag being read
	 * @param idNum the ID # to give the fixture
	 * @return the thing being read
	 */
	private static MobileFixture readSimple(final String tag, final int idNum) {
		switch (tag) {
		case "djinn":
			return new Djinn(idNum);
		case "griffin":
			return new Griffin(idNum);
		case "minotaur":
			return new Minotaur(idNum);
		case "ogre":
			return new Ogre(idNum);
		case "phoenix":
			return new Phoenix(idNum);
		case "simurgh":
			return new Simurgh(idNum);
		case "sphinx":
			return new Sphinx(idNum);
		case "troll":
			return new Troll(idNum);
		default:
			throw new IllegalArgumentException("Unhandled mobile tag " + tag);
		}
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
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public MobileFixture read(final StartElement element,
							  final QName parent, final IMutablePlayerCollection players,
							  final Warning warner, final IDRegistrar idFactory,
							  final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "animal", "centaur", "djinn", "dragon", "fairy",
				"giant", "griffin", "minotaur", "ogre", "phoenix", "simurgh",
				"sphinx", "troll", "unit");
		final MobileFixture retval;
		final String type = element.getName().getLocalPart().toLowerCase();
		switch (type) {
		case "unit":
			return CompactUnitReader.READER.read(element, parent, players,
					warner, idFactory, stream);
		case "animal":
			retval = createAnimal(element,
					getOrGenerateID(element, warner, idFactory));
			break;
		case "centaur":
			retval = new Centaur(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		case "dragon":
			retval = new Dragon(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		case "fairy":
			retval = new Fairy(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		case "giant":
			retval = new Giant(getKind(element), getOrGenerateID(element,
					warner, idFactory));
			break;
		default:
			retval = readSimple(
					NullCleaner.assertNotNull(element.getName().getLocalPart()),
					getOrGenerateID(element, warner, idFactory));
			break;
		}
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		if (retval instanceof HasMutableImage) {
			((HasMutableImage) retval).setImage(getParameter(element, "image", ""));
		}
		return retval;
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write.
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final MobileFixture obj,
					  final int indent) throws IOException {
		if (obj instanceof IUnit) {
			CompactUnitReader.READER.write(ostream, (IUnit) obj, indent);
		} else if (obj instanceof Animal) {
			writeTag(ostream, "animal", indent);
			final Animal animal = (Animal) obj;
			ostream.append(" kind=\"");
			ostream.append(animal.getKind());
			if (animal.isTraces()) {
				ostream.append("\" traces=\"");
			}
			if (animal.isTalking()) {
				ostream.append("\" talking=\"true");
			}
			if (!"wild".equals(animal.getStatus())) {
				ostream.append("\" status=\"");
				ostream.append(animal.getStatus());
			}
			ostream.append("\" id=\"");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append('"').append(imageXML(animal)).append(" />");
			ostream.append(LineEnd.LINE_SEP);
		} else {
			writeTag(ostream, NullCleaner.assertNotNull(TAG_MAP.get(obj.getClass())),
					indent);
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
			ostream.append(" />");
			ostream.append(LineEnd.LINE_SEP);
		}
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return (obj instanceof MobileFixture) && !(obj instanceof Unit);
	}
}
