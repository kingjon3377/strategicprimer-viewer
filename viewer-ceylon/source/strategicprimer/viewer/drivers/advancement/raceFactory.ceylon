import ceylon.test {
    test,
    assertEquals
}

import util {
    SingletonRandom {
        singletonRandom=random
    }
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
        "A random-integer function taking its upper bound as a parameter."
        Integer(Integer) random = (Integer bound) => singletonRandom.nextInt(bound)) {
    assert (exists retval = races[random(races.size)]);
    return retval;
}