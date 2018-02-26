import strategicprimer.model.map.fixtures {
	SPNumber
}
"An interface for fixtures representing large features that report their extent in acres."
shared interface HasExtent {
	shared formal SPNumber acres;
}