"An interface for fixtures representing large features that report their extent in acres."
see(`interface HasPopulation`)
shared interface HasExtent satisfies IFixture&Subsettable<IFixture> {
	shared formal Number<out Anything> acres;
}