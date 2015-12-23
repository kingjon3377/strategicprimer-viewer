package controller.map.readerng;

import controller.map.formatexceptions.MissingPropertyException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.util.Arrays;
import java.util.List;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.resources.FieldStatus;
import model.map.fixtures.resources.Meadow;
import util.NullCleaner;
import util.Pair;
import util.Warning;

import static controller.map.readerng.XMLHelper.addImage;
import static controller.map.readerng.XMLHelper.getAttribute;
import static controller.map.readerng.XMLHelper.getOrGenerateID;
import static controller.map.readerng.XMLHelper.hasAttribute;
import static controller.map.readerng.XMLHelper.spinUntilEnd;

/**
 * A reader for Meadows.
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
public final class MeadowReader implements INodeHandler<Meadow> {
	/**
	 * The name of the property giving the status of the field.
	 */
	private static final String STATUS_ATTR = "status";

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
	@Override
	public Meadow parse(final StartElement element,
						final Iterable<XMLEvent> stream,
						final IMutablePlayerCollection players,
						final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final int id = getOrGenerateID(element, warner, idFactory); // NOPMD
		if (!hasAttribute(element, STATUS_ATTR)) {
			warner.warn(new MissingPropertyException(NullCleaner
															 .assertNotNull(
																	 element.getName()
																			 .getLocalPart()),

															STATUS_ATTR,
															element.getLocation()
																	.getLineNumber()));
		}
		final Meadow fix =
				new Meadow(
								  getAttribute(element, "kind"),
								  "field".equalsIgnoreCase(NullCleaner
																   .assertNotNull(
																		   element
																				   .getName()
																				   .getLocalPart())),
								  Boolean.parseBoolean(
										  getAttribute(element, "cultivated")),
								  id, FieldStatus
											  .parse(getAttribute(element, STATUS_ATTR,
													  FieldStatus.random(id)
															  .toString())));
		addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Arrays.asList("meadow", "field"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Meadow> writtenClass() {
		return Meadow.class;
	}

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the adapter
	 *            work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	@Override
	public <S extends Meadow> SPIntermediateRepresentation write(final S obj) {
		final String cult =
				NullCleaner.assertNotNull(Boolean.toString(obj.isCultivated()));
		final String tag; // NOPMD
		if (obj.isField()) {
			tag = "field";
		} else {
			tag = "meadow";
		}
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
																							tag,
																							Pair.of("kind",
																									obj.getKind()),
																							Pair.of("cultivated",
																									cult),
																							Pair.of(STATUS_ATTR,
																									obj
																											.getStatus()
																											.toString()));
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MeadowReader";
	}
}
