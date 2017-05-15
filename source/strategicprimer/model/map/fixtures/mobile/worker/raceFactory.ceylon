import ceylon.test {
    test,
    assertEquals
}

import lovelace.util.jvm {
    singletonRandom
}
import ceylon.random {
    Random
}

"""A list of races for use in the map. All of them are equally likely except
   human, which is more likely than the others put together."""
shared String[] races = ["dwarf", "elf", "gnome", "half-elf", "Danan",
    *{"human"}.repeat(15)];
"""The Java version programmatically filled up the list to a desired size;
   there's no way to do that AFAICS in Ceylon (with the sort of declarative
   initialization/immutable data structures that are otherwise ideal), so I added a test
   method to assert that the total size is correct."""
test
void testRaceSetup() {
    assertEquals(races.size, 20,
        "With non-human races and the filler-count of 'human', list should be size 20.");
}
"Select a race at random."
shared String randomRace(
        "The RNG to use to determine the result."
        Random random = singletonRandom) {
    assert (exists retval = random.nextElement(races));
    return retval;
}