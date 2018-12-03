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
    TileType,
    River
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import lovelace.util.common {
    silentListener
}
import ceylon.collection {
    MutableMap,
    HashMap
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

    void toggleMountains() { // TODO: Call scs.fireChanges()
        Point localPoint = point;
        if (localPoint.valid) { // TODO: Restrict to non-ocean, non-Mountain terrain
            Boolean newValue = !mountainItem.model.selected;
            model.map.mountainous[localPoint] = newValue;
            model.mapModified = true;
            mountainItem.model.selected = newValue;
        }
    }

    mountainItem.addActionListener(silentListener(toggleMountains));

	void toggleRiver(River river, JMenuItem item)() { // TODO: Call scs.fireChanges()
        Point localPoint = point;
        if (localPoint.valid, exists terrain = model.map.baseTerrain[localPoint],
                terrain != TileType.ocean) {
            if (item.model.selected) {
                model.map.removeRivers(localPoint, river);
                model.mapModified = true;
                item.model.selected = false;
            } else {
                model.map.addRivers(localPoint, river);
                model.mapModified = true;
                item.model.selected = true;
            }
        }
    }

    MutableMap<River, JMenuItem> riverItems = HashMap<River, JMenuItem>();
    for (direction in `River`.caseValues) {
        JMenuItem item = JMenuItem(direction.description + " river");
        item.addActionListener(silentListener(toggleRiver(direction, item)));
        riverItems[direction] = item;
    }

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
		add(mountainItem);
        for (direction->item in riverItems) {
            add(item);
        }
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
//        mountainItem.model.selected = model.map.mountainous[newPoint]; // TODO: syntax sugar once compiler bug fixed
        mountainItem.model.selected = model.map.mountainous.get(newPoint); // TODO: Disable it when listener will ignore it
        if (newPoint.valid, exists terrain = model.map.baseTerrain[newPoint],
                terrain != TileType.ocean) {
//        {River*} rivers = model.map.rivers[point]; // TODO: syntax sugar
            {River*} rivers = model.map.rivers.get(point);
            for (direction->item in riverItems) {
                item.enabled = true;
                item.model.selected = direction in rivers;
            }
        } else {
            for (item in riverItems.items) {
                item.model.selected = false;
                item.enabled = false;
            }
        }
    }

    updateForVersion(mapVersion);
    // Can't use silentListener(nuDialog.showWindow) because it triggers eclipse/ceylon#7379
    newUnitItem.addActionListener((ActionEvent event) => nuDialog.setVisible(true));
    nuDialog.dispose();
}

