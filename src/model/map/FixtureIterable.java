package model.map;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Interface to give us knowledge at run-time that an iterable is an iterable of some sort
 * of fixture.
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
 * @param <T> the type of fixtures in the iterable
 * @author Jonathan Lovelace
 */
@FunctionalInterface
public interface FixtureIterable<T extends IFixture> extends Iterable<T> {
	/**
	 * Stream the fixture's members.
	 * @return a Stream of the fixture's members
	 */
	default Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
