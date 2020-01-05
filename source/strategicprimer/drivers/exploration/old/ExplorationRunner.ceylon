import ceylon.collection {
    LinkedList,
    MutableMap,
    HashMap,
    MutableSet,
    HashSet,
    ArrayList,
    MutableList
}

import lovelace.util.common {
    todo,
    readFileContents
}

import strategicprimer.model.common.map {
    Point,
    TileType,
    TileFixture,
    MapDimensions
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

import ceylon.file {
    File,
    Directory
}

import ceylon.language.meta.declaration {
    Module
}

import ceylon.logging {
    logger,
    Logger
}

Logger log = logger(`module strategicprimer.drivers.exploration.old`);

"""A class to create exploration results. The initial implementation is a bit hackish, and
   should be generalized and improved---except that the entire idea of generating results
   from "encounter tables" instead of selecting from "fixtures" stored in the map has been
   abandoned, after using it to generate the tile-fixtures in each tile in the main map.
   This old method is now only used in the town-contents generator and in the TODO-fixing
   driver's handling of broken town production, consumption, and skill levels."""
shared class ExplorationRunner() {
    MutableMap<String, EncounterTable> tables = HashMap<String, EncounterTable>();

    "Get a table by name." // Used by the table debugger.
    throws(`class MissingTableException`, "if there is no table by that name")
    restricted shared EncounterTable getTable(String name) {
        if (exists retval = tables[name]) {
            return retval;
        } else {
            throw MissingTableException(name);
        }
    }

    "Whether we have a table of the given name."
    shared Boolean hasTable(String name) => name in tables.keys;

    "Split a string on hash-marks."
    {String+} splitOnHash(String string) => string.split('#'.equals, true, false, 3);

    "Consult a table, and if a result indicates recursion, perform it. Recursion is
     indicated by hash-marks around the name of the table to call; results are undefined
     if there are more than two hash marks in any given String, or if either is at the
     beginning or end of the string, since we use [[String.split]]"
    shared String recursiveConsultTable(String table, Point location, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        String result = consultTable(table, location, terrain, mountainous, fixtures,
            mapDimensions);
        if (result.contains('#')) {
            {String+} broken = splitOnHash(result);
            String before = broken.first;
            assert (exists middle = broken.rest.first);
            StringBuilder builder = StringBuilder();
            builder.append(before);
            builder.append(recursiveConsultTable(middle, location, terrain, mountainous,
                fixtures, mapDimensions));
            broken.skip(2).each(builder.append);
            return builder.string;
        } else {
            return result;
        }
    }

    "Check whether a table contains recursive calls to a table that doesn't exist. The
     outermost call to this method should not provide a second parameter (i.e. should
     use the default)"
    shared Boolean recursiveCheck(String table,
            MutableSet<String> state = HashSet<String>()) {
        if (state.contains(table)) {
            return false;
        }
        state.add(table);
        if (tables.defines(table)) {
            try {
                for (string in getTable(table).allEvents) {
                    if (string.contains('#'), recursiveCheck(
                            splitOnHash(string).rest.first else "",
                            state)) {
                        return true;
                    }
                }
            } catch (MissingTableException except) {
                log.info("Missing table ``table``", except);
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    "Check whether any table contains recursive calls to a table that doesn't exist."
    shared Boolean globalRecursiveCheck() {
        MutableSet<String> state = HashSet<String>();
        for (table in tables.keys) {
            if (recursiveCheck(table, state)) {
                return true;
            }
        }
        return false;
    }

    "Print the names of any tables that are called but don't exist yet."
    shared void verboseRecursiveCheck(String table, Anything(String) ostream,
            MutableSet<String> state = HashSet<String>()) {
        if (!state.contains(table)) {
            state.add(table);
            if (tables.defines(table)) {
                try {
                    for (string in getTable(table).allEvents) {
                        if (string.contains('#')) {
                            verboseRecursiveCheck(
                                splitOnHash(string).rest.first else "",
                                    ostream, state);
                        }
                    }
                } catch (MissingTableException except) {
                    ostream(except.table);
                }
            } else {
                ostream(table);
            }
        }
    }

    "Print the names of any tables that are called but don't exist yet."
    shared void verboseGlobalRecursiveCheck(Anything(String) ostream) {
        MutableSet<String> state = HashSet<String>();
        for (table in tables.keys) {
            verboseRecursiveCheck(table, ostream, state);
        }
    }

    "Consult a table. (Look up the given tile if it's a quadrant table, roll on it if
     it's a random-encounter table.) Note that the result may be or include the name of
     another table, which should then be consultd."
    shared String consultTable(
            "The name of the table to consult"
            String table,
            "The location of the tile"
            Point location,
            "The terrain there. Null if unknown."
            TileType? terrain,
            "Whether the tile is mountainous."
            Boolean mountainous,
            "Any fixtures there"
            {TileFixture*} fixtures,
            "The dimensions of the map"
            MapDimensions mapDimensions) => getTable(table).generateEvent(location,
                terrain, mountainous, fixtures, mapDimensions);

    "Get the primary rock at the given location."
    shared String getPrimaryRock(
            "The tile's location."
            Point location,
            "The terrain there."
            TileType terrain,
            "Whether the tile is mountainous."
            Boolean mountainous,
            "Any fixtures there."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions) => consultTable("major_rock", location, terrain,
                mountainous, fixtures, mapDimensions);

    "Get the primary forest at the given location."
    shared String getPrimaryTree(
            "The tile's location."
            Point location,
            "The terrain there."
            TileType terrain,
            "Whether the tile is mountainous."
            Boolean mountainous,
            "Any fixtures there."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions) {
        switch (terrain)
        case (TileType.steppe) {
            if (!fixtures.narrow<Forest>().empty) {
                return consultTable("boreal_major_tree", location, terrain,
                    mountainous, fixtures, mapDimensions);
            }
        } case (TileType.plains) {
            if (!fixtures.narrow<Forest>().empty) {
                return consultTable("temperate_major_tree", location, terrain,
                    mountainous, fixtures, mapDimensions);
            }
        } else { }
        throw AssertionError("Only forests have primary trees");
    }

    """Get the "default results" (primary rock and primary forest) for the given
       location."""
    shared String defaultResults(
            "The tile's location."
            Point location,
            "The terrain there."
            TileType terrain,
            "Whether the tile is mountainous"
            Boolean mountainous,
            "Any fixtures there."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions) {
        if (!fixtures.narrow<Forest>().empty && (terrain == TileType.steppe ||
                    terrain == TileType.plains)) {
            return "The primary rock type here is ``getPrimaryRock(location, terrain,
                        mountainous, fixtures, mapDimensions)``.
                    The main kind of tree is ``getPrimaryTree(location, terrain,
                        mountainous, fixtures, mapDimensions)``.
                    ";
        } else {
            return "The primary rock type here is ``getPrimaryRock(location, terrain,
                        mountainous, fixtures, mapDimensions)``.
                    ";
        }
    }

    "Add a table. This is shared so that tests can use it, but shouldn't be used beyond
     that."
    todo("Move tests *into* this class instead")
    shared restricted(`module strategicprimer.drivers.exploration.old`)
        void loadTable(String name, EncounterTable table) => tables[name] = table;

    "Load a table from a data stream into the runner. This is shared so tests can use it,
     but shouldn't be used beyond that."
    shared restricted(`module strategicprimer.drivers.exploration.old`)
    void loadTableFromDataStream(<String|Finished>?() source, String name) {
        if (is String line = source()) {
            switch (line[0])
            case (null) {
                throw ParseException(
                    "File doesn't start by specifying which kind of table");
            }
            case ('q'|'Q') {
                if (is String firstLine = source()) {
                    value rows = Integer.parse(firstLine);
                    if (is Integer rows) {
                        MutableList<String> items = LinkedList<String>();
                        while (is String tableLine = source()) {
                            items.add(tableLine);
                        }
                        loadTable(name, QuadrantTable(rows, *items));
                    } else {
                        throw Exception(
                            "File doesn't start with number of rows of quadrants", rows);
                    }
                } else {
                    throw Exception(
                        "File doesn't start with number of rows of quadrants");
                }
            }
            case ('r'|'R') {
                MutableList<[Integer, String]> list = ArrayList<[Integer, String]>();
                variable Boolean first = true;
                while (is String tableLine = source()) {
                    value splitted = tableLine.split(' '.equals, true, false);
                    if (splitted.size < 2) {
                        if (first, tableLine == line) {
                            log.debug("Ceylon tried to read the same line twice again");
                        } else if (tableLine.empty) {
                            log.debug("Unexpected blank line");
                        } else {
                            log.error("Line with no blanks, coninuing ...");
                            log.info("It was '``tableLine``'");
                        }
                    } else {
                        String left = splitted.first;
                        value leftNum = Integer.parse(left);
                        if (is Integer leftNum) {
                            list.add([leftNum, " ".join(splitted.rest)]);
                        } else {
                            throw Exception("Non-numeric data", leftNum);
                        }
                    }
                    first = false;
                }
                loadTable(name, RandomTable(*list));
            }
            case ('c'|'C') {
                if (is String tableLine = source()) {
                    loadTable(name, ConstantTable(tableLine));
                } else {
                    throw ParseException("constant value not present");
                }
            }
            case ('t'|'T') {
                MutableList<String->String> list = ArrayList<String->String>();
                variable Boolean first = true;
                while (is String tableLine = source()) {
                    value splitted = tableLine.split(' '.equals, true, false);
                    if (splitted.size < 2) {
                        if (first, tableLine == line) {
                            log.debug("Ceylon tried to read the same line twice again");
                        } else if (tableLine.empty) {
                            log.debug("Unexpected blank line");
                        } else {
                            log.error("Line with no blanks, coninuing ...");
                            log.info("It was '``tableLine``'");
                        }
                    } else {
                        // N.B. first must be a recognized tile type (including ver-1
                        // types), but that's checked by TerrainTable.
                        list.add(splitted.first->(" ".join(splitted.rest)));
                    }
                    first = false;
                }
                loadTable(name, TerrainTable(*list));
            } else {
                throw ParseException("unknown table type '``line`` in file ``name``");
            }
        }
    }
    "Load a table from file into the runner. If the file*name* is provided, as a Tuple,
     the name is relative to a `tables/` directory."
    shared void loadTableFromFile(File|Resource|[Module, String] file) {
        if (is File file) {
            try (reader = file.Reader()) {
                loadTableFromDataStream(reader.readLine, file.name);
            }
        } else if (is Resource file) {
            loadTableFromDataStream(
                LinkedList(file.textContent().lines).accept, file.name);
        } else {
            assert (exists tableContents = readFileContents(file.first,
                "tables/" + file.rest.first));
            loadTableFromDataStream(LinkedList(tableContents.lines).accept,
                file.rest.first);
        }
    }
    "All possible results from the given table."
    throws(`class MissingTableException`, "if that table has not been loaded")
    shared {String*} getTableContents(String table) => getTable(table).allEvents;

    "Load all tables in the specified path into the runner."
    shared void loadAllTables(Directory path) {
        // While it would probably be possible to exclude dotfiles using the `filter`
        // parameter to `Directory.files()`, this would be inefficient.
        for (child in path.files()) {
            if (child.hidden || child.name.startsWith(".")) {
                log.info("``child.name`` looks like a hidden file, skipping ...");
            } else {
                try {
                    loadTableFromFile(child);
                } catch (Exception except) {
                    log.error("Error loading ``child.name``, continuing ...");
                    log.debug("Details of that error:", except);
                }
            }
        }
    }
}
