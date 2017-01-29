package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasMutableImage;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.SimpleImmortal;
import model.map.fixtures.mobile.SimpleImmortal.SimpleImmortalKind;
import org.eclipse.jdt.annotation.NonNull;
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
 */
public final class YAMobileReader extends
		YAAbstractReader<@NonNull MobileFixture> {
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS;
	/**
	 * Map from types to tags.
	 */
	private static final Map<Class<? extends MobileFixture>, String> TAG_MAP;

	/**
	 * Constructor.
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAMobileReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}
	static {
		TAG_MAP = new HashMap<>();
		TAG_MAP.put(Animal.class, "animal");
		TAG_MAP.put(Centaur.class, "centaur");
		TAG_MAP.put(Dragon.class, "dragon");
		TAG_MAP.put(Fairy.class, "fairy");
		TAG_MAP.put(Giant.class, "giant");
		SUPP_TAGS = Stream.concat(TAG_MAP.values().stream(),
				Stream.of(SimpleImmortal.SimpleImmortalKind.values())
						.map(SimpleImmortal.SimpleImmortalKind::getTag))
							.collect(Collectors.toSet());
	}
	/**
	 * Get the value of the "kind" tag.
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
	 * @return the parsed animal
	 * @throws SPFormatException on SP format error
	 */
	private MobileFixture createAnimal(final StartElement element)
			throws SPFormatException {
		final boolean tracks = hasParameter(element, "traces");
		final int idNum;
		if (tracks && !hasParameter(element, "id")) {
			idNum = -1;
		} else {
			idNum = getOrGenerateID(element);
		}
		return new Animal(getKind(element), tracks,
								 Boolean.parseBoolean(
										 getParameter(element, "talking", "false")),
								 getParameter(element, "status", "wild"), idNum);
	}

	/**
	 * Read a fixture that only has ID (and possibly image filename), no other state to
	 * read.
	 * @param tag   the tag being read
	 * @param idNum the ID # to give the fixture
	 * @return the thing being read
	 */
	private static MobileFixture readSimple(final String tag, final int idNum) {
		return new SimpleImmortal(SimpleImmortalKind.parse(tag), idNum);
	}

	/**
	 * Whether the tag is one we support.
	 * @param tag a tag
	 * @return whether we support it
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return SUPP_TAGS.contains(tag);
	}

	/**
	 * Read an object from XML.
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public MobileFixture read(final StartElement element,
							  final QName parent,
							  final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "animal", "centaur", "djinn", "dragon", "fairy",
				"giant", "griffin", "minotaur", "ogre", "phoenix", "simurgh",
				"sphinx", "troll");
		final MobileFixture retval;
		final String type = element.getName().getLocalPart().toLowerCase();
		switch (type) {
		case "animal":
			retval = createAnimal(element);
			break;
		case "centaur":
			retval = new Centaur(getKind(element), getOrGenerateID(element));
			break;
		case "dragon":
			retval = new Dragon(getKind(element), getOrGenerateID(element));
			break;
		case "fairy":
			retval = new Fairy(getKind(element), getOrGenerateID(element));
			break;
		case "giant":
			retval = new Giant(getKind(element), getOrGenerateID(element));
			break;
		default:
			retval = readSimple(element.getName().getLocalPart(),
					getOrGenerateID(element));
			break;
		}
		spinUntilEnd(element.getName(), stream);
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
			throw new IllegalArgumentException("Unit handled elsewhere");
		} else if (obj instanceof Animal) {
			writeTag(ostream, "animal", indent);
			final Animal animal = (Animal) obj;
			writeProperty(ostream, "kind", animal.getKind());
			if (animal.isTraces()) {
				writeProperty(ostream, "traces", "");
			}
			if (animal.isTalking()) {
				writeProperty(ostream, "talking", "true");
			}
			if (!"wild".equals(animal.getStatus())) {
				writeProperty(ostream, "status", animal.getStatus());
			}
			if (!animal.isTraces()) {
				writeProperty(ostream, "id", obj.getID());
			}
			writeImageXML(ostream, (Animal) obj);
			closeLeafTag(ostream);
		} else if (obj instanceof SimpleImmortal) {
			writeTag(ostream, ((SimpleImmortal) obj).getKind(), indent);
			writeProperty(ostream, "id", obj.getID());
			writeImageXML(ostream, (HasImage) obj);
			closeLeafTag(ostream);
		} else if (TAG_MAP.containsKey(obj.getClass())) {
			writeTag(ostream, TAG_MAP.get(obj.getClass()), indent);
			if (obj instanceof HasKind) {
				writeProperty(ostream, "kind", ((HasKind) obj).getKind());
			}
			writeProperty(ostream, "id", obj.getID());
			if (obj instanceof HasImage) {
				writeImageXML(ostream, (HasImage) obj);
			}
			closeLeafTag(ostream);
		} else {
			throw new IllegalArgumentException("No tag for " + obj.shortDesc());
		}
	}

	/**
	 * We can write any MobileFixture except units.
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return (obj instanceof MobileFixture) && !(obj instanceof IUnit);
	}
}
