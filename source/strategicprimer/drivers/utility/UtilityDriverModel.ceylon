import strategicprimer.model.common.map {
    IMutableMapNG
}

import strategicprimer.drivers.common {
    SimpleMultiMapModel,
    IDriverModel
}

import lovelace.util.common {
    PathWrapper
}

"A driver model for the various utility drivers."
shared class UtilityDriverModel extends SimpleMultiMapModel {
    shared new (IMutableMapNG map, PathWrapper? file, Boolean modified = false)
            extends SimpleMultiMapModel(map, file, modified) {}
    shared new copyConstructor(IDriverModel model)
            extends SimpleMultiMapModel.copyConstructor(model) {}
}
