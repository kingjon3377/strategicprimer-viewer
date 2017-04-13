import ceylon.collection {
    MutableMap,
    naturalOrderTreeMap
}
import ceylon.interop.java {
    createJavaObjectArray
}

import java.lang {
    ObjectArray
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    IMutablePlayerCollection,
    Player,
    IPlayerCollection
}

"A collection of players. Using a simple List doesn't work when -1 is the default index if
 one isn't given in the XML."
shared class PlayerCollection() satisfies IMutablePlayerCollection {
	"The collection this class wraps."
	MutableMap<Integer, Player> players = naturalOrderTreeMap<Integer, Player>({});
	"""The player for "independent" fixtures."""
	variable Player independentPlayer = PlayerImpl(-1, "Independent");
	"Get a player by ID number."
	shared actual Player getPlayer(Integer player) {
		if (exists retval = players.get(player)) {
			return retval;
		} else {
			return PlayerImpl(player, "");
		}
	}
	"An iterator over the players in the collection."
	shared actual Iterator<Player> iterator() => players.items.iterator();
	"A player collection is a subset if it has no players we don't."
	todo("Compare corresponding players!")
	shared actual Boolean isSubset(IPlayerCollection obj, Anything(String) report) {
		variable Boolean retval = true;
		for (player in obj) {
			if (!players.items.contains(player)) {
				report("Extra player ``player.name``");
				retval = false;
			}
		}
		return retval;
	}
	shared actual Integer hash => players.hash;
	shared actual String string => "Player collection with ``players.size`` players";
	"Add a player to the collection. Returns true if the collection changed as a result of
	 this call"
	todo("Do we really need the return value?")
	shared actual Boolean add(Player player) {
		if (player.independent) {
			independentPlayer = player;
		}
		Boolean retval = !players.items.contains(player);
		players.put(player.playerId, player);
		return retval;
	}
	"Remove a player from the collection. Returns true if the collection changed as a
	 result of this call."
	shared actual Boolean remove(Player|Integer obj) {
		Boolean retval;
		switch (obj)
		case (is Integer) {
			retval = players.defines(obj);
			players.remove(obj);
		}
		case (is Player) {
			retval = players.items.contains(obj);
			players.remove(obj.playerId);
		}
		return retval;
	}
	"""The player for "independent" fixtures."""
	shared actual Player independent = independentPlayer;
	"The players, as an array."
	shared actual ObjectArray<Player> asArray() =>
			createJavaObjectArray(players.items);
	"Clone the collection."
	shared actual IMutablePlayerCollection copy() {
		IMutablePlayerCollection retval = PlayerCollection();
		for (player in this) {
			retval.add(player);
		}
		return retval;
	}
	"Get the current player, or a new player with an empty name and number -1. Note that
	 this currently iterates through all players to find one marked current."
	todo("Keep a separate 'current' reference.")
	shared actual Player currentPlayer => find(Player.current) else PlayerImpl(-1, "");
	"An object is equal iff it is a player collection with exactly the players we have."
	shared actual Boolean equals(Object obj) {
		if (is IPlayerCollection obj) {
			return isSubset(obj, noop) && obj.isSubset(this, noop);
		} else {
			return false;
		}
	}
}