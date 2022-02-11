package utility;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;

/* package */ final class LandRaces {
	private LandRaces() {}
	/**
	 * List of non-aquatic races.
	 *
	 * Left outside {@link MapCheckerCLI} because it's also used in {@link TodoFixerCLI}.
	 *
	 * TODO: Move into one of those classes, since unlike in Ceylon it has
	 * to be in a class and making a separate class just for this seems
	 * silly.
	 */
	public static final List<String> LAND_RACES = Collections.unmodifiableList(
		Arrays.asList("Danan", "dwarf", "elf", "half-elf", "gnome", "human"));
}
