package controller.map.fluidxml;

import java.io.IOException;

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
	 * @param ostream the stream to write to
	 * @param obj the object to write
	 * @param indent the indentation level
	 * @throws IOException on I/O problems
	 * @throws IllegalArgumentException if obj is not an object we know how to write
	 */
	void writeSPObject(Appendable ostream, Object obj, int indent)
			throws IOException, IllegalArgumentException;
}
