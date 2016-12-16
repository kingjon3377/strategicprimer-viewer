package controller.map.fluidxml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An interface for writers-to-XML. It's expected that most "implementations" will in
 * fact be method references.
 *
 * In the interests of having two *entirely different* XML I/O implementations, we'd
 * like to use the DOM APIs for writing. Unfortunately, however, their output proves
 * unusable for our purposes---both our tests and the readability of the generated XML
 * depend on attributes being in an order that makes sense within the domain, and the
 * DOM APIs only output attributes in alphabetical order.
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
 * @deprecated FluidXML is deprecated in favor of YAXML
 */
@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
@Deprecated
@FunctionalInterface
public interface FluidXMLWriter {
	/**
	 * Write XML representing an object.
	 *
	 * @param ostream the writer to write to
	 * @param indent  the indentation level
	 * @param obj     The object being written.
	 * @throws XMLStreamException on error in the writer
	 */
	void writeSPObject(final XMLStreamWriter ostream, final Object obj, final int indent)
			throws XMLStreamException;
}
