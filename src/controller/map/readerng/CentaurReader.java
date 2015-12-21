package controller.map.readerng;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import model.map.fixtures.mobile.Centaur;
import util.NullCleaner;
import util.Pair;
import util.Warning;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Collections;
import java.util.List;

import static controller.map.readerng.XMLHelper.*;

/**
 * A reader for Centaurs.
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
public final class CentaurReader implements INodeHandler<Centaur> {
	/**
	 * Parse a centaur.
	 *
	 * @param element   the element to read from
	 * @param stream    the stream to read more elements from
	 * @param players   the collection of players
	 * @param warner    the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new
	 *                     ones as
	 *                  needed
	 * @return the centaur represented by the element
	 * @throws SPFormatException on SP format error
	 */
	@Override
	public Centaur parse(final StartElement element,
	                     final Iterable<XMLEvent> stream,
	                     final IMutablePlayerCollection players,
	                     final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		spinUntilEnd(NullCleaner.assertNotNull(element.getName()), stream);
		final Centaur fix = new Centaur(getAttribute(element, "kind"),
				                               getOrGenerateID(element, warner,
						                               idFactory));
		XMLHelper.addImage(element, fix);
		return fix;
	}

	/**
	 * @return a list of the tags this reader understands
	 */
	@Override
	public List<String> understands() {
		return NullCleaner.assertNotNull(Collections.singletonList("centaur"));
	}

	/**
	 * @return the kind we know how to read
	 */
	@Override
	public Class<Centaur> writtenClass() {
		return Centaur.class;
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
	public <S extends Centaur> SPIntermediateRepresentation write(final S obj) {
		final SPIntermediateRepresentation retval = new SPIntermediateRepresentation(
				                                                                            "centaur",
				                                                                            Pair.of("kind",
						                                                                            obj.getKind()));
		retval.addIdAttribute(obj.getID());
		retval.addImageAttribute(obj);
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CentaurReader";
	}
}
