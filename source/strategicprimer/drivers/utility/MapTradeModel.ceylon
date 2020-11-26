import strategicprimer.drivers.common {
    IDriverModel,
    SimpleMultiMapModel
}

import lovelace.util.common {
    PathWrapper
}

import strategicprimer.model.common.map {
    IMutableMapNG
}

shared class MapTradeModel extends SimpleMultiMapModel {
    shared new (IMutableMapNG map, PathWrapper? file, Boolean modified = false)
            extends SimpleMultiMapModel(map, file, modified) { }
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}
}
