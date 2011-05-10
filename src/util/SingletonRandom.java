package util;

import java.util.Random;

/**
 * A wrapper class for a single Random for the whole application.
 * 
 * @author Jonathan Lovelace
 * 
 */
public final class SingletonRandom {
	/**
	 * The singleton Random.
	 */
	public static final Random RANDOM = new Random(System.currentTimeMillis());

	/**
	 * Private constructor so we can't instantiate this class.
	 */
	private SingletonRandom() {
		// Do nothing
	}
}
