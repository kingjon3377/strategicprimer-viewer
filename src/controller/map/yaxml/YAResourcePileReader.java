package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import java.io.IOException;
import java.math.BigDecimal;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.fixtures.ResourcePile;
import util.Quantity;
import util.Warning;

/**
 * A reader for resource piles.
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
public final class YAResourcePileReader extends YAAbstractReader<ResourcePile> {
	/**
	 * @param warning the Warning instance to use
	 * @param idRegistrar the factory for ID numbers.
	 */
	public YAResourcePileReader(final Warning warning, final IDRegistrar idRegistrar) {
		super(warning, idRegistrar);
	}

	/**
	 * @param element   the XML element to parse
	 * @param parent    the parent tag
	 * @param stream    the stream to read more elements from
	 * @return the parsed implement
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public ResourcePile read(final StartElement element, final QName parent,
							 final Iterable<XMLEvent> stream) throws SPFormatException {
		requireTag(element, parent, "resource");
		final String quantityStr = getParameter(element, "quantity");
		final Number quantity;
		if (quantityStr.contains(".")) {
			quantity = new BigDecimal(quantityStr);
		} else {
			quantity = Integer.valueOf(Integer.parseInt(quantityStr));
		}
		final ResourcePile retval =
				new ResourcePile(getOrGenerateID(element),
										getParameter(element, "kind"),
										getParameter(element, "contents"),
										new Quantity(quantity,
															getParameter(element, "unit",
																	"")));
		if (hasParameter(element, "created")) {
			retval.setCreated(getIntegerParameter(element, "created"));
		}
		spinUntilEnd(element.getName(), stream);
		retval.setImage(getParameter(element, "image", ""));
		return retval;
	}

	/**
	 * @param tag a tag
	 * @return whether it's one we supported
	 */
	@Override
	public boolean isSupportedTag(final String tag) {
		return "resource".equalsIgnoreCase(tag);
	}

	/**
	 * Write a resource pile to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj     the resource to write
	 * @param indent  the current indentation level
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Appendable ostream, final ResourcePile obj,
					  final int indent) throws IOException {
		writeTag(ostream, "resource", indent);
		writeProperty(ostream, "id", Integer.toString(obj.getID()));
		writeProperty(ostream, "kind", obj.getKind());
		writeProperty(ostream, "contents", obj.getContents());
		writeProperty(ostream, "quantity", obj.getQuantity().getNumber().toString());
		writeProperty(ostream, "unit", obj.getQuantity().getUnits());
		if (obj.getCreated() >= 0) {
			writeProperty(ostream, "created", Integer.toString(obj.getCreated()));
		}
		writeImageXML(ostream, obj);
		closeLeafTag(ostream);
	}

	/**
	 * @param obj an object
	 * @return whether we can write it
	 */
	@Override
	public boolean canWrite(final Object obj) {
		return obj instanceof ResourcePile;
	}
}
