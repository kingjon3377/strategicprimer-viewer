package view.util;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * A wrapper around System.in to prevent it being closed.
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
@SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
public final class SystemIn extends FilterInputStream {
	/**
	 * Constructor.
	 */
	private SystemIn() {
		super(System.in);
	}
	/**
	 * Singleton.
	 */
	public static final InputStream SYS_IN = new SystemIn();
	/**
	 * Do *not* close.
	 */
	@Override
	public void close() {
		// Do nothing.
	}
}
