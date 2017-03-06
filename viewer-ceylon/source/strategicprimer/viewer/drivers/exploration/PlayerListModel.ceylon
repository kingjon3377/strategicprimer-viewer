import javax.swing {
    DefaultListModel
}
import model.map {
    Player
}
import model.listeners {
    MapChangeListener
}
import model.exploration {
    IExplorationModel
}
"A list model for players in the exploration GUI."
class PlayerListModel(IExplorationModel model) extends DefaultListModel<Player>()
        satisfies MapChangeListener {
    for (player in model.playerChoices) {
        addElement(player);
    }
    shared actual void mapChanged() {
        clear();
        for (player in model.playerChoices) {
            addElement(player);
        }
    }
}
