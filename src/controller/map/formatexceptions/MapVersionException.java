package controller.map.formatexceptions;

import javax.xml.stream.Location;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception to throw when the map's version is too old.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class MapVersionException extends SPFormatException {
	/**
	 * Constructor.
	 *
	 * @param message the message to show the user if this isn't caught.
	 */
	public MapVersionException(final String message) {
		super(message, new ZeroLocation());
	}

	/**
	 * Constructor.
	 *
	 * @param message the message to show the user if this isn't caught.
	 * @param loc     the location of the map tag.
	 */
	public MapVersionException(final String message, final Location loc) {
		super(message, loc);
	}

	/**
	 * The location of the start of the document.
	 */
	private static class ZeroLocation implements Location {
		/**
		 * The zero location is at line 0.
		 * @return zero
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public int getLineNumber() {
			return 0;
		}

		/**
		 * The zero location is at column 0.
		 * @return zero
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public int getColumnNumber() {
			return 0;
		}

		/**
		 * The zero location is at character offset 0.
		 * @return zero
		 */
		@SuppressWarnings("MethodReturnAlwaysConstant")
		@Override
		public int getCharacterOffset() {
			return 0;
		}

		/**
		 * The zero location doesn't have a public ID.
		 * @return null
		 */
		@SuppressWarnings("ReturnOfNull")
		@Override
		@Nullable
		public String getPublicId() {
			return null;
		}

		/**
		 * The zero location doesn't have a system ID.
		 * @return null
		 */
		@SuppressWarnings("ReturnOfNull")
		@Override
		@Nullable
		public String getSystemId() {
			return null;
		}
	}
}
