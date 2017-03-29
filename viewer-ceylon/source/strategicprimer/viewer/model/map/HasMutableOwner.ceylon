import model.map {
    HasOwner,
    Player
}
"An interface for things that are owned by a player whose owner can change."
shared interface HasMutableOwner satisfies HasOwner {
	"The owner of the whatever-this is, now `variable`."
	shared actual formal variable Player owner;
}