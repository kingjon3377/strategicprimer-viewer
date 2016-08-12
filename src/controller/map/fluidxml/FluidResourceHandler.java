package controller.map.fluidxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDRegistrar;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.getAttrWithDeprecatedForm;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
import static controller.map.fluidxml.XMLHelper.writeImage;
import static controller.map.fluidxml.XMLHelper.writeIntegerAttribute;
import static java.lang.Boolean.parseBoolean;
import static model.map.fixtures.resources.StoneKind.parseStoneKind;
import static model.map.fixtures.towns.TownStatus.parseTownStatus;
import static util.NullCleaner.assertNotNull;

/**
 * A class to hold XML I/O for "resource" fixtures, including "harvestable" fixtures
 * and implements and resource piles.
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
public final class FluidResourceHandler {
	/**
	 * Do not instantiate.
	 */
	private FluidResourceHandler() {
		// Do not instantiate
	}
	/**
	 * Parse a resource pile.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the resource pile represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static ResourcePile readResource(final StartElement element,
											final Iterable<XMLEvent> stream,
											final IMutablePlayerCollection players,
											final Warning warner,
											final IDRegistrar idFactory) throws SPFormatException {
		requireTag(element, "resource");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final ResourcePile retval =
				new ResourcePile(getOrGenerateID(element, warner, idFactory),
										getAttribute(element, "kind"),
										getAttribute(element, "contents"),
										getIntegerAttribute(element, "quantity"),
										getAttribute(element, "unit", ""));
		if (hasAttribute(element, "created")) {
			retval.setCreated(getIntegerAttribute(element, "created"));
		}
		return setImage(retval, element, warner);
	}
	/**
	 * Parse a cache.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the cache represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static CacheFixture readCache(final StartElement element,
										 final Iterable<XMLEvent> stream,
										 final IMutablePlayerCollection players,
										 final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "cache");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new CacheFixture(getAttribute(element, "kind"),
												getAttribute(element, "contents"),
												getOrGenerateID(element, warner,
														idFactory)), element, warner);
	}
	/**
	 * Parse a grove.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static Grove readGrove(final StartElement element,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "grove");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final boolean cultivated;
		if (hasAttribute(element, "cultivated")) {
			cultivated = parseBoolean(getAttribute(element, "cultivated"));
		} else if (hasAttribute(element, "wild")) {
			warner.warn(new DeprecatedPropertyException(element, "wild", "cultivated"));
			cultivated = !parseBoolean(getAttribute(element, "wild"));
		} else {
			throw new MissingPropertyException(element, "cultivated");
		}
		return setImage(new Grove(false, cultivated,
										 getAttrWithDeprecatedForm(element, "kind",
												 "tree", warner),
										 getOrGenerateID(element, warner, idFactory)),
				element, warner);
	}
	/**
	 * Parse an orchard.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the grove represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static Grove readOrchard(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "orchard");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final boolean cultivated;
		if (hasAttribute(element, "cultivated")) {
			cultivated = parseBoolean(getAttribute(element, "cultivated"));
		} else if (hasAttribute(element, "wild")) {
			warner.warn(new DeprecatedPropertyException(element, "wild", "cultivated"));
			cultivated = !parseBoolean(getAttribute(element, "wild"));
		} else {
			throw new MissingPropertyException(element, "cultivated");
		}
		return setImage(new Grove(true, cultivated,
										 getAttrWithDeprecatedForm(element, "kind",
												 "tree", warner),
										 getOrGenerateID(element, warner, idFactory)),
				element, warner);
	}
	/**
	 * Parse a meadow.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the meadow represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static Meadow readMeadow(final StartElement element,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "meadow");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.warn(new MissingPropertyException(element, "status"));
		}
		return setImage(new Meadow(getAttribute(element, "kind"), false, parseBoolean(
				getAttribute(element, "cultivated")), id, FieldStatus.parse(getAttribute(
				element, "status", FieldStatus.random(id).toString()))), element, warner);
	}
	/**
	 * Parse a field.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the meadow represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static Meadow readField(final StartElement element,
								   final Iterable<XMLEvent> stream,
								   final IMutablePlayerCollection players,
								   final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "field");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.warn(new MissingPropertyException(element, "status"));
		}
		return setImage(new Meadow(getAttribute(element, "kind"), true, parseBoolean(
				getAttribute(element, "cultivated")), id, FieldStatus.parse(getAttribute(
				element, "status", FieldStatus.random(id).toString()))), element, warner);
	}
	/**
	 * Parse a mine.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the mine represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static Mine readMine(final StartElement element,
								final Iterable<XMLEvent> stream,
								final IMutablePlayerCollection players,
								final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "mine");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(
				new Mine(getAttrWithDeprecatedForm(element, "kind", "product", warner),
								parseTownStatus(getAttribute(element, "status")),
								getOrGenerateID(element, warner, idFactory)), element,
				warner);
	}
	/**
	 * Parse a Mineral.
	 *
	 * @param element   the element to read from
	 * @param stream    a stream of more elements
	 * @param players   the list of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the parsed mineral
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static MineralVein readMineral(final StartElement element,
										  final Iterable<XMLEvent> stream,
										  final IMutablePlayerCollection players,
										  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "mineral");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(
				new MineralVein(getAttrWithDeprecatedForm(element, "kind", "mineral",
						warner), parseBoolean(getAttribute(element, "exposed")),
									   getIntegerAttribute(element, "dc"),
									   getOrGenerateID(element, warner, idFactory)),
				element, warner);
	}
	/**
	 * Parse a shrub.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the shrub represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static Shrub readShrub(final StartElement element,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "shrub");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Shrub(getAttrWithDeprecatedForm(element,
				"kind", "shrub", warner), getOrGenerateID(element, warner,
				idFactory)), element, warner);
	}
	/**
	 * Parse a Stone.
	 *
	 * @param element   the element to read from
	 * @param stream    a stream of more elements
	 * @param players   the list of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the parsed stone
	 * @throws SPFormatException on SP format error
	 */
	@SuppressWarnings("UnusedParameters")
	public static StoneDeposit readStone(final StartElement element,
										 final Iterable<XMLEvent> stream,
										 final IMutablePlayerCollection players,
										 final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, "stone");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new StoneDeposit(parseStoneKind(
				getAttrWithDeprecatedForm(element, "kind", "stone", warner)),
														 getIntegerAttribute(element,
																 "dc"),
														 getOrGenerateID(element, warner,
																 idFactory)), element, warner);
	}

	/**
	 * Write a resource pile to XML.
	 *
	 * @param obj     the resource to write. Must be a ResourcePile.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeResource(final Document document, final Node parent,
									 Object obj) {
		if (!(obj instanceof ResourcePile)) {
			throw new IllegalArgumentException("Can only write ResourcePile");
		}
		final ResourcePile pile = (ResourcePile) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "resource");
		writeIntegerAttribute(element, "id", pile.getID());
		writeAttribute(element, "kind", pile.getKind());
		writeAttribute(element, "contents", pile.getContents());
		writeIntegerAttribute(element, "quantity", pile.getQuantity());
		writeAttribute(element, "unit", pile.getUnits());
		if (pile.getCreated() >= 0) {
			writeIntegerAttribute(element, "created", pile.getCreated());
		}
		writeImage(element, pile);
		parent.appendChild(element);
	}
	/**
	 * Write a cache to XML.
	 *
	 * @param obj     the resource to write. Must be a CacheFixture.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeCache(final Document document, final Node parent,
								  Object obj) {
		if (!(obj instanceof CacheFixture)) {
			throw new IllegalArgumentException("Can only write CacheFixture");
		}
		final CacheFixture fix = (CacheFixture) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "cache");
		writeAttribute(element, "kind", fix.getKind());
		writeAttribute(element, "contents", fix.getContents());
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
	/**
	 * Write a field or meadow to XML.
	 *
	 * @param obj     the resource to write. Must be a Meadow.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMeadow(final Document document, final Node parent,
								   Object obj) {
		if (!(obj instanceof Meadow)) {
			throw new IllegalArgumentException("Can only write Meadows");
		}
		final Meadow fix = (Meadow) obj;
		final Element element;
		if (fix.isField()) {
			element = document.createElementNS(ISPReader.NAMESPACE, "field");
		} else {
			element = document.createElementNS(ISPReader.NAMESPACE, "meadow");
		}
		writeAttribute(element, "kind", fix.getKind());
		writeBooleanAttribute(element, "cultivated", fix.isCultivated());
		writeAttribute(element, "status", fix.getStatus().toString());
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
	/**
	 * Write a grove or orchard to XML.
	 *
	 * @param obj     the resource to write. Must be a Grove.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeGrove(final Document document, final Node parent,
								  Object obj) {
		if (!(obj instanceof Grove)) {
			throw new IllegalArgumentException("Can only write Grove");
		}
		final Grove fix = (Grove) obj;
		final Element element;
		if (fix.isOrchard()) {
			element = document.createElementNS(ISPReader.NAMESPACE, "orchard");
		} else {
			element = document.createElementNS(ISPReader.NAMESPACE, "grove");
		}
		writeBooleanAttribute(element, "cultivated", fix.isCultivated());
		writeAttribute(element, "kind", fix.getKind());
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
	/**
	 * Write a mine to XML.
	 *
	 * @param obj     the resource to write. Must be a Mine.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMine(final Document document, final Node parent,
								 Object obj) {
		if (!(obj instanceof Mine)) {
			throw new IllegalArgumentException("Can only write Mine");
		}
		final Mine fix = (Mine) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "mine");
		writeAttribute(element, "kind", fix.getKind());
		writeAttribute(element, "status", fix.getStatus().toString());
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
	/**
	 * Write a mineral vein to XML.
	 *
	 * @param obj     the resource to write. Must be a MineralVein.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMineral(final Document document, final Node parent,
									Object obj) {
		if (!(obj instanceof MineralVein)) {
			throw new IllegalArgumentException("Can only write MineralVein");
		}
		final MineralVein fix = (MineralVein) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "mineral");
		writeAttribute(element, "kind", fix.getKind());
		writeBooleanAttribute(element, "exposed", fix.isExposed());
		writeIntegerAttribute(element, "dc", fix.getDC());
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
	/**
	 * Write a stone deposit to XML.
	 *
	 * @param obj     the resource to write. Must be a StoneDeposit.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeStone(final Document document, final Node parent,
								  Object obj) {
		if (!(obj instanceof StoneDeposit)) {
			throw new IllegalArgumentException("Can only write StoneDeposit");
		}
		final StoneDeposit fix = (StoneDeposit) obj;
		final Element element = document.createElementNS(ISPReader.NAMESPACE, "stone");
		writeAttribute(element, "kind", fix.stone().toString());
		writeIntegerAttribute(element, "dc", fix.getDC());
		writeIntegerAttribute(element, "id", fix.getID());
		writeImage(element, fix);
		parent.appendChild(element);
	}
}