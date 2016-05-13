package controller.map.fluidxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.io.IOException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import util.NullCleaner;
import util.Warning;

import static controller.map.fluidxml.XMLHelper.setImage;
import static controller.map.fluidxml.XMLHelper.getAttrWithDeprecatedForm;
import static controller.map.fluidxml.XMLHelper.getAttribute;
import static controller.map.fluidxml.XMLHelper.getIntegerAttribute;
import static controller.map.fluidxml.XMLHelper.getOrGenerateID;
import static controller.map.fluidxml.XMLHelper.hasAttribute;
import static controller.map.fluidxml.XMLHelper.imageXML;
import static controller.map.fluidxml.XMLHelper.requireTag;
import static controller.map.fluidxml.XMLHelper.spinUntilEnd;
import static controller.map.fluidxml.XMLHelper.writeAttribute;
import static controller.map.fluidxml.XMLHelper.writeBooleanAttribute;
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
public class FluidResourceHandler {
	/**
	 * Parse an implement.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the implement represented by the element
	 * @throws SPFormatException on SP format error
	 */
	public static final Implement readImplement(final StartElement element, final Iterable<XMLEvent> stream,
						   final IMutablePlayerCollection players, final Warning warner,
						   final IDFactory idFactory) throws SPFormatException {
		requireTag(element, "implement");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		return setImage(new Implement(getOrGenerateID(element, warner, idFactory),
							 getAttribute(element, "kind")), element, warner);
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
	public static final ResourcePile readResource(final StartElement element,
							  final Iterable<XMLEvent> stream,
							  final IMutablePlayerCollection players,
							  final Warning warner,
							  final IDFactory idFactory) throws SPFormatException {
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
	public static final CacheFixture readCache(final StartElement element,
							  final Iterable<XMLEvent> stream,
							  final IMutablePlayerCollection players,
							  final Warning warner, final IDFactory idFactory)
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
	public static final Grove readGrove(final StartElement element, final Iterable<XMLEvent> stream,
					   final IMutablePlayerCollection players, final Warning warner,
					   final IDFactory idFactory) throws SPFormatException {
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
	public static final Grove readOrchard(final StartElement element, final Iterable<XMLEvent> stream,
										final IMutablePlayerCollection players, final Warning warner,
										final IDFactory idFactory) throws SPFormatException {
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
	public static final Meadow readMeadow(final StartElement element, final Iterable<XMLEvent> stream,
						final IMutablePlayerCollection players, final Warning warner,
						final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "meadow");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
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
	public static final Meadow readField(final StartElement element, final Iterable<XMLEvent> stream,
										  final IMutablePlayerCollection players, final Warning warner,
										  final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "field");
		spinUntilEnd(assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
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
	public static final Mine readMine(final StartElement element, final Iterable<XMLEvent> stream,
					  final IMutablePlayerCollection players, final Warning warner,
					  final IDFactory idFactory) throws SPFormatException {
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
	public static final MineralVein readMineral(final StartElement element,
							 final Iterable<XMLEvent> stream,
							 final IMutablePlayerCollection players,
							 final Warning warner, final IDFactory idFactory)
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
	public static final Shrub readShrub(final StartElement element,
					   final Iterable<XMLEvent> stream,
					   final IMutablePlayerCollection players,
					   final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		requireTag(element, "shrub");
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
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
	public static final StoneDeposit readStone(final StartElement element,
							  final Iterable<XMLEvent> stream,
							  final IMutablePlayerCollection players,
							  final Warning warner, final IDFactory idFactory)
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
	 * Write a resource pile to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a ResourcePile.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeResource(final Appendable ostream, final Object obj,
					  final int indent) throws IOException {
		if (!(obj instanceof ResourcePile)) {
			throw new IllegalArgumentException("Can only write ResourcePile");
		}
		final ResourcePile pile = (ResourcePile) obj;
		writeTag(ostream, "resource", indent);
		writeIntegerAttribute(ostream, "id", pile.getID());
		writeAttribute(ostream, "kind", pile.getKind());
		writeAttribute(ostream, "contents", pile.getContents());
		writeIntegerAttribute(ostream, "quantity", pile.getQuantity());
		writeAttribute(ostream, "unit", pile.getUnits());
		if (pile.getCreated() >= 0) {
			writeIntegerAttribute(ostream, "created", pile.getCreated());
		}
		ostream.append(imageXML(pile));
		ostream.append(" />\n");
	}
	/**
	 * Write a cache to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a CacheFixture.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeCache(final Appendable ostream, final Object obj,
							  final int indent) throws IOException {
		if (!(obj instanceof CacheFixture)) {
			throw new IllegalArgumentException("Can only write CacheFixture");
		}
		final CacheFixture fix = (CacheFixture) obj;
		writeTag(ostream, "cache", indent);
		writeAttribute(ostream, "kind", fix.getKind());
		writeAttribute(ostream, "contents", fix.getContents());
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}
	/**
	 * Write a field or meadow to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a Meadow.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeMeadow(final Appendable ostream, final Object obj,
							  final int indent) throws IOException {
		if (!(obj instanceof Meadow)) {
			throw new IllegalArgumentException("Can only write Meadows");
		}
		final Meadow fix = (Meadow) obj;
		if (fix.isField()) {
			writeTag(ostream, "field", indent);
		} else {
			writeTag(ostream, "meadow", indent);
		}
		writeAttribute(ostream, "kind", fix.getKind());
		writeBooleanAttribute(ostream, "cultivated", fix.isCultivated());
		writeAttribute(ostream, "status", fix.getStatus().toString());
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}
	/**
	 * Write a grove or orchard to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a Grove.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeGrove(final Appendable ostream, final Object obj,
							  final int indent) throws IOException {
		if (!(obj instanceof Grove)) {
			throw new IllegalArgumentException("Can only write Grove");
		}
		final Grove fix = (Grove) obj;
		if (fix.isOrchard()) {
			writeTag(ostream, "orchard", indent);
		} else {
			writeTag(ostream, "grove", indent);
		}
		writeBooleanAttribute(ostream, "cultivated", fix.isCultivated());
		writeAttribute(ostream, "kind", fix.getKind());
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}
	/**
	 * Write a mine to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a Mine.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeMine(final Appendable ostream, final Object obj,
							  final int indent) throws IOException {
		if (!(obj instanceof Mine)) {
			throw new IllegalArgumentException("Can only write Mine");
		}
		final Mine fix = (Mine) obj;
		writeTag(ostream, "mine", indent);
		writeAttribute(ostream, "kind", fix.getKind());
		writeAttribute(ostream, "status", fix.getStatus().toString());
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}
	/**
	 * Write a mineral vein to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a MineralVein.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeMineral(final Appendable ostream, final Object obj,
							  final int indent) throws IOException {
		if (!(obj instanceof MineralVein)) {
			throw new IllegalArgumentException("Can only write MineralVein");
		}
		final MineralVein fix = (MineralVein) obj;
		writeTag(ostream, "mineral", indent);
		writeAttribute(ostream, "kind", fix.getKind());
		writeBooleanAttribute(ostream, "exposed", fix.isExposed());
		writeIntegerAttribute(ostream, "dc", fix.getDC());
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}
	/**
	 * Write a stone deposit to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write. Must be a StoneDeposit.
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	public static final void writeStone(final Appendable ostream, final Object obj,
							  final int indent) throws IOException {
		if (!(obj instanceof StoneDeposit)) {
			throw new IllegalArgumentException("Can only write StoneDeposit");
		}
		final StoneDeposit fix = (StoneDeposit) obj;
		writeTag(ostream, "stone", indent);
		writeAttribute(ostream, "kind", fix.stone().toString());
		writeIntegerAttribute(ostream, "dc", fix.getDC());
		writeIntegerAttribute(ostream, "id", fix.getID());
		ostream.append(imageXML(fix));
		ostream.append(" />\n");
	}



	}