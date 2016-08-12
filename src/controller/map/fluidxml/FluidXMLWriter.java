package controller.map.fluidxml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An interface for writers-to-XML. It's expected that most "implementations" will in
 * fact be method references.
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
@FunctionalInterface
public interface FluidXMLWriter {
	/**
	 * Create DOM subtree representing the given object.
	 * @param document the Document object, used to get new Elements
	 * @param parent the parent tag, to which the subtree should be attached
	 * @param obj The object being written.
	 */
	void writeSPObject(final Document document, final Node parent, Object obj);
}
