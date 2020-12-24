import strategicprimer.model.common.map {
    Player,
    HasOwner,
    Point,
    IMapNG
}

import strategicprimer.model.common.map.fixtures.towns {
    ITownFixture
}

import strategicprimer.drivers.common {
    SPOptions,
    CLIDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.utility {
    UtilityDriverModel
}


"""A driver to update a player's map to include a certain minimum distance around allied
   villages."""
// FIXME: Write GUI for map-expanding driver
shared class ExpansionDriver(ICLIHelper cli, options, model) satisfies CLIDriver {
    shared actual SPOptions options;
    shared actual UtilityDriverModel model;

    // TODO: fat arrow once syntax sugar in place
    Boolean containsSwornVillage(IMapNG map, Player currentPlayer)(Point point) {
//        return map.fixtures[point].narrow<ITownFixture>() // TODO: syntax sugar once compiler bug fixed
        return map.fixtures.get(point).narrow<ITownFixture>()
            .map(HasOwner.owner).any(currentPlayer.equals);
    }

    shared actual void startDriver() {
        for (player in model.subordinateMaps.map(IMapNG.currentPlayer).filter(not(Player.independent))) {
            for (point in model.map.locations.filter(containsSwornVillage(model.map, player))) {
                model.expandAroundPoint(point, player);
            }
        }
    }
}
