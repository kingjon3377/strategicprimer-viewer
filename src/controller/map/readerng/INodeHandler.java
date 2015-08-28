package controller.map.readerng;

import java.util.List;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDFactory;
import model.map.IMutablePlayerCollection;
import util.Warning;

/**
 * An interface for *stateless* per-class XML readers/writers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 * @param <T> The type of object the reader knows how to read
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public interface INodeHandler<T> {
	/**
	 * @return the class this can write to a writer.
	 */
	Class<T> writes();

	/**
	 * @return a list of the tags the reader can handle.
	 */
	List<String> understands();

	/**
	 * Parse an instance of the type from XML.
	 *
	 * @param element the eleent to start parsing with
	 * @param stream to get more elements from
	 * @param players the collection of players
	 * @param warner the Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @return the produced type
	 * @throws SPFormatException on map format problems
	 */
	T parse(StartElement element, Iterable<XMLEvent> stream,
			IMutablePlayerCollection players, Warning warner, IDFactory idFactory)
			throws SPFormatException;

	/**
	 * Create an intermediate representation to convert to XML.
	 *
	 * @param <S> the type of the object---it can be a subclass, to make the
	 *        adapter work.
	 * @param obj the object to write
	 * @return an intermediate representation
	 */
	<S extends T> SPIntermediateRepresentation write(S obj);
}
