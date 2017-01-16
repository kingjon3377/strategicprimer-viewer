package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.IMutablePlayerCollection;
import model.map.River;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttrWithDeprecatedForm;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.requireNonEmptyAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.writeTag;
import static java.lang.Boolean.parseBoolean;

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
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 * @deprecated FluidXML is deprecated in favor of YAXML
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
@Deprecated
public final class FluidTerrainHandler {
	/**
	 * Do not instantiate.
	 */
	private FluidTerrainHandler() {
		// Do not instantiate
	}

	/**
	 * Read Ground from XML.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed Ground
	 * @throws SPFormatException on SP format problems
	 */
	public static Ground readGround(final StartElement element,
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner,
									final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "ground");
		final String kind = getAttrWithDeprecatedForm(element, "kind",
				"ground", warner);
		requireNonEmptyAttribute(element, "exposed", true, warner);
		spinUntilEnd(element.getName(), stream);
		return setImage(new Ground(kind, parseBoolean(getAttribute(element, "exposed"))),
				element, warner);
	}

	/**
	 * Read a Forest from XML.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed Forest
	 * @throws SPFormatException on SP format problems
	 */
	public static Forest readForest(final StartElement element,
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner,
									final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "forest");
		final int id = getIntegerAttribute(element, "id", -1);
		if (id >= 0) {
			idFactory.register(warner, id);
		}
		final Forest retval =
				new Forest(getAttribute(element, "kind"), hasAttribute(element, "rows"),
								  id);
		spinUntilEnd(element.getName(), stream);
		return setImage(retval, element, warner);
	}

	/**
	 * Read a Mountain from XML.
	 *
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the ID factory to use to generate IDs
	 * @return the parsed Mountain
	 * @throws SPFormatException on SP format problems
	 */
	public static Mountain readMountain(final StartElement element,
										final QName parent,
										final Iterable<XMLEvent> stream,
										final IMutablePlayerCollection players,
										final Warning warner,
										final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "mountain");
		spinUntilEnd(element.getName(), stream);
		return setImage(new Mountain(), element, warner);
	}

	/**
	 * Write Ground to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeGround(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (!(obj instanceof Ground)) {
			throw new IllegalArgumentException("Can only write Ground");
		}
		final Ground grd = (Ground) obj;
		writeTag(ostream, "ground", indent, true);
		writeAttribute(ostream, "kind", grd.getKind());
		writeBooleanAttribute(ostream, "exposed", grd.isExposed());
		writeImage(ostream, grd);
	}

	/**
	 * Write a Mountain to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMountain(final XMLStreamWriter ostream, final Object obj,
									 final int indent) throws XMLStreamException {
		if (!(obj instanceof Mountain)) {
			throw new IllegalArgumentException("Can only write Mountain");
		}
		writeTag(ostream, "mountain", indent, true);
		writeImage(ostream, (HasImage) obj);
	}

	/**
	 * Write a Forest to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeForest(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (!(obj instanceof Forest)) {
			throw new IllegalArgumentException("Can only write Forest");
		}
		final Forest forest = (Forest) obj;
		writeTag(ostream, "forest", indent, true);
		writeAttribute(ostream, "kind", forest.getKind());
		if (forest.isRows()) {
			writeBooleanAttribute(ostream, "rows", true);
		}
		writeIntegerAttribute(ostream, "id", forest.getID());
		writeImage(ostream, forest);
	}

	/**
	 * Parse a river.
	 *
	 * @param element   the element to read from
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("SameReturnValue")
	public static River readLake(final StartElement element,
								 final QName parent,
								 final Iterable<XMLEvent> stream,
								 final IMutablePlayerCollection players,
								 final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "lake");
		spinUntilEnd(element.getName(), stream);
		return River.Lake;
	}

	/**
	 * Parse a river.
	 *
	 * @param element   the element to read from
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the river represented by the element
	 * @throws SPFormatException on SP format error
	 */
	public static River readRiver(final StartElement element,
								  final QName parent,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "river");
		spinUntilEnd(element.getName(), stream);
		return River.getRiver(getAttribute(element, "direction"));
	}

	/**
	 * Write a river, or a collection of rivers.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException on XML-creation problem
	 */
	public static void writeRivers(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (River.Lake == obj) {
			writeTag(ostream, "lake", indent, true);
		} else if (obj instanceof River) {
			writeTag(ostream, "river", indent, true);
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
	}

}
