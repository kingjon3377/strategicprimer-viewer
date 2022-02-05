package common.map.fixtures.mobile.worker;

import lovelace.util.SingletonRandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RaceFactory {
	private RaceFactory() {
	}

	/**
	 * A list of races for use in the map. All of them are equally likely
	 * except human, which is more likely than the others put together.
	 */
	public static final List<String> RACES = Collections.unmodifiableList(
		Stream.concat(Stream.of("dwarf", "elf", "gnome", "half-elf", "Danan"),
			Stream.generate(() -> "human")).limit(20)
			.collect(Collectors.toList()));

	/**
	 * Select a race at random.
	 *
	 * @param random The RNG to use to determine the result.
	 */
	public static String randomRace(final Random random) {
		return RACES.get(random.nextInt(RACES.size()));
	}

	/**
	 * Select a race at random.
	 */
	public static String randomRace() {
		return randomRace(SingletonRandom.SINGLETON_RANDOM);
	}
}
