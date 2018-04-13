import lovelace.util.jvm {
    singletonRandom
}
import ceylon.random {
    Random
}

shared object raceFactory {
	"""A list of races for use in the map. All of them are equally likely except
	   human, which is more likely than the others put together."""
	shared String[] races = ["dwarf", "elf", "gnome", "half-elf", "Danan"].chain(Singleton("human").cycled)
			.take(20).sequence();
	"Select a race at random."
	shared String randomRace(
			"The RNG to use to determine the result."
			Random random = singletonRandom) {
		assert (exists retval = random.nextElement(races));
		return retval;
	}
}
