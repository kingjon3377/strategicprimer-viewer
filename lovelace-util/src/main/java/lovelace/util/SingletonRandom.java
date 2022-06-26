package lovelace.util;

import java.util.Random;

/**
 * A single {@link Random} instance for the whole application.
 */
public final class SingletonRandom {
	private SingletonRandom() {
	}

	public static final Random SINGLETON_RANDOM = new Random();
}
