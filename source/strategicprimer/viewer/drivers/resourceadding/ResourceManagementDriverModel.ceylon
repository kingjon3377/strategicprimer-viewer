import strategicprimer.model.common.map.fixtures.towns {
    Fortress
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
                    mapPlayer.playerId == player.playerId) {
                addResourceToMap(resource.copy(false), map, player);
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

    "Add a resource to a player's HQ in a particular map."
    shared void addResourceToMap(FortressMember resource, IMapNG map, Player player) {
        for (fixture in map.fixtures.items.narrow<Fortress>()) {
            if ("HQ" == fixture.name, player.playerId == fixture.owner.playerId) {
                fixture.addMember(resource);
            } // TODO: Set modified flag for that map
        }
    }

    "Get the current player. If none is current, returns null."
    shared Player? currentPlayer => players.find(Player.current);
}
