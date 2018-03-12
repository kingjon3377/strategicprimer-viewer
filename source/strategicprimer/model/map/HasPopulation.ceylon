"An interface for fixtures with an integer population (or quantity reported as such)."
see(`interface HasExtent`)
shared interface HasPopulation/*<Self>*/ satisfies IFixture&Subsettable<IFixture>
		/* given Self satisfies HasPopulation<Self>&Object */ {
	shared formal Integer population;
	"Return a copy of this object, except with its population the specified value
	 instead of its current value."
	shared formal HasPopulation/*&Self*/ reduced(Integer newPopulation);
	"Return a copy of this object, except with its population increased to its current population
	 plus that of the [[addend]], which must be of the same type."
	shared formal HasPopulation/*&Self*/ combined(HasPopulation/*&Self*/ addend);
}