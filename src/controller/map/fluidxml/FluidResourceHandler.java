package controller.map.fluidxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.math.BigDecimal;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
import util.Quantity;
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
import static controller.map.fluidxml.XMLHelper.writeTag;
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
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
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
	 * @param parent    the parent tag
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
											final QName parent,
											final Iterable<XMLEvent> stream,
											final IMutablePlayerCollection players,
											final Warning warner,
											final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "resource");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final String quantityStr = getAttribute(element, "quantity");
		final Number quantity;
		if (quantityStr.contains(".")) {
			quantity = new BigDecimal(quantityStr);
		} else {
			quantity = Integer.parseInt(quantityStr);
		}
		final ResourcePile retval =
				new ResourcePile(getOrGenerateID(element, warner, idFactory),
										getAttribute(element, "kind"),
										getAttribute(element, "contents"),
										new Quantity(quantity,
															getAttribute(element, "unit",
																	"")));
		if (hasAttribute(element, "created")) {
			retval.setCreated(getIntegerAttribute(element, "created"));
		}
		return setImage(retval, element, warner);
	}

	/**
	 * Parse a cache.
	 *
	 * @param element   the element to read from
	 * @param parent    the parent tag
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
										 final QName parent,
										 final Iterable<XMLEvent> stream,
										 final IMutablePlayerCollection players,
										 final Warning warner,
										 final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "cache");
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
	 * @param parent    the parent tag
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
								  final QName parent,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "grove");
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
	 * @param parent    the parent tag
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
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "orchard");
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
	 * @param parent    the parent tag
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
									final QName parent,
									final Iterable<XMLEvent> stream,
									final IMutablePlayerCollection players,
									final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "meadow");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.warn(new MissingPropertyException(element, "status"));
		}
		return setImage(new Meadow(getAttribute(element, "kind"), false, parseBoolean(
				getAttribute(element, "cultivated")), id, FieldStatus.parse(getAttribute(
				element, "status", FieldStatus.random(id).toString()))), element,
				warner);
	}

	/**
	 * Parse a field.
	 *
	 * @param element   the element to read from
	 * @param parent    the parent tag
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
								   final QName parent,
								   final Iterable<XMLEvent> stream,
								   final IMutablePlayerCollection players,
								   final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "field");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory);
		if (!hasAttribute(element, "status")) {
			warner.warn(new MissingPropertyException(element, "status"));
		}
		return setImage(new Meadow(getAttribute(element, "kind"), true, parseBoolean(
				getAttribute(element, "cultivated")), id, FieldStatus.parse(getAttribute(
				element, "status", FieldStatus.random(id).toString()))), element,
				warner);
	}

	/**
	 * Parse a mine.
	 *
	 * @param element   the element to read from
	 * @param parent    the parent tag
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
								final QName parent,
								final Iterable<XMLEvent> stream,
								final IMutablePlayerCollection players,
								final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "mine");
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
	 * @param parent    the parent tag
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
										  final QName parent,
										  final Iterable<XMLEvent> stream,
										  final IMutablePlayerCollection players,
										  final Warning warner,
										  final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "mineral");
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
	 * @param parent    the parent tag
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
								  final QName parent,
								  final Iterable<XMLEvent> stream,
								  final IMutablePlayerCollection players,
								  final Warning warner, final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "shrub");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Shrub(getAttrWithDeprecatedForm(element,
				"kind", "shrub", warner), getOrGenerateID(element, warner,
				idFactory)), element, warner);
	}

	/**
	 * Parse a Stone.
	 *
	 * @param element   the element to read from
	 * @param parent    the parent tag
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
										 final QName parent,
										 final Iterable<XMLEvent> stream,
										 final IMutablePlayerCollection players,
										 final Warning warner,
										 final IDRegistrar idFactory)
			throws SPFormatException {
		requireTag(element, parent, "stone");
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
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeResource(final XMLStreamWriter ostream, final Object obj,
									 final int indent) throws XMLStreamException {
		if (!(obj instanceof ResourcePile)) {
			throw new IllegalArgumentException("Can only write ResourcePile");
		}
		final ResourcePile pile = (ResourcePile) obj;
		writeTag(ostream, "resource", indent, true);
		writeIntegerAttribute(ostream, "id", pile.getID());
		writeAttribute(ostream, "kind", pile.getKind());
		writeAttribute(ostream, "contents", pile.getContents());
		final Number quantity = pile.getQuantity().getNumber();
		if (quantity instanceof Integer) {
			writeIntegerAttribute(ostream, "quantity", quantity.intValue());
		} else if (quantity instanceof BigDecimal) {
			if (((BigDecimal) quantity).scale() > 0) {
				writeAttribute(ostream, "quantity",
						((BigDecimal) quantity).toPlainString());
			} else {
				writeIntegerAttribute(ostream, "quantity", quantity.intValue());
			}
		} else {
			throw new IllegalArgumentException("ResourcePile with non-Integer, " +
													   "non-BigDecimal quantity");
		}
		writeAttribute(ostream, "unit", pile.getQuantity().getUnits());
		if (pile.getCreated() >= 0) {
			writeIntegerAttribute(ostream, "created", pile.getCreated());
		}
		writeImage(ostream, pile);
	}

	/**
	 * Write a cache to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeCache(final XMLStreamWriter ostream, final Object obj,
								  final int indent) throws XMLStreamException {
		if (!(obj instanceof CacheFixture)) {
			throw new IllegalArgumentException("Can only write CacheFixture");
		}
		final CacheFixture fix = (CacheFixture) obj;
		writeTag(ostream, "cache", indent, true);
		writeAttribute(ostream, "kind", fix.getKind());
		writeAttribute(ostream, "contents", fix.getContents());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
	}

	/**
	 * Write a field or meadow to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMeadow(final XMLStreamWriter ostream, final Object obj,
								   final int indent) throws XMLStreamException {
		if (!(obj instanceof Meadow)) {
			throw new IllegalArgumentException("Can only write Meadows");
		}
		final Meadow fix = (Meadow) obj;
		if (fix.isField()) {
			writeTag(ostream, "field", indent, true);
		} else {
			writeTag(ostream, "meadow", indent, true);
		}
		writeAttribute(ostream, "kind", fix.getKind());
		writeBooleanAttribute(ostream, "cultivated", fix.isCultivated());
		writeAttribute(ostream, "status", fix.getStatus().toString());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
	}

	/**
	 * Write a grove or orchard to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeGrove(final XMLStreamWriter ostream, final Object obj,
								  final int indent) throws XMLStreamException {
		if (!(obj instanceof Grove)) {
			throw new IllegalArgumentException("Can only write Grove");
		}
		final Grove fix = (Grove) obj;
		if (fix.isOrchard()) {
			writeTag(ostream, "orchard", indent, true);
		} else {
			writeTag(ostream, "grove", indent, true);
		}
		writeBooleanAttribute(ostream, "cultivated", fix.isCultivated());
		writeAttribute(ostream, "kind", fix.getKind());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
	}

	/**
	 * Write a mine to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMine(final XMLStreamWriter ostream, final Object obj,
								 final int indent) throws XMLStreamException {
		if (!(obj instanceof Mine)) {
			throw new IllegalArgumentException("Can only write Mine");
		}
		final Mine fix = (Mine) obj;
		writeTag(ostream, "mine", indent, true);
		writeAttribute(ostream, "kind", fix.getKind());
		writeAttribute(ostream, "status", fix.getStatus().toString());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
	}

	/**
	 * Write a mineral vein to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeMineral(final XMLStreamWriter ostream, final Object obj,
									final int indent) throws XMLStreamException {
		if (!(obj instanceof MineralVein)) {
			throw new IllegalArgumentException("Can only write MineralVein");
		}
		final MineralVein fix = (MineralVein) obj;
		writeTag(ostream, "mineral", indent, true);
		writeAttribute(ostream, "kind", fix.getKind());
		writeBooleanAttribute(ostream, "exposed", fix.isExposed());
		writeIntegerAttribute(ostream, "dc", fix.getDC());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
	}

	/**
	 * Write a stone deposit to XML.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException       on error in the writer
	 * @throws IllegalArgumentException if obj is not the type we expect
	 */
	public static void writeStone(final XMLStreamWriter ostream, final Object obj,
								  final int indent) throws XMLStreamException {
		if (!(obj instanceof StoneDeposit)) {
			throw new IllegalArgumentException("Can only write StoneDeposit");
		}
		final StoneDeposit fix = (StoneDeposit) obj;
		writeTag(ostream, "stone", indent, true);
		writeAttribute(ostream, "kind", fix.stone().toString());
		writeIntegerAttribute(ostream, "dc", fix.getDC());
		writeIntegerAttribute(ostream, "id", fix.getID());
		writeImage(ostream, fix);
	}
}