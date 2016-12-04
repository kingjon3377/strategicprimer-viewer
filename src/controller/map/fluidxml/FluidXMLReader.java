package controller.map.fluidxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.misc.IDRegistrar;
import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutablePlayerCollection;
import util.Warning;

/**
 * An interface for readers-from-XML. It's expected that most "implementations" will in
 * fact be method references.
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
@FunctionalInterface
public interface FluidXMLReader {
	/**
	 * @param element   the XML tag being parsed
	 * @param parent    the parent tag
	 * @param stream    the stream of XML elements we're reading from
	 * @param players   the collection of players in the current map
	 * @param warner    the Warning instance to use to handle warning conditions
	 * @param idFactory the factory to record and generate ID numbers
	 * @return the object parsed from XML
	 * @throws SPFormatException        on SP format errors
	 * @throws IllegalArgumentException if the tag is not one we know how to read
	 */
	Object readSPObject(StartElement element, QName parent, Iterable<XMLEvent> stream,
						IMutablePlayerCollection players, Warning warner,
						IDRegistrar idFactory)
			throws SPFormatException, IllegalArgumentException;
}
