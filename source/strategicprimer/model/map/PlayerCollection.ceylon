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
        if (exists retval = players[player]) {
            return retval;
        } else if (player < 0) {
            return PlayerImpl(player, "");
        } else {
            Player retval = PlayerImpl(player, "");
            players[player] = retval;
            return retval;
        }
    }
    "An iterator over the players in the collection."
    shared actual Iterator<Player> iterator() => players.items.iterator();
    "A player collection is a subset if it has no players we don't."
    todo("Compare corresponding players!")
    shared actual Boolean isSubset({Player*} obj, Anything(String) report) {
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
    variable Player current = PlayerImpl(-1, "");
    "Add a player to the collection. Returns true if the collection changed as a result of
     this call"
    shared actual void add(Player player) {
        if (player.independent) {
            independentPlayer = player;
        }
        if (player.current, (current.playerId < 0 || !current.current)) {
            current = player;
        }
        players[player.playerId] = player;
    }
    "Remove a player from the collection. Returns true if the collection changed as a
     result of this call."
    shared actual void remove(Player|Integer obj) {
        Player? removed;
        switch (obj)
        case (is Integer) {
            removed = players.remove(obj);
        }
        case (is Player) {
            removed = players.remove(obj.playerId);
        }
        if (exists removed) {
            if (independentPlayer == removed) {
                independentPlayer = find(Player.independent) else PlayerImpl(-1, "Independent");
            }
            if (current == removed) {
                current = PlayerImpl(-1, "");
            }
        }
    }
    """The player for "independent" fixtures."""
    shared actual Player independent => independentPlayer;
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
    "Get the current player, or a new player with an empty name and number -1."
    shared actual Player currentPlayer => current;
    assign currentPlayer {
        Player oldCurrent = current;
        if (is MutablePlayer oldCurrent) {
            oldCurrent.current = false;
        } else {
            log.warn("Previous current player wasn't mutable");
        }
        if (contains(currentPlayer)) {
            current = currentPlayer;
        } else if (exists temp = find((player) => player.playerId == currentPlayer.playerId)) {
            current = temp;
        } else {
            current = currentPlayer;
        }
        if (is MutablePlayer temp = current) {
            temp.current = true;
        } else {
            log.warn(
                "Player in collection matching specified 'new' player wasn't mutable");
        }
    }
    "An object is equal iff it is a player collection with exactly the players we have."
    shared actual Boolean equals(Object obj) {
        if (is IPlayerCollection obj) {
            return isSubset(obj, noop) && obj.isSubset(this, noop);
        } else {
            return false;
        }
    }
}
