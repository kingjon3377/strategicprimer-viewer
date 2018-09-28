import javax.swing {
    DefaultListModel
}
import strategicprimer.model.common.map {
    Player
}
import strategicprimer.drivers.common {
    MapChangeListener
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel
}
"A list model for players in the exploration GUI."
class PlayerListModel(IExplorationModel model) extends DefaultListModel<Player>()
        satisfies MapChangeListener {
    model.playerChoices.each(addElement);
    shared actual void mapChanged() {
        clear();
        model.playerChoices.each(addElement);
    }
}
