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
import strategicprimer.model.common.idreg {
    createIDFactory
}
import strategicprimer.model.common.map {
    Point,
    TileType
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import lovelace.util.common {
    silentListener
}

"A popup menu to let the user change a tile's terrain type, or add a unit."
class TerrainChangingMenu(Integer mapVersion, IViewerModel model) extends JPopupMenu()
        satisfies VersionChangeListener&SelectionChangeSource&SelectionChangeListener {
    NewUnitDialog nuDialog = NewUnitDialog(model.map.currentPlayer,
        createIDFactory(model.map));

    SelectionChangeSupport scs = SelectionChangeSupport();

    JMenuItem newUnitItem = JMenuItem("Add New Unit");
    variable Point point = Point.invalidPoint;
    nuDialog.addNewUnitListener(object satisfies NewUnitListener {
        shared actual void addNewUnit(IUnit unit) {
            model.map.addFixture(point, unit);
            model.mapModified = true;
            model.selection = point;
            scs.fireChanges(null, point);
        }
    });

    JMenuItem mountainItem = JMenuItem("Mountainous");

    void toggleMountains() {
        Point localPoint = point;
        if (localPoint.valid) {
            Boolean newValue = !mountainItem.model.selected;
            model.map.mountainous[localPoint] = newValue;
            model.mapModified = true;
            mountainItem.model.selected = newValue;
        }
    }

    mountainItem.addActionListener(silentListener(toggleMountains));

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
//        mountainItem.model.selected = model.map.mountainous[point]; // TODO: syntax sugar once compiler bug fixed
        mountainItem.model.selected = model.map.mountainous.get(point);
        add(mountainItem);
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

