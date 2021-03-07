import javax.swing {
    DefaultListModel
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

import strategicprimer.drivers.common {
    PlayerChangeListener
}

import strategicprimer.model.common.map {
    Player
}

import strategicprimer.drivers.exploration.common {
    IExplorationModel
}

"The list model for the list of units to choose the explorer (or otherwise moving unit)
 from."
class UnitListModel(IExplorationModel model) extends DefaultListModel<IUnit>()
        satisfies PlayerChangeListener {
    shared actual void playerChanged(Player? old, Player newPlayer) {
        log.trace("Regenerating UnitListModel");
        if (exists old, old == newPlayer) {
            return;
        }
        clear();
        model.getUnits(newPlayer).each(addElement);
    }
}
