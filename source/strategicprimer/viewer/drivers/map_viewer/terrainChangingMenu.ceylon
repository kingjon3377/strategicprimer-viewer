import java.awt.event {
    ActionEvent
}

import javax.swing {
    JMenuItem,
    JPopupMenu
}

import strategicprimer.drivers.common {
    VersionChangeListener,
    SelectionChangeListener,
    SelectionChangeSource,
    PlayerChangeListener
}
import strategicprimer.drivers.worker.common {
    NewUnitListener
}
import strategicprimer.model.idreg {
    createIDFactory
}
import strategicprimer.model.map {
    Point,
    TileType,
    invalidPoint
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.viewer.drivers {
    SPDialog
}
import strategicprimer.viewer.drivers.worker_mgmt {
    NewUnitSource
}
"A popup menu to let the user change a tile's terrain type, or add a unit."
JPopupMenu&VersionChangeListener&SelectionChangeSource&SelectionChangeListener
terrainChangingMenu(Integer mapVersion, IViewerModel model) {
    SPDialog&NewUnitSource&PlayerChangeListener nuDialog =
            newUnitDialog(model.map.currentPlayer,
                createIDFactory(model.map));
    SelectionChangeSupport scs = SelectionChangeSupport();
    JMenuItem newUnitItem = JMenuItem("Add New Unit");
    variable Point point = invalidPoint;
    nuDialog.addNewUnitListener(object satisfies NewUnitListener {
        shared actual void addNewUnit(IUnit unit) {
            model.map.addFixture(point, unit);
            model.selection = point;
            scs.fireChanges(null, point);
        }
    });
    object retval extends JPopupMenu() satisfies VersionChangeListener&
            SelectionChangeListener&SelectionChangeSource {
        void updateForVersion(Integer version) {
            removeAll();
            for (type in TileType.valuesForVersion(version)) {
                JMenuItem item = JMenuItem(type.string);
                add(item);
                item.addActionListener((ActionEvent event) {
                    model.map.baseTerrain[point] = type;
                    scs.fireChanges(null, point);
                });
            }
            addSeparator();
            add(newUnitItem);
        }
        shared actual void changeVersion(Integer old, Integer newVersion) =>
                updateForVersion(newVersion);
        shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
                scs.addSelectionChangeListener(listener);
        shared actual void removeSelectionChangeListener(SelectionChangeListener listener)
                => scs.removeSelectionChangeListener(listener);
        shared actual void selectedPointChanged(Point? old, Point newPoint) {
            point = newPoint;
            if (newPoint.valid,
//                TileType.notVisible != model.map.baseTerrain[newPoint]) { // TODO: syntax sugar once compiler bug fixed
                TileType.notVisible != model.map.baseTerrain.get(newPoint)) {
                newUnitItem.enabled = true;
            } else {
                newUnitItem.enabled = false;
            }
        }
        updateForVersion(mapVersion);
    }
    newUnitItem.addActionListener((ActionEvent event) => nuDialog.setVisible(true));
    nuDialog.dispose();
    return retval;
}
