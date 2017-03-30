import lovelace.util.common {
    todo
}
import model.map {
    Player
}
"An interface for player collections that can be modified."
shared interface IMutablePlayerCollection satisfies IPlayerCollection {
	"Add a player to the collection."
	todo("Do we really need the return value?")
	shared formal Boolean add(Player player);
	"Remove a player from the collection."
	todo("Do we really need the return value?")
	shared formal Boolean remove(Player|Integer obj);
	"Clone the collection."
	shared actual formal IMutablePlayerCollection copy();
}