import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    IFixture
}

"Something that can go on a tile."
todo("Any other members?")
shared interface TileFixture satisfies IFixture&Comparable<TileFixture> {
    "A *short*, no more than one line and preferably no more than two dozen characters,
     description of the fixture, suitable for saying what it is when an explorer happens
     on it."
    shared formal String shortDescription;

    "Clone the object."
    shared actual formal TileFixture copy(Boolean zero);

    "Compare to another fixture."
    shared actual default Comparison compare(TileFixture other) => hash <=> other.hash;

    """The required Perception check for an explorer to find the fixture.

       Some rough guidelines for the scale:

       - 0 is "impossible to miss": the type of terrain you pass through
       - 10 and under is "hard to miss": forests, mountains, rivers, perhaps hills
       - 10-20 is "not hard to spot": shrubs, active populations
       - 20-30 is "you have to be observant": ruins, etc.
       - 30+ is generally "*really* observant or specialized equipment": unexposed mineral
         deposits, portals to other worlds, etc."""
    todo("In many or most cases, DCs should take surrounding-terrain context into account,
          and so this shouldn't be an instance function here, but either on the map, in a
          separate class, or in a toplevel function.")
    shared formal Integer dc;
}
