"An interface for fixtures with an integer population (or quantity reported as such)."
see(`interface HasExtent`)
shared interface HasPopulation<Self> satisfies IFixture&Subsettable<IFixture>
        given Self satisfies HasPopulation<Self>&Object {
    "The population."
    shared formal Integer population;

    "Return a copy of this object, except with its population the specified value
     instead of its current value, and if an ID is specified that ID instead of its
     current ID."
    shared formal Self reduced(Integer newPopulation, Integer newId = id);

    "Return a copy of this object, except with its population increased to its current
     population plus that of the [[addend]], which must be of the same type."
    shared formal Self combined(Self addend);
}
