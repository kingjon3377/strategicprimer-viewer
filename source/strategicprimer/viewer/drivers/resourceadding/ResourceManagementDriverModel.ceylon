import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel
}
import lovelace.util.common {
    PathWrapper
}
import strategicprimer.model.common.map.fixtures {
    FortressMember
}
import strategicprimer.model.common.map {
    IMapNG,
    Player,
    IMutableMapNG
}

"A driver model for resource-entering drivers."
class ResourceManagementDriverModel extends SimpleMultiMapModel {
    shared new fromMap(IMutableMapNG map, PathWrapper? file) extends
        SimpleMultiMapModel(map, file) { }
    shared new fromDriverModel(IDriverModel driverModel) extends
        SimpleMultiMapModel.copyConstructor(driverModel) { }

    "All the players in all the maps."
    shared {Player*} players =>
        allMaps.map(Entry.key).flatMap(IMapNG.players).distinct;

    "Add a resource to a player's HQ."
    shared void addResource(FortressMember resource, Player player) {
        for (map->[file, modifiedFlag] in allMaps) {
            Player mapPlayer = map.currentPlayer;
            if (mapPlayer.independent || mapPlayer.playerId < 0 ||
            mapPlayer.playerId == player.playerId) {
                addResourceToMap(resource.copy(false), map, player);
                if (!modifiedFlag) {
                    setModifiedFlag(map, true);
                }
            } // TODO: Else log why we're skipping the map
        }
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
