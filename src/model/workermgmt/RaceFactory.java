package model.workermgmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.SingletonRandom;

/**
 * A class to select a race at "random".
 *
 * @author Jonathan Lovelace
 *
 */
public final class RaceFactory {
	/**
	 * Do not instantiate.
	 */
	private RaceFactory() {
		// Static class.
	}

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
		// ESCA-JAVA0076:
		while (RACES.size() < 20) {
			RACES.add("human");
		}
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
}
