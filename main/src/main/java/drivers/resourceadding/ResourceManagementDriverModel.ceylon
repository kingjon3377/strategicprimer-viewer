import strategicprimer.model.common.map.fixtures.towns {
    IMutableFortress
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel
}

import strategicprimer.model.common.map.fixtures {
    FortressMember,
    IMutableResourcePile,
    IResourcePile,
    Quantity,
    ResourcePileImpl
}
import strategicprimer.model.common.map {
    IMapNG,
    Player,
    IMutableMapNG
}

import ceylon.decimal {
    Decimal
}

import lovelace.util.common {
    matchingValue
}

"A driver model for resource-entering drivers."
class ResourceManagementDriverModel extends SimpleMultiMapModel {
    shared new fromMap(IMutableMapNG map) extends SimpleMultiMapModel(map) { }
    shared new fromDriverModel(IDriverModel driverModel) extends
        SimpleMultiMapModel.copyConstructor(driverModel) { }

    "All the players in all the maps."
    shared {Player*} players => allMaps.flatMap(IMapNG.players).distinct;

    "Add a resource to a player's HQ."
    shared void addResource(FortressMember resource, Player player) {
        for (map in restrictedAllMaps) {
            Player mapPlayer = map.currentPlayer;
            if (mapPlayer.independent || mapPlayer.playerId < 0 ||
                    mapPlayer.playerId == player.playerId,
                    exists fortress = map.fixtures.items.narrow<IMutableFortress>()
                        .filter(matchingValue("HQ", IMutableFortress.name))
                        .find(matchingValue(mapPlayer.playerId,
                            compose(Player.playerId, IMutableFortress.owner)))) {
                fortress.addMember(resource);
                map.modified = true;
            } // TODO: Else log why we're skipping the map
        }
    }

    shared IResourcePile addResourcePile(Player player, Integer id, String kind, String resource, Decimal quantity, String units,
            Integer? created) {
        IMutableResourcePile pile = ResourcePileImpl(id, kind, resource, Quantity(quantity, units));
        if (exists created) {
            pile.created = created;
        }
        addResource(pile, player);
        return pile;
    }

    "Get the current player. If none is current, returns null."
    shared Player? currentPlayer => players.find(Player.current);
}
