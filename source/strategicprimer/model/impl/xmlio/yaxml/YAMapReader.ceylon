import ceylon.collection {
    Stack,
    LinkedList,
    MutableMap,
    HashMap
}
import ceylon.language.meta {
    classDeclaration,
    type
}

import javax.xml.namespace {
    QName
}
import javax.xml.stream.events {
    StartElement,
    XMLEvent,
    EndElement,
    Characters
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.model.common.map {
    Player,
    MapDimensions,
    MapDimensionsImpl,
    Point,
    River,
    TileType,
    IMutablePlayerCollection,
    TileFixture,
    IMutableMapNG,
    IMapNG,
    SPMapNG,
    Direction
}
import strategicprimer.model.common.map.fixtures {
    TextFixture,
    Ground
}
import strategicprimer.model.common.xmlio {
    Warning,
    futureTags
}
import strategicprimer.model.impl.xmlio.exceptions {
    MissingChildException,
    MissingPropertyException,
    UnwantedChildException,
    UnsupportedTagException,
    MapVersionException
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.common.map.fixtures.mobile {
    maturityModel,
    immortalAnimals
}
import strategicprimer.model.common.map.fixtures.towns {
    IFortress
}
import ceylon.language.meta.model {
    ClassOrInterface
}
import lovelace.util.common {
    matchingValue,
    comparingOn
}

// TODO: Use the one in the Animal maturityModel instead of here?
variable Integer currentTurn = -1;

"A reader for Strategic Primer maps."
class YAMapReader("The Warning instance to use" Warning warner,
        "The factory for ID numbers" IDRegistrar idRegistrar,
        "The map's collection of players" IMutablePlayerCollection players)
        extends YAAbstractReader<IMapNG>(warner, idRegistrar) {
    "The reader for players"
    YAReader<Player> playerReader = YAPlayerReader(warner, idRegistrar);

    "The readers we'll try sub-tags on"
    value readers = [YAMobileReader(warner, idRegistrar),
        YAResourceReader(warner, idRegistrar), YATerrainReader(warner, idRegistrar),
        YATextReader(warner, idRegistrar), YATownReader(warner, idRegistrar, players),
        YAGroundReader(warner, idRegistrar),
        YAAdventureReader(warner, idRegistrar, players),
        YAPortalReader(warner, idRegistrar), YAExplorableReader(warner, idRegistrar),
        YAUnitReader(warner, idRegistrar, players) ];

    MutableMap<String, YAAbstractReader<out TileFixture>> readerCache =
            HashMap<String, YAAbstractReader<out TileFixture>>();

    MutableMap<ClassOrInterface<TileFixture>, YAAbstractReader<out TileFixture>>
        writerCache =
            HashMap<ClassOrInterface<TileFixture>, YAAbstractReader<out TileFixture>>();

    "Get the first open-tag event in our namespace in the stream."
    StartElement getFirstStartElement({XMLEvent*} stream, StartElement parent) {
        if (exists element = stream.narrow<StartElement>().find(
                compose(isSupportedNamespace, StartElement.name))) {
            return element;
        } else {
            throw MissingChildException(parent);
        }
    }

    "Write a newline if needed."
    void eolIfNeeded(Boolean needEol, Anything(String) writer) {
        if (needEol) {
            writer(operatingSystem.newline);
        }
    }

    "Parse a river from XML. The caller is now responsible for advancing the stream past
     the closing tag."
    shared River parseRiver(StartElement element, QName parent) {
        requireTag(element, parent, "river", "lake");
        if ("lake" == element.name.localPart.lowercased) {
            expectAttributes(element);
            return River.lake;
        } else {
            expectAttributes(element, "direction");
            value river = River.parse(getParameter(element, "direction"));
            if (is River river) {
                return river;
            } else {
                throw MissingPropertyException(element, "direction", river);
            }
        }
    }

    "Write a river."
    shared void writeRiver(Anything(String) ostream, River obj, Integer indent) {
        if (River.lake == obj) {
            writeTag(ostream, "lake", indent);
        } else {
            writeTag(ostream, "river", indent);
            writeProperty(ostream, "direction", obj.description);
        }
        closeLeafTag(ostream);
    }

    "Parse what should be a [[TileFixture]] from the XML."
    TileFixture parseFixture(StartElement element, QName parent,
            {XMLEvent*} stream) {
        String name = element.name.localPart;
        if (exists reader = readerCache[name.lowercased]) {
            return reader.read(element, parent, stream);
        }
        for (reader in readers) {
            if (reader.isSupportedTag(name)) {
                readerCache[name.lowercased] = reader;
                return reader.read(element, parent, stream);
            }
        } else {
            if (immortalAnimals.contains(name.lowercased)) {
                if (exists reader = readerCache["animal"]) {
                    return reader.read(element, parent, stream);
                } else {
                    for (reader in readers) {
                        if (reader.isSupportedTag("animal")) {
                            return reader.read(element, parent, stream);
                        }
                    }
                }
            }
            throw UnwantedChildException(QName(element.name.namespaceURI, "tile"),
                element);
        }
    }

    "Read a map from XML."
    shared actual IMutableMapNG read(StartElement element, QName parent,
            {XMLEvent*} stream) {
        requireTag(element, parent, "map", "view");
        Integer currentTurn;
        StartElement mapTag;
        switch(element.name.localPart.lowercased)
        case ("view") {
            expectAttributes(element, "current_turn", "current_player");
            currentTurn = getIntegerParameter(element, "current_turn");
            maturityModel.currentTurn = currentTurn;
            mapTag = getFirstStartElement(stream, element);
            requireTag(mapTag, element.name, "map");
            expectAttributes(mapTag, "version", "rows", "columns");
        }
        case ("map") {
            currentTurn = 0;
            mapTag = element;
            expectAttributes(mapTag, "version", "rows", "columns", "current_player");
        }
        else {
            throw UnwantedChildException.listingExpectedTags(QName("xml"), element,
                ["map", "view"]);
        }
        MapDimensions dimensions;
        MapDimensions readDimensions = MapDimensionsImpl(getIntegerParameter(mapTag, "rows"),
            getIntegerParameter(mapTag, "columns"),
            getIntegerParameter(mapTag, "version"));
        if (readDimensions.version == 2) {
            dimensions = readDimensions;
        } else {
            warner.handle(MapVersionException(mapTag, readDimensions.version, 2, 2));
            dimensions =
                MapDimensionsImpl(readDimensions.rows, readDimensions.columns, 2);
        }
        Stack<QName> tagStack = LinkedList<QName>();
        tagStack.push(element.name);
        tagStack.push(mapTag.name);
        IMutableMapNG retval = SPMapNG(dimensions, players, currentTurn);
        variable Point? point = null;
        for (event in stream) {
            if (is StartElement event, isSupportedNamespace(event.name)) {
                String type = event.name.localPart.lowercased;
                if ("player" == type) {
                    assert (exists top = tagStack.top);
                    retval.addPlayer(playerReader.read(event, top, stream));
                } else if ("row" == type) {
                    expectAttributes(event, "index");
                    tagStack.push(event.name);
                    // Deliberately ignore "row"
                    continue;
                } else if ("tile" == type) {
                    if (point exists) {
                        assert (exists top = tagStack.top);
                        throw UnwantedChildException(top, event);
                    }
                    expectAttributes(event, "row", "column", "kind", "type");
                    tagStack.push(event.name);
                    Point localPoint = parsePoint(event);
                    point = localPoint;
                    // Since tiles have sometimes been *written* without "kind", then
                    // failed to load, be liberal in waht we accept here.
                    if ((hasParameter(event, "kind") ||hasParameter(event, "type"))) {
                        value kind = TileType.parse(getParamWithDeprecatedForm(event,
                            "kind", "type"));
                        if (is TileType kind) {
                            retval.baseTerrain[localPoint] = kind;
                        } else {
                            warner.handle(MissingPropertyException(event, "kind", kind));
                        }
                    } else {
                        warner.handle(MissingPropertyException(event, "kind"));
                    }
                } else if ("elsewhere" == type) {
                    if (point exists) {
                        assert (exists top = tagStack.top);
                        throw UnwantedChildException(top, event);
                    }
                    expectAttributes(event);
                    tagStack.push(event.name);
                    point = Point.invalidPoint;
                } else if (futureTags.contains(type)) {
                    tagStack.push(event.name);
                    warner.handle(UnsupportedTagException.future(event));
                } else if ("sandbar" == type) {
                    tagStack.push(event.name);
                    warner.handle(UnsupportedTagException.obsolete(event));
                } else if (exists localPoint = point) {
                    if ("lake" == type || "river" == type) {
                        assert (exists top = tagStack.top);
                        retval.addRivers(localPoint, parseRiver(event, top));
                        spinUntilEnd(event.name, stream);
                    } else if ("mountain" == type) {
                        tagStack.push(event.name);
                        retval.mountainous[localPoint] = true;
                    } else if ("bookmark" == type) {
                        tagStack.push(event.name);
                        expectAttributes(event, "player");
                        retval.addBookmark(localPoint, players.getPlayer(getIntegerParameter(event, "player")));
                    } else if ("road" == type) {
                        tagStack.push(event.name);
                        expectAttributes(event, "direction", "quality");
                        if (exists direction = Direction.parse(getParameter(event, "direction"))) {
                            retval.setRoadLevel(localPoint, direction, getIntegerParameter(event, "quality"));
                        } else {
                            throw MissingPropertyException(event, "direction");
                        }
                    } else {
                        assert (exists top = tagStack.top);
                        value child = parseFixture(event, top, stream);
                        if (is IFortress child, retval.fixtures.get(localPoint) // TODO: syntax sugar
                                .narrow<IFortress>()
                                .any(matchingValue(child.owner, IFortress.owner))) {
                            warner.handle(UnwantedChildException.withMessage(top, event,
                                    "Multiple fortresses owned by one player on a tile"));
                        }
                        retval.addFixture(localPoint, child);
                    }
                } else {
                    // fixture outside tile
                    assert (exists top = tagStack.top);
                    throw UnwantedChildException.listingExpectedTags(top, event,
                        ["tile", "elsewhere"]);
                }
            } else if (is EndElement event) {
                if (exists top = tagStack.top, top == event.name) {
                    tagStack.pop();
                }
                if (element.name == event.name) {
                    break;
                } else if ("tile" == event.name.localPart.lowercased ||
                        "elsewhere" == event.name.localPart.lowercased) {
                    point = null;
                }
            } else if (is Characters event) {
                String data = event.data.trimmed;
                if (!data.empty) {
                    retval.addFixture(point else Point.invalidPoint,
                        TextFixture(data, -1));
                }
            }
        }
        if (hasParameter(mapTag, "current_player")) {
            retval.currentPlayer = players.getPlayer(getIntegerParameter(mapTag,
                "current_player"));
        } else if (hasParameter(element, "current_player")) {
            retval.currentPlayer = players.getPlayer(getIntegerParameter(element,
                "current_player"));
        } else {
            warner.handle(MissingPropertyException(mapTag, "current_player"));
        }
        retval.modified = false;
        return retval;
    }

    "Write a child object"
    void writeChild(Anything(String) ostream, TileFixture child, Integer tabs) {
        value cls = type(child);
        if (exists writer = writerCache[cls]) {
            writer.writeRaw(ostream, child, tabs);
            return;
        }
        for (reader in readers) {
            if (reader.canWrite(child)) {
                writerCache[cls] = reader;
                reader.writeRaw(ostream, child, tabs);
                return;
            }
        } else {
            throw AssertionError("After checking ``readers
                .size`` readers, don't know how to write a ``classDeclaration(child)
                .name``");
        }
    }

    Boolean validPoint(Point->TileFixture entry) => entry.key.valid;

    "Write a map."
    shared actual void write(Anything(String) ostream, IMapNG obj, Integer tabs) {
        writeTag(ostream, "view", tabs);
        writeProperty(ostream, "current_player", obj.currentPlayer.playerId);
        writeProperty(ostream, "current_turn", obj.currentTurn);
        currentTurn = obj.currentTurn;
        finishParentTag(ostream);
        writeTag(ostream, "map", tabs + 1);
        MapDimensions dimensions = obj.dimensions;
        writeProperty(ostream, "version", dimensions.version);
        writeProperty(ostream, "rows", dimensions.rows);
        writeProperty(ostream, "columns", dimensions.columns);
        finishParentTag(ostream);
        for (player in obj.players) {
            playerReader.write(ostream, player, tabs + 2);
        }
        for (i in 0:(dimensions.rows)) {
            variable Boolean rowEmpty = true;
            for (j in 0:(dimensions.columns)) {
                Point loc = Point(i, j);
                TileType? terrain = obj.baseTerrain[loc];
                if (!obj.locationEmpty(loc)) {
                    if (rowEmpty) {
                        rowEmpty = false;
                        writeTag(ostream, "row", tabs + 2);
                        writeProperty(ostream, "index", i);
                        finishParentTag(ostream);
                    }
                    writeTag(ostream, "tile", tabs + 3);
                    writeProperty(ostream, "row", i);
                    writeProperty(ostream, "column", j);
                    if (exists terrain) {
                        writeProperty(ostream, "kind", terrain.xml);
                    }
                    ostream(">");
                    variable Boolean needEol = true;
                    for (player in obj.allBookmarks.get(loc)) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeTag(ostream, "bookmark", tabs + 4);
                        writeProperty(ostream, "player", player.playerId);
                        closeLeafTag(ostream);
                    }
//                    if (obj.mountainous[loc]) { // TODO: syntax sugar once compiler bug fixed
                    if (obj.mountainous.get(loc)) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeTag(ostream, "mountain", tabs + 4);
                        closeLeafTag(ostream);
                    }
//                    for (river in sort(obj.rivers[loc])) {
                    for (river in sort(obj.rivers.get(loc))) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeRiver(ostream, river, tabs + 4);
                    }
                    for (direction->quality in obj.roads.getOrDefault(loc, [])
                            .sort(comparingOn(Entry<Direction,Integer>.key, increasing<Direction>))) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeTag(ostream, "road", tabs + 4);
                        writeProperty(ostream, "direction", direction.string);
                        writeProperty(ostream, "quality", quality);
                        closeLeafTag(ostream);
                    }
                    // To avoid breaking map-format-conversion tests, and to
                    // avoid churn in existing maps, put the first Ground and Forest
                    // before other fixtures.
//                    Ground? ground = obj.fixtures[loc].narrow<Ground>().first;
                    Ground? ground = obj.fixtures.get(loc).narrow<Ground>().first;
                    if (exists ground) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeChild(ostream, ground, tabs + 4);
                    }
//                    Forest? forest = obj.fixtures[loc].narrow<Forest>().first;
                    Forest? forest = obj.fixtures.get(loc).narrow<Forest>().first;
                    if (exists forest) {
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeChild(ostream, forest, tabs + 4);
                    }
//                    for (fixture in obj.fixtures[loc]) {
                    for (fixture in obj.fixtures.get(loc)) {
                        if ([ground, forest].coalesced.contains(fixture)) {
                            continue;
                        }
                        eolIfNeeded(needEol, ostream);
                        needEol = false;
                        writeChild(ostream, fixture, tabs + 4);
                    }
                    if (!needEol) {
                        indent(ostream, tabs + 3);
                    }
                    closeTag(ostream, 0, "tile");
                }
            }
            if (!rowEmpty) {
                closeTag(ostream, tabs + 2, "row");
            }
        }
        if (obj.fixtures.any(not(validPoint))) {
            writeTag(ostream, "elsewhere", tabs + 2);
            finishParentTag(ostream);
            for (fixture in obj.fixtures.filter(not(validPoint)).map(Entry.item)
                    .coalesced) {
                writeChild(ostream, fixture, tabs + 3);
            }
            closeTag(ostream, tabs + 2, "elsewhere");
        }
        closeTag(ostream, tabs + 1, "map");
        closeTag(ostream, tabs, "view");
    }

    shared actual Boolean isSupportedTag(String tag) =>
            ["map", "view"].contains(tag.lowercased);

    shared actual Boolean canWrite(Object obj) => obj is IMapNG;
}
