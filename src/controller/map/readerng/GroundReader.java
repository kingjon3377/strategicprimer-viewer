package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import java.util.Collections;
import java.util.List;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.Ground;
import util.NullCleaner;
import util.Pair;
import util.Warning;

/**
 * A reader for Ground.
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
public final class GroundReader implements INodeHandler<Ground> {
	/**
	 * Parse ground.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the ground represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Ground parse(final StartElement element,
	                    final Iterable<XMLEvent> stream,
	                    final IMutablePlayerCollection players,
	                    final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Ground fix = new Ground(getAttributeWithDeprecatedForm(element,
				"kind", "ground", warner), Boolean.parseBoolean(getAttribute(
				element, "exposed")));
		addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("ground"));
	}

	/**
	 * @return the class we know how to write
	 */
	@Override
	public Class<Ground> writtenClass() {
		return Ground.class;
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
	public <S extends Ground> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval =
				new SPIntermediateRepresentation("ground", Pair.of("kind",
						obj.getKind()), Pair.of("exposed", NullCleaner
								                                   .assertNotNull(
										                                   Boolean
												                                   .toString(
												                                   obj
														                                   .isExposed()))));
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "GroundReader";
	}
}
