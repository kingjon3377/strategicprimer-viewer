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
    SelectionChangeListener,
    NewFixtureListener
}
import strategicprimer.drivers.worker.common {
    NewUnitListener
}
import strategicprimer.model.common.idreg {
    createIDFactory,
    IDRegistrar
}
import strategicprimer.model.common.map {
    Point,
    TileType,
    River,
    TileFixture
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
import strategicprimer.model.common.map.fixtures.terrain {
    Hill
}
import strategicprimer.drivers.gui.common {
    SPDialog
}

"A popup menu to let the user change a tile's terrain type, or add a unit."
class TerrainChangingMenu(Integer mapVersion, IViewerModel model) extends JPopupMenu()
        satisfies VersionChangeListener&SelectionChangeListener {
    IDRegistrar idf = createIDFactory(model.map);
    NewUnitDialog nuDialog = NewUnitDialog(model.map.currentPlayer, idf);

    SelectionChangeSupport scs = SelectionChangeSupport();

    JMenuItem newUnitItem = JMenuItem("Add New Unit ...");
    variable Point point = Point.invalidPoint;
    nuDialog.addNewUnitListener(object satisfies NewUnitListener {
        shared actual void addNewUnit(IUnit unit) {
            model.map.addFixture(point, unit);
            model.mapModified = true; // FIXME: Extract a method for the 'set modified flag, fire changes, reset interaction' procedure
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

    JCheckBoxMenuItem hillItem = JCheckBoxMenuItem("Hill(s)");

    void toggleHill() {
        Point localPoint = point;
        if (localPoint.valid, exists terrain = model.map.baseTerrain[localPoint],
                terrain != TileType.ocean) {
            if (model.map.fixtures.get(localPoint).narrow<Hill>().empty) { // TODO: syntax sugar
                // FIXME: get an ID factory somehow
                model.map.addFixture(localPoint, Hill(idf.createID()));
            } else {
                for (hill in model.map.fixtures.get(localPoint).narrow<Hill>()) { // TODO: syntax sugar
                    model.map.removeFixture(localPoint, hill);
                }
            }
            model.mapModified = true;
            hillItem.model.selected = !model.map.fixtures.get(localPoint).empty; // TODO: syntax sugar
            scs.fireChanges(null, localPoint);
        }
        model.interaction = null;
    }

    hillItem.addActionListener(silentListener(toggleHill));

    JMenuItem newForestItem = JMenuItem("Add New Forest ...");
    NewForestDialog nfDialog = NewForestDialog(idf);
    object newFixtureListener satisfies NewFixtureListener {
        shared actual void addNewFixture(TileFixture fixture) {
            model.map.addFixture(point, fixture);
            model.mapModified = true;
            model.selection = point; // TODO: We probably don't always want to do this ...
            scs.fireChanges(null, point);
            model.interaction = null;
        }
    }

    nfDialog.addNewFixtureListener(newFixtureListener);

    JMenuItem textNoteItem = JMenuItem("Add Text Note ...");
    Integer currentTurn() => model.map.currentTurn;
    TextNoteDialog tnDialog = TextNoteDialog(currentTurn);
    tnDialog.addNewFixtureListener(newFixtureListener);

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

    void removeTerrain(ActionEvent event) {
        model.map.baseTerrain[point] = null;
        model.mapModified = true;
        scs.fireChanges(null, point);
        model.interaction = null;
    }

    void updateForVersion(Integer version) {
        removeAll();
        add(bookmarkItem);
        add(textNoteItem);
        addSeparator();
        JMenuItem removalItem = JMenuItem("Remove terrain");
        add(removalItem);
        removalItem.addActionListener(removeTerrain);
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
        add(hillItem);
        add(newForestItem);
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
            mountainItem.model.selected =model.map.mountainous.get(point);
            mountainItem.enabled = true;
            hillItem.model.selected = !model.map.fixtures.get(point).narrow<Hill>().empty; // TODO: syntax sugar
            hillItem.enabled = true;
            newForestItem.enabled = true;
        } else {
            mountainItem.model.selected = false;
            mountainItem.enabled = false;
            hillItem.model.selected = false;
            hillItem.enabled = false;
            newForestItem.enabled = false;
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
    void showDialogImpl(SPDialog dialog)(ActionEvent event) => dialog.setVisible(true);
    newUnitItem.addActionListener(showDialogImpl(nuDialog));
    newForestItem.addActionListener(showDialogImpl(nfDialog));
    textNoteItem.addActionListener(showDialogImpl(tnDialog));
    nuDialog.dispose();
}

