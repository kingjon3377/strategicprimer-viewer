package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.ResourcePile;
import org.eclipse.jdt.annotation.NonNull;
import util.NullCleaner;
import util.Warning;

import static controller.map.readerng.XMLHelper.addImage;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getIntegerAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

/**
 * A reader for Resource Piles.
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
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public final class ResourceReader implements INodeHandler<@NonNull ResourcePile> {
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
	@Override
	public ResourcePile parse(final StartElement element,
							  final Iterable<XMLEvent> stream,
							  final IMutablePlayerCollection players,
							  final Warning warner,
							  final IDFactory idFactory) throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final ResourcePile retval =
				new ResourcePile(getOrGenerateID(element, warner, idFactory),
										getAttribute(element, "kind"),
										getAttribute(element, "contents"),
										getIntegerAttribute(element, "quantity"),
										getAttribute(element, "unit", ""));
		if (hasAttribute(element, "created")) {
			retval.setCreated(getIntegerAttribute(element, "created"));
		}
		addImage(element, retval);
		return retval;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("resource"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<ResourcePile> writtenClass() {
		return ResourcePile.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public SPIntermediateRepresentation write(final ResourcePile obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("resource");
		retval.addIdAttribute(obj.getID());
		retval.addAttribute("kind", obj.getKind());
		retval.addAttribute("contents", obj.getContents());
		retval.addIntegerAttribute("quantity", obj.getQuantity());
		retval.addAttribute("unit", obj.getUnits());
		if (obj.getCreated() >= 0) {
			retval.addIntegerAttribute("created", obj.getCreated());
		}
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ResourceReader";
	}

}
