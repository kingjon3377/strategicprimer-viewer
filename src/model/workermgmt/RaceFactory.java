package model.workermgmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import util.SingletonRandom;

/**
 * A class to select a race at "random".
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
@SuppressWarnings("UtilityClassCanBeEnum")
public final class RaceFactory {
	/**
	 * A list of races.
	 */
	private static final List<String> RACES = new ArrayList<>();

	static {
		RACES.add("dwarf");
		RACES.add("elf");
		RACES.add("gnome");
		RACES.add("half-elf");
		RACES.add("Danan");
		while (RACES.size() < 20) {
			RACES.add("human");
		}
	}

	/**
	 * Do not instantiate.
	 */
	private RaceFactory() {
		// Static class.
	}

	/**
	 * @param random a Random instance to use
	 * @return a race selected using that instance.
	 */
	public static String getRace(final Random random) {
		return RACES.get(random.nextInt(RACES.size()));
	}

	/**
	 * @return a race selected at random.
	 */
	public static String getRace() {
		return getRace(SingletonRandom.RANDOM);
	}

	/**
	 * @return the collection of races.
	 */
	public static Collection<String> getRaces() {
		return Collections.unmodifiableList(RACES);
	}
}
