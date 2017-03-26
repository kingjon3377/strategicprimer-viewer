import java.awt.event {
    ActionEvent
}

import javax.swing {
    JMenuItem,
    JPopupMenu
}

import model.listeners {
    SelectionChangeSupport,
    VersionChangeListener,
    PlayerChangeListener,
    SelectionChangeListener,
    SelectionChangeSource
}
import model.map {
    PointFactory,
    Point,
    TileType
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}

import strategicprimer.viewer.drivers {
    SPDialog,
    createIDFactory
}
import strategicprimer.viewer.drivers.worker_mgmt {
    NewUnitSource,
    NewUnitListener
}
"A popup menu to let the user change a tile's terrain type, or add a unit."
JPopupMenu&VersionChangeListener&SelectionChangeSource&SelectionChangeListener
terrainChangingMenu(Integer mapVersion, IViewerModel model) {
    SPDialog&NewUnitSource&PlayerChangeListener nuDialog =
            newUnitDialog(model.map.currentPlayer,
                createIDFactory(model.map));
    SelectionChangeSupport scs = SelectionChangeSupport();
    JMenuItem newUnitItem = JMenuItem("Add New Unit");
    variable Point point = PointFactory.invalidPoint;
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
                    model.map.setBaseTerrain(point, type);
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
                TileType.notVisible != model.map.getBaseTerrain(newPoint)) {
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
