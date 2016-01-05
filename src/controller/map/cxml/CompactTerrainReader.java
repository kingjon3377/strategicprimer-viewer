package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.IMutablePlayerCollection;
import model.map.TerrainFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.terrain.Sandbar;
import util.IteratorWrapper;
import util.NullCleaner;
import util.Warning;

/**
 * A reader for TerrainFixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class CompactTerrainReader extends
		AbstractCompactReader<TerrainFixture> {
	/**
	 * Singleton object.
	 */
	public static final CompactTerrainReader READER = new CompactTerrainReader();
	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList("forest", "hill", "mountain", "oasis",
					"sandbar")));

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
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed tile
	 * @throws SPFormatException on SP format problem
	 */
	@Override
	public TerrainFixture read(final StartElement element,
	                           final IteratorWrapper<XMLEvent> stream,
	                           final IMutablePlayerCollection players,
	                           final Warning warner,
	                           final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "forest", "hill", "mountain", "oasis", "sandbar");
		final TerrainFixture retval; // NOPMD
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "forest":
			retval = new Forest(getParameter(element, "kind"), hasParameter(
					element, "rows"));
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
		((HasImage) retval).setImage(getParameter(element, "image", ""));
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
		indent(ostream, indent);
		if (obj instanceof Mountain) {
			ostream.append("<mountain").append(imageXML((Mountain) obj))
					.append(" />\n");
			return; // NOPMD Mountains don't yet have IDs.
		} else {
			if (obj instanceof Forest) {
				ostream.append("<forest kind=\"");
				ostream.append(((Forest) obj).getKind());
				if (((Forest) obj).isRows()) {
					ostream.append("\" rows=\"true");
				}
				ostream.append('"').append(imageXML((Forest) obj)).append(" />\n");
				return; // NOPMD Neither do Forests.
			} else {
				if (obj instanceof Hill) {
					ostream.append("<hill");
					ostream.append(imageXML((Hill) obj));
				} else if (obj instanceof Oasis) {
					ostream.append("<oasis");
					ostream.append(imageXML((Oasis) obj));
				} else if (obj instanceof Sandbar) {
					ostream.append("<sandbar");
					ostream.append(imageXML((Sandbar) obj));
				} else {
					throw new IllegalStateException("Unexpected TerrainFixture type.");
				}
				ostream.append(" id=\"");
				ostream.append(Integer.toString(obj.getID()));
				ostream.append("\" />\n");
			}
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactTerrainReader";
	}
	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	public boolean canWrite(final Object obj) {
		return obj instanceof TerrainFixture;
	}
}
