package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.HasImage;
import model.map.IMutablePlayerCollection;
import model.map.River;
import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.createElement;
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
	 * Write Ground to XML.
	 *
	 * @param obj     The object to write. Must be an instance of Ground
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeGround(final Document document, final Node parent,
								   Object obj) {
		if (!(obj instanceof Ground)) {
			throw new IllegalArgumentException("Can only write Ground");
		}
		final Ground grd = (Ground) obj;
		final Element element = createElement(document, "ground");
		writeAttribute(element, "kind", grd.getKind());
		writeBooleanAttribute(element, "exposed", grd.isExposed());
		writeImage(element, grd);
		parent.appendChild(element);
	}
	/**
	 * Write a Mountain to XML.
	 *
	 * @param obj     The object to write. Must be an instance of Mountain
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMountain(final Document document, final Node parent,
									 Object obj) {
		if (!(obj instanceof Mountain)) {
			throw new IllegalArgumentException("Can only write Mountain");
		}
		final Element element = createElement(document, "mountain");
		writeImage(element, (HasImage) obj);
		parent.appendChild(element);
	}
	/**
	 * Write a Forest to XML.
	 *
	 * @param obj     The object to write. Must be an instance of Forest
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeForest(final Document document, final Node parent,
								   Object obj) {
		if (!(obj instanceof Forest)) {
			throw new IllegalArgumentException("Can only write Forest");
		}
		final Forest forest = (Forest) obj;
		final Element element = createElement(document, "forest");
		writeAttribute(element, "kind", forest.getKind());
		if (forest.isRows()) {
			writeBooleanAttribute(element, "rows", true);
		}
		writeImage(element, forest);
		parent.appendChild(element);
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
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @param obj The object being written.
	 */
	public static void writeRivers(final Document document, final Node parent, final Object obj) {
		if (River.Lake == obj) {
			parent.appendChild(createElement(document, "lake"));
		} else if (obj instanceof River) {
			final Element element = createElement(document, "river");
			writeAttribute(element, "direction", ((River) obj).getDescription());
			parent.appendChild(element);
		} else if (obj instanceof RiverFixture) {
			// TODO: Test
			for (final River river : (RiverFixture) obj) {
				writeRivers(document, parent, river);
			}
			return;
		} else {
			throw new IllegalArgumentException("Can only write River or RiverFixture");
		}
	}

}
