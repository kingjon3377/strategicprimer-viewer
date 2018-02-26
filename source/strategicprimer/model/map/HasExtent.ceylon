import strategicprimer.model.map.fixtures {
	SPNumber
}
"An interface for fixtures representing large features that report their extent in acres."
see(`interface HasPopulation`)
shared interface HasExtent {
	shared formal SPNumber acres;
}