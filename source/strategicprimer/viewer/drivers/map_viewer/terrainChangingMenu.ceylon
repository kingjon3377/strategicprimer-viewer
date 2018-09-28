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
    SelectionChangeSource
}
import strategicprimer.drivers.worker.common {
    NewUnitListener
}
import strategicprimer.model.impl.idreg {
    createIDFactory
}
import strategicprimer.model.impl.map {
    Point,
    TileType,
    invalidPoint
}
import strategicprimer.model.impl.map.fixtures.mobile {
    IUnit
}

"A popup menu to let the user change a tile's terrain type, or add a unit."
class TerrainChangingMenu(Integer mapVersion, IViewerModel model) extends JPopupMenu()
        satisfies VersionChangeListener&SelectionChangeSource&SelectionChangeListener {
    NewUnitDialog nuDialog = NewUnitDialog(model.map.currentPlayer,
        createIDFactory(model.map));
    SelectionChangeSupport scs = SelectionChangeSupport();
    JMenuItem newUnitItem = JMenuItem("Add New Unit");
    variable Point point = invalidPoint;
    nuDialog.addNewUnitListener(object satisfies NewUnitListener {
        shared actual void addNewUnit(IUnit unit) {
            model.map.addFixture(point, unit);
            model.mapModified = true;
            model.selection = point;
            scs.fireChanges(null, point);
        }
    });
    void updateForVersion(Integer version) {
        removeAll();
        for (type in TileType.valuesForVersion(version)) {
            JMenuItem item = JMenuItem(type.string);
            add(item);
            item.addActionListener((ActionEvent event) {
                model.map.baseTerrain[point] = type;
                model.mapModified = true;
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
        if (newPoint.valid, model.map.baseTerrain[newPoint] exists) {
            newUnitItem.enabled = true;
        } else {
            newUnitItem.enabled = false;
        }
    }
    updateForVersion(mapVersion);
    // Can't use silentListener(nuDialog.showWindow) because it triggers eclipse/ceylon#7379
    newUnitItem.addActionListener((ActionEvent event) => nuDialog.setVisible(true));
    nuDialog.dispose();
}

