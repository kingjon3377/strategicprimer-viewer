import javax.swing {
    DefaultListModel
}
import strategicprimer.model.map {
    Player
}
import strategicprimer.viewer.model {
    MapChangeListener
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
