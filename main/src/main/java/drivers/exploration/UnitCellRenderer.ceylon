import javax.swing {
    DefaultListCellRenderer,
    ListCellRenderer,
    JLabel,
    SwingList=JList
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import java.awt {
    Component
}

class UnitCellRenderer satisfies ListCellRenderer<IUnit> {
    static DefaultListCellRenderer defaultRenderer = DefaultListCellRenderer();
    shared new () {}
    shared actual Component getListCellRendererComponent(
            SwingList<out IUnit>? list, IUnit? val, Integer index,
            Boolean isSelected, Boolean cellHasFocus) {
        Component retval = defaultRenderer.getListCellRendererComponent(list,
            val, index, isSelected, cellHasFocus);
        if (exists val, is JLabel retval) {
            retval.text = "``val.name`` (``val.kind``)";
        }
        return retval;
    }
}
