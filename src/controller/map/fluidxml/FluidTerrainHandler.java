package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.IMutablePlayerCollection;
import model.map.River;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import util.LineEnd;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttrWithDeprecatedForm;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
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
public final class FluidTerrainHandler {
	/**
	 * Do not instantiate.
	 */
	private FluidTerrainHandler() {
		// Do not instantiate
	}
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
	@SuppressWarnings("UnusedParameters")
	public static Ground readGround(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players, final Warning warner,
									final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, "ground");
		final String kind = getAttrWithDeprecatedForm(element, "kind",
				"ground", warner);
		requireNonEmptyAttribute(element, "exposed", true, warner);
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Ground(kind, parseBoolean(getAttribute(element, "exposed"))),
				element, warner);
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
	@SuppressWarnings("UnusedParameters")
	public static Forest readForest(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players, final Warning warner,
									final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, "forest");
		final Forest retval =
				new Forest(getAttribute(element, "kind"), hasAttribute(element, "rows"));
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(retval, element, warner);
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
	@SuppressWarnings("UnusedParameters")
	public static Mountain readMountain(final StartElement element,
										final Iterable<XMLEvent> stream,
										final IMutablePlayerCollection players, final Warning warner,
										final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, "mountain");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Mountain(), element, warner);
	}

	/**
	 * Write Ground to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an instance of Ground
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeGround(final Appendable ostream, final Object obj,
								   final int indent) throws IOException {
		if (!(obj instanceof Ground)) {
			throw new IllegalArgumentException("Can only write Ground");
		}
		final Ground grd = (Ground) obj;
		writeTag(ostream, "ground", indent);
		writeAttribute(ostream, "kind", grd.getKind());
		writeBooleanAttribute(ostream, "exposed", grd.isExposed());
		writeImage(ostream, grd);
		ostream.append(" />");
		ostream.append(LineEnd.LINE_SEP);
	}
	/**
	 * Write a Mountain to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an instance of Mountain
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeMountain(final Appendable ostream, final Object obj,
									 final int indent) throws IOException {
		if (!(obj instanceof Mountain)) {
			throw new IllegalArgumentException("Can only write Mountain");
		}
		writeTag(ostream, "mountain", indent);
		writeImage(ostream, (HasImage) obj);
		ostream.append(" />");
		ostream.append(LineEnd.LINE_SEP);
	}
	/**
	 * Write a Forest to a stream.
	 *
	 * @param ostream The stream to write to.
	 * @param obj     The object to write. Must be an instance of Forest
	 * @param indent  The current indentation level.
	 * @throws IOException on I/O error
	 */
	public static void writeForest(final Appendable ostream, final Object obj,
								   final int indent) throws IOException {
		if (!(obj instanceof Forest)) {
			throw new IllegalArgumentException("Can only write Forest");
		}
		final Forest forest = (Forest) obj;
		writeTag(ostream, "forest", indent);
		writeAttribute(ostream, "kind", forest.getKind());
		if (forest.isRows()) {
			writeBooleanAttribute(ostream, "rows", true);
		}
		writeImage(ostream, forest);
		ostream.append(" />");
		ostream.append(LineEnd.LINE_SEP);
	}
	/**
	 * Parse a river.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings({"SameReturnValue", "UnusedParameters"})
	public static River readLake(final StartElement element,
								 final Iterable<XMLEvent> stream,
								 final IMutablePlayerCollection players,
								 final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "lake");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return River.Lake;
	}
	/**
	 * Parse a river.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static River readRiver(final StartElement element,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "river");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return River.getRiver(getAttribute(element, "direction"));
	}
	/**
	 * Write a river, or a collection of rivers.
	 *
	 * @param ostream the stream we're writing to
	 * @param obj     the river to write. Must be a River or a RiverFixture.
	 * @param indent  the indentation level
	 * @throws IOException on I/O error
	 */
	public static void writeRivers(final Appendable ostream, final Object obj,
								   final int indent) throws IOException {
		if (River.Lake == obj) {
			writeTag(ostream, "lake", indent);
		} else if (obj instanceof River) {
			writeTag(ostream, "river", indent);
			writeAttribute(ostream, "direction", ((River) obj).getDescription());
		} else if (obj instanceof RiverFixture) {
			// TODO: Test
			for (final River river : (RiverFixture) obj) {
				writeRivers(ostream, river, indent);
			}
			return;
		} else {
			throw new IllegalArgumentException("Can only write River or RiverFixture");
		}
		ostream.append(" />");
		ostream.append(LineEnd.LINE_SEP);
	}

}
