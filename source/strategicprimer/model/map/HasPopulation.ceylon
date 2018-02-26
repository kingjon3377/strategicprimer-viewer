"An interface for fixtures with an integer population (or quantity reported as such)."
see(`interface HasExtent`)
shared interface HasPopulation satisfies IFixture&Subsettable<IFixture> {
	shared formal Integer population;
}