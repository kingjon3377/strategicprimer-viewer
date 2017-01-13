package controller.map.yaxml;

import controller.map.formatexceptions.DeprecatedPropertyException;
import controller.map.formatexceptions.MissingPropertyException;
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
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.resources.StoneKind;
import model.map.fixtures.towns.TownStatus;
import util.Warning;

import static java.lang.Boolean.parseBoolean;

/**
 * A reader for resource-bearing TileFixtures.
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
public final class YAResourceReader extends YAAbstractReader<HarvestableFixture> {
	/**
	 * The parameter giving the status of a fixture.
	 */
	private static final String STATUS_PAR = "status";
	/**
	 * The parameter saying what kind of thing is in a HarvestableFixture.
	 */
	private static final String KIND_PAR = "kind";
	/**
	 * The parameter saying whether a grove or field or orchard or meadow is cultivated.
	 */
	private static final String CULTIVATED_PARAM = "cultivated";

	/**
	 * List of supported tags.
	 */
	private static final Set<String> SUPP_TAGS = Collections.unmodifiableSet(
			new HashSet<>(Arrays.asList("cache", "grove", "orchard", "field", "meadow",
					"mine", "mineral", "shrub", "stone")));

	/**
	 * The Warning instance to use.
	 */
	private final Warning warner;

	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAResourceReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
		warner = warning;
	}
	/**
	 * @param element a tag
	 * @return the value of its 'dc' property.
	 * @throws SPFormatException on SP format problem
	 */
	private static int getDC(final StartElement element)
			throws SPFormatException {
		return getIntegerParameter(element, "dc");
	}

	/**
	 * Create a Meadow, to reduce code duplication between 'field' and 'meadow' cases.
	 *
	 * @param element the tag we're parsing
	 * @param field   whether this is a field (meadow otherwise)
	 * @param idNum   the ID number parsed or generated
	 * @return the parsed Meadow object.
	 * @throws SPFormatException on SP format problems
	 */
	private HarvestableFixture createMeadow(final StartElement element,
												   final boolean field, final int idNum)
			throws SPFormatException {
		if (!hasParameter(element, STATUS_PAR)) {
			warner.warn(new MissingPropertyException(element, STATUS_PAR));
		}
		return new Meadow(getParameter(element, KIND_PAR), field,
								 parseBoolean(getParameter(element, CULTIVATED_PARAM)),
								 idNum,
								 FieldStatus.parse(getParameter(element, STATUS_PAR,
										 FieldStatus.random(idNum).toString())));
	}

	/**
	 * Create a Grove, to reduce code duplication between 'grove' and 'orchard' cases.
	 *
	 * @param element the tag we're parsing
	 * @param orchard whether this is an orchard, a grove otherwise
	 * @param idNum   the ID number parsed or generated
	 * @return the parsed Grove object
	 * @throws SPFormatException on SP format problems
	 */
	private HarvestableFixture createGrove(final StartElement element,
												  final boolean orchard, final int idNum)
			throws SPFormatException {
		return new Grove(orchard, isCultivated(element),
								getParamWithDeprecatedForm(element, KIND_PAR, "tree"),
								idNum);
	}

	/**
	 * @param element a tag representing a grove or orchard
	 * @return whether the grove or orchard is cultivated
	 * @throws SPFormatException on SP format problems: use of 'wild' if warnings are
	 *                           fatal, or if both properties are missing.
	 */
	private boolean isCultivated(final StartElement element) throws SPFormatException {
		if (hasParameter(element, CULTIVATED_PARAM)) {
			return parseBoolean(getParameter(element, CULTIVATED_PARAM));
		} else {
			if (hasParameter(element, "wild")) {
				warner.warn(new DeprecatedPropertyException(element, "wild",
																   CULTIVATED_PARAM));
				return !parseBoolean(getParameter(element, "wild"));
			} else {
				throw new MissingPropertyException(element, CULTIVATED_PARAM);
			}
		}
	}

	/**
	 * @param meadow a meadow or field
	 * @return the proper tag for it
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String getMeadowTag(final Meadow meadow) {
		if (meadow.isField()) {
			return "field";
		} else {
			return "meadow";
		}
	}

	/**
	 * @param grove a grove or orchard
	 * @return the proper tag for it
	 */
	@SuppressWarnings("TypeMayBeWeakened")
	private static String getGroveTag(final Grove grove) {
		if (grove.isOrchard()) {
			return "orchard";
		} else {
			return "grove";
		}
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
	 * @param stream    the stream to read more elements from     @return the parsed
	 *                  resource
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public HarvestableFixture read(final StartElement element,
								   final QName parent,
								   final Iterable<XMLEvent> stream)
			throws SPFormatException {
		requireTag(element, parent, "cache", "grove", "orchard",
				"field", "meadow", "mine", "mineral", "shrub", "stone");
		final int idNum = getOrGenerateID(element);
		final HarvestableFixture retval;
		switch (element.getName().getLocalPart().toLowerCase()) {
		case "cache":
			retval = new CacheFixture(getParameter(element, KIND_PAR),
											 getParameter(element, "contents"), idNum);
			break;
		case "field":
			retval = createMeadow(element, true, idNum);
			break;
		case "grove":
			retval = createGrove(element, false, idNum);
			break;
		case "meadow":
			retval = createMeadow(element, false, idNum);
			break;
		case "mine":
			retval = new Mine(getParamWithDeprecatedForm(element, KIND_PAR,
					"product"),
									 TownStatus.parseTownStatus(
											 getParameter(element, STATUS_PAR)),
									 idNum);
			break;
		case "mineral":
			retval = new MineralVein(getParamWithDeprecatedForm(element, KIND_PAR,
					"mineral"), parseBoolean(getParameter(element,
					"exposed")), getDC(element), idNum);
			break;
		case "orchard":
			retval = createGrove(element, true, idNum);
			break;
		case "shrub":
			retval = new Shrub(getParamWithDeprecatedForm(element, KIND_PAR,
					"shrub"), idNum);
			break;
		case "stone":
			retval = new StoneDeposit(StoneKind.parseStoneKind(
					getParamWithDeprecatedForm(element,
							KIND_PAR, "stone")), getDC(element), idNum);
			break;
		default:
			throw new IllegalArgumentException("Unhandled harvestable tag");
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
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
	public void write(final Appendable ostream, final HarvestableFixture obj,
					  final int indent) throws IOException {
		if (obj instanceof CacheFixture) {
			writeTag(ostream, "cache", indent);
			final CacheFixture cache = (CacheFixture) obj;
			writeProperty(ostream, "kind", cache.getKind());
			writeProperty(ostream, "contents", cache.getContents());
		} else if (obj instanceof Meadow) {
			writeTag(ostream, getMeadowTag((Meadow) obj), indent);
			final Meadow meadow = (Meadow) obj;
			writeProperty(ostream, "kind", meadow.getKind());
			writeProperty(ostream, "cultivated", Boolean.toString(meadow.isCultivated()));
			writeProperty(ostream, "status", meadow.getStatus().toString());
		} else if (obj instanceof Grove) {
			writeTag(ostream, getGroveTag((Grove) obj), indent);
			final Grove grove = (Grove) obj;
			writeProperty(ostream, "cultivated", Boolean.toString(grove.isCultivated()));
			writeProperty(ostream, "kind", grove.getKind());
		} else if (obj instanceof Mine) {
			writeTag(ostream, "mine", indent);
			final Mine mine = (Mine) obj;
			writeProperty(ostream, "kind", mine.getKind());
			writeProperty(ostream, "status", mine.getStatus().toString());
		} else if (obj instanceof MineralVein) {
			writeTag(ostream, "mineral", indent);
			final MineralVein mineral = (MineralVein) obj;
			writeProperty(ostream, "kind", mineral.getKind());
			writeProperty(ostream, "exposed", Boolean.toString(mineral.isExposed()));
			writeProperty(ostream, "dc", mineral.getDC());
		} else if (obj instanceof Shrub) {
			writeTag(ostream, "shrub", indent);
			writeProperty(ostream, "kind", ((Shrub) obj).getKind());
		} else if (obj instanceof StoneDeposit) {
			writeTag(ostream, "stone", indent);
			final StoneDeposit stone = (StoneDeposit) obj;
			writeProperty(ostream, "kind", stone.stone().toString());
			writeProperty(ostream, "dc", stone.getDC());
		} else {
			throw new IllegalStateException("Unhandled HarvestableFixture subtype");
		}
		writeProperty(ostream, "id", obj.getID());
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof HarvestableFixture;
	}
}
