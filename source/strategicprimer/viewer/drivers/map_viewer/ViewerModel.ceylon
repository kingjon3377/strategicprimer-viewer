import ceylon.collection {
    ArrayList,
    HashSet,
    MutableList,
    MutableSet
}

import lovelace.util.common {
    matchingValue,
    todo
}

import strategicprimer.drivers.common {
    SimpleDriverModel,
    IDriverModel,
    SelectionChangeListener
}
import strategicprimer.model.common.map {
    Point,
    HasKind,
    HasMutableKind,
    HasMutableName,
    HasMutableOwner,
    HasOwner,
    IFixture,
    IMutableMapNG,
    Player,
    River,
    TileFixture,
    TileType
}

import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.model.common.map.fixtures.mobile {
    IMutableUnit,
    IUnit
}

import strategicprimer.model.common.map.fixtures.towns {
    IFortress,
    IMutableFortress
}

"A class to encapsulate the various model-type things views need to do with maps."
todo("Tests")
shared class ViewerModel extends SimpleDriverModel satisfies IViewerModel {
    "The starting zoom level."
    shared static Integer defaultZoomLevel = 8;

    "The maximum zoom level, to make sure that the tile size never overflows."
    static Integer maxZoomLevel = runtime.maxArraySize / 2;

    "The distance a 'jump' will take the cursor."
    static Integer jumpInterval = 5;

    static {IFixture*} flattenIncluding(IFixture fixture) {
        if (is {IFixture*} fixture) {
            return fixture.follow(fixture);
        } else {
            return Singleton(fixture);
        }
    }

    "If the item in the entry is a [[fortress|IFortress]], return a stream of its
     contents paired with its location; otherwise, return a [[Singleton]] of
     the argument."
    static {<Point->IFixture>*} flattenEntries(Point->IFixture entry) {
        if (is IFortress item = entry.item) {
            return item.map((each) => entry.key->each);
        } else {
            return Singleton(entry);
        }
    }

    "If [[fixture]] is a [[fortress|IFortress]], return it; otherwise, return a Singleton
     containing it. This is intended to be used in [[Iterable.flatMap]]."
    static {IFixture*} unflattenNonFortresses(TileFixture fixture) {
        if (is IFortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    "The list of graphical-parameter listeners."
    MutableList<GraphicalParamsListener> gpListeners =
            ArrayList<GraphicalParamsListener>();

    "The object to handle notifying selection-change listeners."
    SelectionChangeSupport scs = SelectionChangeSupport();

    "Previously dismissed members."
    MutableSet<UnitMember> dismissedMembers = HashSet<UnitMember>();

    "The current zoom level."
    variable Integer _zoomLevel = defaultZoomLevel;

    "The current zoom level."
    shared actual Integer zoomLevel => _zoomLevel;

    "Zoom in, increasing the zoom level."
    shared actual void zoomIn() {
        if (_zoomLevel < maxZoomLevel) {
            Integer oldZoom = _zoomLevel;
            _zoomLevel++;
            Integer newZoom = _zoomLevel;
            for (listener in gpListeners) {
                listener.tileSizeChanged(oldZoom, newZoom);
            }
        }
    }

    "Zoom out, decreasing the zoom level."
    shared actual void zoomOut() {
        if (_zoomLevel > 1) {
            Integer oldZoom = _zoomLevel;
            _zoomLevel--;
            Integer newZoom = _zoomLevel;
            for (listener in gpListeners) {
                listener.tileSizeChanged(oldZoom, newZoom);
            }
        }
    }

    "The currently selected point in the main map."
    variable Point selPoint = Point.invalidPoint;

    "The point currently pointed to by the scroll-bars."
    variable Point cursorPoint = Point(0, 0);

    "The point in the map the user has just right-clicked on, if any."
    variable Point? interactionPoint = null;

    "The visible dimensions of the map."
    variable VisibleDimensions visDimensions;

    shared new ("The initial map" IMutableMapNG theMap)
            extends SimpleDriverModel(theMap) {
        visDimensions = VisibleDimensions(0, theMap.dimensions.rows - 1, 0,
            theMap.dimensions.columns - 1);
    }

    shared new copyConstructor(IDriverModel model)
            extends SimpleDriverModel(model.restrictedMap) {
        if (is IViewerModel model) {
            visDimensions = model.visibleDimensions;
            selPoint = model.selection;
            _zoomLevel = model.zoomLevel;
        } else {
            visDimensions = VisibleDimensions(0, model.mapDimensions.rows - 1,
                0, model.mapDimensions.columns - 1);
            _zoomLevel = defaultZoomLevel;
        }
    }

    "The visible dimensions of the map."
    shared actual VisibleDimensions visibleDimensions => visDimensions;
    assign visibleDimensions {
        if (visDimensions != visibleDimensions) {
            VisibleDimensions oldDimensions = visDimensions;
            visDimensions = visibleDimensions;
            // We notify listeners after the change, since one object's
            // dimensionsChanged() delegates to repaint(). (The other uses the parameter
            // we provide for robustness.)
            for (listener in gpListeners) {
                listener.dimensionsChanged(oldDimensions, visibleDimensions);
            }
        }
    }

    void fixVisibility() {
        Point currSelection = selPoint;
        VisibleDimensions currDims = visDimensions;
        Integer row;
        if (currSelection.row < 0) {
            row = 0;
        } else if (currSelection.row >= map.dimensions.rows) {
            row = map.dimensions.rows - 1;
        } else {
            row = currSelection.row;
        }
        Integer column;
        if (currSelection.column < 0) {
            column = 0;
        } else if (currSelection.column >= map.dimensions.columns) {
            column = map.dimensions.columns - 1;
        } else {
            column = currSelection.column;
        }
        Integer minRow;
        Integer maxRow;
        if (currDims.rows.contains(row)) {
            minRow = currDims.minimumRow;
            maxRow = currDims.maximumRow;
        } else if (currDims.minimumRow > row, currDims.minimumRow - row <= jumpInterval) {
            minRow = row;
            maxRow = currDims.maximumRow - (currDims.minimumRow - row);
        } else if (currDims.maximumRow < row, row - currDims.maximumRow <= jumpInterval) {
            minRow = currDims.minimumRow + (row - currDims.maximumRow);
            maxRow = row;
        } else if ((0:currDims.height).contains(row)) {
            minRow = 0;
            maxRow = currDims.height - 1;
        } else if (((map.dimensions.rows - currDims.height)..(map.dimensions.rows - 1))
                .contains(row)) {
            minRow = map.dimensions.rows - currDims.height;
            maxRow = map.dimensions.rows - 1;
        } else {
            minRow = row - currDims.height / 2;
            maxRow = minRow + currDims.height - 1;
        }
        Integer minColumn;
        Integer maxColumn;
        if (currDims.columns.contains(column)) {
            minColumn = currDims.minimumColumn;
            maxColumn = currDims.maximumColumn;
        } else if (currDims.minimumColumn > column,
                currDims.minimumColumn - column <= jumpInterval) {
            minColumn = column;
            maxColumn = currDims.maximumColumn - (currDims.minimumColumn - column);
        } else if (currDims.maximumColumn < column,
                column - currDims.maximumColumn <= jumpInterval) {
            minColumn = currDims.minimumColumn + (column - currDims.maximumColumn);
            maxColumn = column;
        } else if ((0:currDims.width).contains(column)) {
            minColumn = 0;
            maxColumn = currDims.width - 1;
        } else if (column in
                (map.dimensions.columns - currDims.width)..(map.dimensions.columns - 1)) {
            minColumn = map.dimensions.columns - currDims.width;
            maxColumn = map.dimensions.columns - 1;
        } else {
            minColumn = column - currDims.width / 2;
            maxColumn = minColumn + currDims.width - 1;
        }
        visibleDimensions = VisibleDimensions(minRow, maxRow, minColumn, maxColumn);
    }

    "Reset the zoom level to the default."
    shared actual void resetZoom() {
        Integer old = _zoomLevel;
        _zoomLevel = defaultZoomLevel;
        for (listener in gpListeners) {
            listener.tileSizeChanged(old, _zoomLevel);
        }
        fixVisibility();
    }

    "The point currently pointed to by the scroll-bars."
    shared actual Point cursor => cursorPoint;
    assign cursor {
        Point oldCursor = cursorPoint;
        cursorPoint = cursor;
        scs.fireCursorChanges(oldCursor, cursor);
    }

   "The currently selected point in the map."
    shared actual Point selection => selPoint;
    assign selection {
        Point oldSel = selPoint;
        selPoint = selection;
        if (selection.valid) {
            cursor = selection;
        } else {
            cursor = Point(largest(selection.row, 0), largest(selection.column, 0));
        }
        scs.fireChanges(oldSel, selPoint);
        fixVisibility();
    }

    "The point in the map the user has just right-clicked on, if any."
    shared actual Point? interaction => interactionPoint;
    assign interaction {
        interactionPoint = interaction;
        scs.fireInteraction();
    }

    "Clear the selection."
    shared void clearSelection() {
        selection = Point.invalidPoint;
        interaction = null;
    }

    shared actual void addSelectionChangeListener(SelectionChangeListener listener) =>
            scs.addSelectionChangeListener(listener);
    shared actual void removeSelectionChangeListener(SelectionChangeListener listener) =>
            scs.removeSelectionChangeListener(listener);
    shared actual void addGraphicalParamsListener(GraphicalParamsListener listener) =>
            gpListeners.add(listener);
    shared actual void removeGraphicalParamsListener(GraphicalParamsListener listener) =>
            gpListeners.remove(listener);

    shared actual String string {
        if (exists path = map.filename) {
            return "ViewerModel for ``path``";
        } else {
            return "ViewerModel for an unsaved map";
        }
    }

    "Set the map and its filename, and also clear the selection and reset the visible
     dimensions and the zoom level."
    shared actual void setMap(IMutableMapNG newMap) {
        super.setMap(newMap);
        clearSelection();
        visDimensions = VisibleDimensions(0, newMap.dimensions.rows - 1, 0,
            newMap.dimensions.columns - 1);
        resetZoom();
    }

    "Set whether a tile is mountainous."
    shared actual void setMountainous(Point location, Boolean mountainous) {
        restrictedMap.mountainous[location] = mountainous;
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Add a fixture to the map at a point."
    shared actual void addFixture(Point location, TileFixture fixture) {
        restrictedMap.addFixture(location, fixture);
        mapModified = true; // TODO: If addFixture() returns Boolean, only set this flag if this was a change?
    }

    "Remove a fixture from the map at a point."
    shared actual void removeMatchingFixtures(Point location, Boolean(TileFixture) condition) {
        for (fixture in map.fixtures.get(location).filter(condition)) {
            restrictedMap.removeFixture(location, fixture);
        }
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Add a bookmark at the given location."
    shared actual void addBookmark(Point location) {
        restrictedMap.addBookmark(location);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Remove a bookmark at the current location."
    shared actual void removeBookmark(Point location) {
        restrictedMap.removeBookmark(location);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Add a river at a location."
    shared actual void addRiver(Point location, River river) {
        restrictedMap.addRivers(location, river);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Remove a river at a location."
    shared actual void removeRiver(Point location, River river) {
        restrictedMap.removeRivers(location, river);
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Set the map's terrain type at the given point."
    shared actual void setBaseTerrain(Point location, TileType? terrain) {
        restrictedMap.baseTerrain[location] = terrain;
        mapModified = true; // TODO: Only set the flag if this was a change?
    }

    "Move a unit-member from one unit to another."
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) {
        if (exists matchingOld = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>()
                    .filter(matchingValue(old.owner, HasOwner.owner))
                    .filter(matchingValue(old.kind, IUnit.kind))
                    .filter(matchingValue(old.name, IUnit.name))
                    .find(matchingValue(old.id, IUnit.id)),
                exists matchingMember = matchingOld.find(member.equals), // TODO: equals() isn't ideal for finding a matching member ...
                exists matchingNew = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>()
                    .filter(matchingValue(newOwner.owner, HasOwner.owner))
                    .filter(matchingValue(newOwner.kind, IUnit.kind))
                    .filter(matchingValue(newOwner.name, IUnit.name))
                    .find(matchingValue(newOwner.id, IUnit.id))) {
            matchingOld.removeMember(matchingMember);
            matchingNew.addMember(matchingMember);
            restrictedMap.modified = true;
        }
    }

    Boolean unitMatching(IUnit unit)(Point->IFixture entry) {
        value location->fixture = entry;
        if (is IUnit fixture, fixture.id == unit.id, fixture.owner == unit.owner) {
            return true;
        } else {
            return false;
        }
    }

    """Remove the given unit from the map. It must be empty, and may be
       required to be owned by the current player. The operation will also fail
       if "matching" units differ in name or kind from the provided unit.
       Returns [[true]] if the preconditions were met and the unit was removed,
       and [[false]] otherwise. To make an edge case explicit, if there are no
       matching units in any map the method returns [[false]]."""
    shared actual Boolean removeUnit(IUnit unit) {
        log.trace("In ViewerModel.removeUnit()");
        if (exists location->fixture = map.fixtures.flatMap(flattenEntries)
                .find(unitMatching(unit))) {
            log.trace("Map has matching unit");
            assert (is IUnit fixture);
            if (fixture.kind == unit.kind, fixture.name == unit.name, fixture.empty) {
                log.trace("Matching unit meets preconditions");
                if (fixture in map.fixtures.get(location)) { // TODO: syntax sugar
                    restrictedMap.removeFixture(location, fixture);
                    log.trace("Finished removing matching unit from map");
                    return true;
                } else {
                    for (fort in map.fixtures.get(location).narrow<IMutableFortress>()) { // TODO: syntax sugar
                        if (fixture in fort) {
                            fort.removeMember(fixture);
                            restrictedMap.modified = true;
                            log.trace("Finished removing matching unit from map");
                            return true;
                        }
                    } else {
                        log.warn("Failed to find unit to remove that we thought might be in a fortress");
                        return false;
                    }
                }
            } else {
                log.warn("Matching unit in ``map.filename else "an unsaved map"`` fails preconditions for removal");
                return false;
            }
        } else {
            log.trace("No matching units");
            return false;
        }
    }

    shared actual void addUnitMember(IUnit unit, UnitMember member) {
        if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                .narrow<IMutableUnit>()
                .filter(matchingValue(unit.owner, HasOwner.owner))
                .filter(matchingValue(unit.name, IUnit.name))
                .filter(matchingValue(unit.kind, IUnit.kind))
                .find(matchingValue(unit.id, IUnit.id))) {
            matching.addMember(member.copy(false));
            restrictedMap.modified = true;
        }
    }

    shared actual Boolean renameItem(HasMutableName item, String newName) {
        if (is IUnit item) {
            if (is HasMutableName matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IUnit>()
                    .filter(matchingValue(item.owner, HasOwner.owner))
                    .filter(matchingValue(item.name, IUnit.name))
                    .filter(matchingValue(item.kind, IUnit.kind))
                    .find(matchingValue(item.id, IUnit.id))) {
                matching.name = newName;
                restrictedMap.modified = true;
                return true;
            } else {
                log.warn("Unable to find unit to rename");
                return false;
            }
        } else if (is UnitMember item) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IUnit>()
                    .filter(matchingValue(map.players.currentPlayer, HasOwner.owner))
                    .flatMap(identity).narrow<HasMutableName>()
                    .filter(matchingValue(item.name, HasMutableName.name))
                    .find(matchingValue(item.id, UnitMember.id))) { // FIXME: We should have a firmer identification than just name and ID
                matching.name = newName;
                restrictedMap.modified = true;
                return true;
            } else {
                log.warn("Unable to find unit member to rename");
                return false;
            }
        } else {
            log.warn("Unable to find item to rename");
            return false;
        }
    }

    shared actual Boolean changeKind(HasKind item, String newKind) {
        if (is IUnit item) {
            if (is HasMutableKind matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IUnit>()
                    .filter(matchingValue(item.owner, HasOwner.owner))
                    .filter(matchingValue(item.name, IUnit.name))
                    .filter(matchingValue(item.kind, IUnit.kind))
                    .find(matchingValue(item.id, IUnit.id))) {
                matching.kind = newKind;
                restrictedMap.modified = true;
                return true;
            } else {
                log.warn("Unable to find unit to change kind");
                return false;
            }
        } else if (is UnitMember item) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IUnit>()
                    .filter(matchingValue(map.players.currentPlayer, HasOwner.owner))
                    .flatMap(identity).narrow<HasMutableKind>()
                    .filter(matchingValue(item.kind, HasMutableKind.kind))
                    .find(matchingValue(item.id, UnitMember.id))) { // FIXME: We should have a firmer identification than just kind and ID
                matching.kind = newKind;
                restrictedMap.modified = true;
                return true;
            } else {
                log.warn("Unable to find unit member to change kind");
                return false;
            }
        } else {
            log.warn("Unable to find item to change kind");
            return false;
        }
    }

    shared actual void dismissUnitMember(UnitMember member) {
        for (unit in map.fixtures.items.flatMap(unflattenNonFortresses)
                .narrow<IMutableUnit>()
                .filter(matchingValue(map.players.currentPlayer, HasOwner.owner))) {
            if (exists matching = unit.find(member.equals)) { // FIXME: equals() will really not do here ...
                unit.removeMember(matching);
                dismissedMembers.add(member);
                restrictedMap.modified = true;
                break;
            }
        }
    }

    shared actual {UnitMember*} dismissed => dismissedMembers;

    shared actual Boolean addSibling(UnitMember existing, UnitMember sibling) {
        for (unit in map.fixtures.items.flatMap(unflattenNonFortresses)
                .narrow<IMutableUnit>()
                .filter(matchingValue(map.players.currentPlayer, HasOwner.owner))) {
            if (existing in unit) { // TODO: look beyond equals() for matching-in-existing?
                unit.addMember(sibling.copy(false));
                restrictedMap.modified = true;
                return true;
            }
        }
        return false;
    }

    "Change the owner of the given item in all maps. Returns [[true]] if this
     succeeded in any map, [[false]] otherwise."
    shared actual Boolean changeOwner(HasMutableOwner item, Player newOwner) {
        if (exists matching = map.fixtures.items.flatMap(flattenIncluding)
                .flatMap(flattenIncluding).narrow<HasMutableOwner>()
                .find(item.equals)) { // TODO: equals() is not the best way to find it ...
            if (!newOwner in map.players) {
                restrictedMap.addPlayer(newOwner);
            }
            matching.owner = map.players.getPlayer(newOwner.playerId);
            restrictedMap.modified = true;
            return true;
        }
        return false;
    }

    shared actual Boolean sortFixtureContents(IUnit fixture) {
        if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                .narrow<IMutableUnit>()
                .filter(matchingValue(map.players.currentPlayer, HasOwner.owner))
                .filter(matchingValue(fixture.name, IUnit.name))
                .filter(matchingValue(fixture.kind, IUnit.kind))
                .find(matchingValue(fixture.id, IUnit.id))) {
            matching.sortMembers();
            restrictedMap.modified = true;
            return true;
        }
        return false;
    }

    shared actual void addUnit(IUnit unit) {
        variable Point hqLoc = Point.invalidPoint;
        for (location in map.locations) {
            if (exists fortress = map.fixtures.get(location).narrow<IFortress>()
                    .find(matchingValue(unit.owner, HasOwner.owner))) {
                if (fortress.name == "HQ") {
                    hqLoc = location;
                    break;
                } else if (!hqLoc.valid) {
                    hqLoc = location;
                }
            }
        }
        restrictedMap.addFixture(hqLoc, unit);
    }
}
