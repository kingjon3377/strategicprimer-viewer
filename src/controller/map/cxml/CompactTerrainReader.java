package controller.map.cxml;

import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import model.map.HasImage;
import model.map.HasMutableImage;
import model.map.IMutablePlayerCollection;
import model.map.TerrainFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import util.LineEnd;
import util.NullCleaner;
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
 * @deprecated CompactXML is deprecated in favor of FluidXML
 */
@SuppressWarnings("ClassHasNoToStringMethod")
@Deprecated
public final class CompactTerrainReader extends
		AbstractCompactReader<TerrainFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactReader<TerrainFixture> READER = new CompactTerrainReader();
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS = NullCleaner.assertNotNull(
			Collections.unmodifiableSet(new HashSet<>(Arrays.asList("forest",
					"hill", "mountain", "oasis", "sandbar"))));

	/**
	 * Singleton.
	 */
	private CompactTerrainReader() {
		// Singleton.
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
	 * @param parent	the parent tag
	 *@param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @param stream    the stream to read more elements from     @return the parsed tile
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public TerrainFixture read(final StartElement element,
							   final QName parent, final IMutablePlayerCollection players,
							   final Warning warner, final IDRegistrar idFactory,
							   final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "forest", "hill", "mountain", "oasis", "sandbar");
		final TerrainFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "forest":
			retval = new Forest(getParameter(element, "kind"), hasParameter(
					element, "rows"), getIntegerParameter(element, "id", -1));
			break;
		case "hill":
			retval = new Hill(getOrGenerateID(element, warner, idFactory));
			break;
		case "mountain":
			retval = new Mountain();
			break;
		case "oasis":
			retval = new Oasis(getOrGenerateID(element, warner, idFactory));
			break;
		case "sandbar":
			retval = new Sandbar(getOrGenerateID(element, warner, idFactory));
			break;
		default:
			throw new IllegalArgumentException("Unhandled terrain fixture tag " +
													element.getName().getLocalPart());
		}
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
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
			ostream.append(imageXML((Mountain) obj)).append(" />");
			ostream.append(LineEnd.LINE_SEP);
			return; // Mountains don't yet have IDs.
		} else if (obj instanceof Forest) {
			writeTag(ostream, "forest", indent);
			ostream.append(" kind=\"");
			ostream.append(((Forest) obj).getKind());
			if (((Forest) obj).isRows()) {
				ostream.append("\" rows=\"true");
			}
			ostream.append('"');
		} else if (obj instanceof Hill) {
			writeTag(ostream, "hill", indent);
		} else if (obj instanceof Oasis) {
			writeTag(ostream, "oasis", indent);
		} else if (obj instanceof Sandbar) {
			writeTag(ostream, "sandbar", indent);
		} else {
			throw new IllegalStateException("Unexpected TerrainFixture type.");
		}
		ostream.append(imageXML((HasImage) obj));
		ostream.append(" id=\"");
		ostream.append(Integer.toString(obj.getID()));
		ostream.append("\" />");
		ostream.append(LineEnd.LINE_SEP);
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
