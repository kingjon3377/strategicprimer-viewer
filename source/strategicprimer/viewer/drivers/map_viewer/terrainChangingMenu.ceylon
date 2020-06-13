import java.awt.event {
    ActionEvent
}

import javax.swing {
    JMenuItem,
    JPopupMenu,
    JCheckBoxMenuItem
}

import strategicprimer.drivers.common {
    VersionChangeListener,
    SelectionChangeListener
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
        satisfies VersionChangeListener&SelectionChangeListener {
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
            model.interaction = null;
        }
    });

    JCheckBoxMenuItem mountainItem = JCheckBoxMenuItem("Mountainous");

    void toggleMountains() {
        Point localPoint = point;
        if (localPoint.valid, exists terrain = model.map.baseTerrain[localPoint],
                terrain != TileType.ocean) {
            Boolean newValue = !model.map.mountainous.get(localPoint);
            model.map.mountainous[localPoint] = newValue;
            model.mapModified = true;
            mountainItem.model.selected = newValue;
            scs.fireChanges(null, localPoint);
        }
        model.interaction = null;
    }

    mountainItem.addActionListener(silentListener(toggleMountains));

    JCheckBoxMenuItem bookmarkItem = JCheckBoxMenuItem("Bookmarked");

    void toggleBookmarked() {
        Point localPoint = point;
        if (localPoint in model.map.bookmarks) {
            model.map.removeBookmark(localPoint);
            bookmarkItem.model.selected = false;
        } else {
            model.map.addBookmark(localPoint);
            bookmarkItem.model.selected = true;
        }
        model.mapModified = true;
        scs.fireChanges(null, localPoint);
        model.interaction = null;
    }

    bookmarkItem.addActionListener(silentListener(toggleBookmarked));

    void toggleRiver(River river, JCheckBoxMenuItem item)() {
        Point localPoint = point;
        if (localPoint.valid, exists terrain = model.map.baseTerrain[localPoint],
                terrain != TileType.ocean) {
            if (river in model.map.rivers.get(localPoint)) {
                model.map.removeRivers(localPoint, river);
                item.model.selected = false;
            } else {
                model.map.addRivers(localPoint, river);
                item.model.selected = true;
            }
            model.mapModified = true;
            scs.fireChanges(null, localPoint);
            model.interaction = null;
        }
    }

    MutableMap<River, JCheckBoxMenuItem> riverItems = HashMap<River, JCheckBoxMenuItem>();
    for (direction in `River`.caseValues) {
        JCheckBoxMenuItem item = JCheckBoxMenuItem(direction.description + " river");
        item.addActionListener(silentListener(toggleRiver(direction, item)));
        riverItems[direction] = item;
    }

    // TODO: Make some way to manipulate roads?

    void updateForVersion(Integer version) {
        removeAll();
        add(bookmarkItem);
        addSeparator();
        for (type in TileType.valuesForVersion(version)) {
            JMenuItem item = JMenuItem(type.string);
            add(item);
            item.addActionListener((ActionEvent event) {
                model.map.baseTerrain[point] = type;
                model.mapModified = true;
                scs.fireChanges(null, point);
                model.interaction = null;
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

    shared actual void selectedPointChanged(Point? old, Point newPoint) {}

    shared actual void interactionPointChanged() {
        // We default to the selected point if the model has no interaction point, in case the menu gets shown before
        // the interaction point gets set somehow.
        value localPoint = model.interaction else model.selection;
        point = localPoint;
        if (point.valid, model.map.baseTerrain[point] exists) {
            newUnitItem.enabled = true;
        } else {
            newUnitItem.enabled = false;
        }
        if (point.valid, exists terrain = model.map.baseTerrain[point],
                terrain != TileType.ocean) {
//          mountainItem.model.selected = model.map.mountainous[newPoint]; // TODO: syntax sugar once compiler bug fixed
            mountainItem.model.selected = model.map.mountainous.get(point);
            mountainItem.enabled = true;
        } else {
            mountainItem.model.selected = false;
            mountainItem.enabled = false;
        }
        if (point.valid, exists terrain = model.map.baseTerrain[point],
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
        bookmarkItem.model.selected = localPoint in model.map.bookmarks;
    }

    shared actual void selectedUnitChanged(IUnit? oldSelection, IUnit? newSelection) {}

    updateForVersion(mapVersion);
    // Can't use silentListener(nuDialog.showWindow): triggers eclipse/ceylon#7379
    void showDialogImpl(ActionEvent event) => nuDialog.setVisible(true);
    newUnitItem.addActionListener(showDialogImpl);
    nuDialog.dispose();
}

