import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.drivers.exploration.common {
    ExplorationModel
}

import lovelace.util.common {
    PathWrapper
}

import strategicprimer.drivers.common {
    IDriverModel
}

shared class TurnRunningModel extends ExplorationModel satisfies ITurnRunningModel {
    shared new (IMutableMapNG map, PathWrapper? file, Boolean modified = false)
        extends ExplorationModel(map, file, modified) {}
    shared new copyConstructor(IDriverModel model)
        extends ExplorationModel.copyConstructor(model) {}
}
