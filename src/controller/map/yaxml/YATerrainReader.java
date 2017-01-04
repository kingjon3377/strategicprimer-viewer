package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.HasMutableImage;
import model.map.TerrainFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import util.Warning;

/**
 * A reader for TerrainFixtures.
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
@SuppressWarnings("ClassHasNoToStringMethod")
public final class YATerrainReader extends YAAbstractReader<TerrainFixture> {
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList("forest", "hill", "mountain", "oasis",
					"sandbar")));

	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YATerrainReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
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
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public TerrainFixture read(final StartElement element, final QName parent,
							   final Iterable<XMLEvent> stream) throws
			SPFormatException {
		requireTag(element, parent, "forest", "hill", "mountain", "oasis", "sandbar");
		final TerrainFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "forest":
			final int id = getIntegerParameter(element, "id", -1);
			if (id >= 0) {
				registerID(id);
			}
			retval = new Forest(getParameter(element, "kind"), hasParameter(
					element, "rows"), id);
			break;
		case "hill":
			retval = new Hill(getOrGenerateID(element));
			break;
		case "mountain":
			retval = new Mountain();
			break;
		case "oasis":
			retval = new Oasis(getOrGenerateID(element));
			break;
		case "sandbar":
			retval = new Sandbar(getOrGenerateID(element));
			break;
		default:
			throw new IllegalArgumentException("Unhandled terrain fixture tag " +
													   element.getName().getLocalPart());
		}
		spinUntilEnd(element.getName(), stream);
		((HasMutableImage) retval).setImage(getParameter(element, "image", ""));
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
	public void write(final Appendable ostream, final TerrainFixture obj,
					  final int indent) throws IOException {
		if (obj instanceof Mountain) {
			writeTag(ostream, "mountain", indent);
			writeImageXML(ostream, (Mountain) obj);
			closeLeafTag(ostream);
			return; // Mountains don't yet have IDs.
		} else if (obj instanceof Forest) {
			writeTag(ostream, "forest", indent);
			final Forest forest = (Forest) obj;
			writeProperty(ostream, "kind", forest.getKind());
			if (forest.isRows()) {
				writeProperty(ostream, "rows", "true");
			}
		} else if (obj instanceof Hill) {
			writeTag(ostream, "hill", indent);
		} else if (obj instanceof Oasis) {
			writeTag(ostream, "oasis", indent);
		} else if (obj instanceof Sandbar) {
			writeTag(ostream, "sandbar", indent);
		} else {
			throw new IllegalStateException("Unexpected TerrainFixture type.");
		}
		writeImageXML(ostream, (HasImage) obj);
		writeProperty(ostream, "id", Integer.toString(obj.getID()));
		closeLeafTag(ostream);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof TerrainFixture;
	}
}
