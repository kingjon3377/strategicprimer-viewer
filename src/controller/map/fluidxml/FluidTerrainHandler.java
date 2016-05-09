package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttrWithDeprecatedForm;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.imageXML;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;
import static java.lang.Boolean.parseBoolean;
import static util.NullCleaner.assertNotNull;

/**
 * A class to hold XML I/O for terrain fixtures.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
public class FluidTerrainHandler {
	/**
	 * Read Ground from XML.
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed Ground
	 * @throws SPFormatException on SP format problems
	 */
	public static final Ground readGround(final StartElement element,
					   final Iterable<XMLEvent> stream,
					   final IMutablePlayerCollection players, final Warning warner,
					   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "ground");
		final String kind = getAttrWithDeprecatedForm(element, "kind",
				"ground", warner);
		requireNonEmptyAttribute(element, "exposed", true, warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final Ground retval =
				new Ground(kind, parseBoolean(getAttribute(element, "exposed")));
		retval.setImage(getAttribute(element, "image", ""));
		return retval;
	}
	/**
	 * Read a Forest from XML.
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed Forest
	 * @throws SPFormatException on SP format problems
	 */
	public static final Forest readForest(final StartElement element,
										   final Iterable<XMLEvent> stream,
										   final IMutablePlayerCollection players, final Warning warner,
										   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "forest");
		final Forest retval =
				new Forest(getAttribute(element, "kind"), hasAttribute(element, "rows"));
		spinUntilEnd(assertNotNull(element.getName()), stream);
		retval.setImage(getAttribute(element, "image", ""));
		return retval;
	}

	/**
	 * Read a Mountain from XML.
	 * @param element   the XML element to parse
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed Mountain
	 * @throws SPFormatException on SP format problems
	 */
	public static final Mountain readMountain(final StartElement element,
										   final Iterable<XMLEvent> stream,
										   final IMutablePlayerCollection players, final Warning warner,
										   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "mountain");
		final Mountain retval = new Mountain();
		spinUntilEnd(assertNotNull(element.getName()), stream);
		retval.setImage(getAttribute(element, "image", ""));
		return retval;
	}

	/**
	 * Write Ground to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an instance of Ground
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static final void writeGround(final Appendable ostream, final Object obj, final int indent)
			throws IOException {
		if (!(obj instanceof Ground)) {
			throw new IllegalArgumentException("Can only write Ground");
		}
		final Ground grd = (Ground) obj;
		writeTag(ostream, "ground", indent);
		writeAttribute(ostream, "kind", grd.getKind());
		writeBooleanAttribute(ostream, "exposed", grd.isExposed());
		ostream.append(imageXML(grd));
		ostream.append(" />\n");
	}
	/**
	 * Write a Mountain to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an instance of Mountain
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static final void writeMountain(final Appendable ostream, final Object obj, final int indent) throws IOException {
		if (!(obj instanceof Mountain)) {
			throw new IllegalArgumentException("Can only write Mountain");
		}
		writeTag(ostream, "mountain", indent);
		ostream.append(imageXML((Mountain) obj));
		ostream.append(" />\n");
	}
	/**
	 * Write a Forest to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an instance of Forest
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static final void writeForest(final Appendable ostream, final Object obj, final int indent) throws IOException {
		if (!(obj instanceof Forest)) {
			throw new IllegalArgumentException("Can only write Forest");
		}
		final Forest forest = (Forest) obj;
		writeTag(ostream, "forest", indent);
		writeAttribute(ostream, "kind", forest.getKind());
		if (forest.isRows()) {
			writeBooleanAttribute(ostream, "rows", true);
		}
		ostream.append(imageXML(forest));
		ostream.append(" />\n");
	}

}
