import ceylon.collection {
    Queue,
    LinkedList,
    MutableMap,
    HashMap,
    MutableSet,
    HashSet
}
import ceylon.test {
    assertEquals,
    assertThatException,
    assertTrue,
    assertFalse,
    test
}

import java.lang {
    UnsupportedOperationException,
    IllegalArgumentException
}

import lovelace.util.common {
    todo
}

import strategicprimer.model.map {
    Point,
    TileFixture,
    TileType,
    pointFactory,
    MapDimensionsImpl,
    MapDimensions
}

"""A class to create exploration results. The initial implementation is a bit hackish, and
   should be generalized and improved---except that the entire idea of generating results
   from "encounter tables" instead of selecting from "fixtures" stored in the map has been
   abandoned. This old method remains as a way of generating the map's contents *once* per
   campaign."""
shared class ExplorationRunner() {
    MutableMap<String, EncounterTable> tables = HashMap<String, EncounterTable>();
    "Get a table by name."
    throws(`class MissingTableException`, "if there is no table by that name")
    todo("Should this really be `shared`?")
    shared EncounterTable getTable(String name) {
        if (exists retval = tables.get(name)) {
            return retval;
        } else {
            throw MissingTableException(name);
        }
    }
    "Consult a table, and if a result indicates recursion, perform it. Recursion is
     indicated by hash-marks around the name of the table to call; results are undefined
     if there are more than two hash marks in any given String, or if either is at the
     beginning or end of the string, since we use String.split"
    shared String recursiveConsultTable(String table, Point location, TileType terrain,
            {TileFixture*} fixtures, MapDimensions mapDimensions) {
        String result = consultTable(table, location, terrain, fixtures, mapDimensions);
        if (result.contains("#")) {
            {String+} broken = result.split('#'.equals, true, false, 3);
            String before = broken.first;
            assert (exists middle = broken.rest.first);
            StringBuilder builder = StringBuilder();
            builder.append(before);
            builder.append(recursiveConsultTable(middle, location, terrain, fixtures,
                mapDimensions));
            for (part in broken.skip(2)) {
                builder.append(part);
            }
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
                    if (string.contains("#"), recursiveCheck(
                            string.split('#'.equals, true,
                                false, 3).rest.first else "",
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
                        if (string.contains("#")) {
                            verboseRecursiveCheck(
                                string.split('#'.equals, true,
                                    false, 3).rest.first else "",
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
            "The terrain there"
            TileType terrain,
            "Any fixtures there"
            {TileFixture*} fixtures,
            "The dimensions of the map"
            MapDimensions mapDimensions) {
        return getTable(table).generateEvent(location, terrain, fixtures,
            mapDimensions);
    }
    "Get the primary rock at the given location."
    shared String getPrimaryRock(
            "The tile's location."
            Point location,
            "The terrain there."
            TileType terrain,
            "Any fixtures there."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions) => consultTable("major_rock", location, terrain,
                fixtures, mapDimensions);
    "Get the primary forest at the given location."
    shared String getPrimaryTree(
            "The tile's location."
            Point location,
            "The terrain there."
            TileType terrain,
            "Any fixtures there."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions) {
        switch (terrain)
        case (TileType.borealForest) {
            return consultTable("boreal_major_tree", location, terrain,
                fixtures, mapDimensions);
        } case (TileType.temperateForest) {
            return consultTable("temperate_major_tree", location, terrain,
                fixtures, mapDimensions);
        } else {
            throw IllegalArgumentException("Only forests have primary trees");
        }
    }
    """Get the "default results" (primary rock and primary forest) for the given
       location."""
    shared String defaultResults(
            "The tile's location."
            Point location,
            "The terrain there."
            TileType terrain,
            "Any fixtures there."
            {TileFixture*} fixtures,
            "The dimensions of the map."
            MapDimensions mapDimensions) {
        if (terrain == TileType.borealForest || terrain == TileType.temperateForest) {
            return "The primary rock type here is ``getPrimaryRock(location, terrain, fixtures, mapDimensions)``.
                    The main kind of tree is ``getPrimaryTree(location, terrain, fixtures, mapDimensions)``.
                    ";
        } else {
            return "The primary rock type here is ``getPrimaryRock(location, terrain, fixtures, mapDimensions)``.
                    ";
        }
    }
    "Add a table. This is shared so that tests can use it, but shouldn't be used beyond that."
    todo("If Ceylon ever gets a more nuanced visibility model, revise this",
        "Move tests *into* this class instead")
    shared void loadTable(String name, EncounterTable table) => tables.put(name, table);
}

"A mock [[EncounterTable]] for the apparatus to test the ExplorationRunner."
class MockTable(String* values) satisfies EncounterTable {
    Queue<String> queue = LinkedList<String> { *values };
    shared actual String generateEvent(Point point, TileType terrain,
            {TileFixture*} fixtures, MapDimensions mapDimensions) {
        assert (exists retval = queue.accept());
        return retval;
    }
    shared actual Set<String> allEvents {
        throw UnsupportedOperationException("mock object");
    }
}
test
void testGetPrimaryRock() {
    ExplorationRunner runner = ExplorationRunner();
    runner.loadTable("major_rock", MockTable("primary_rock_test"));
    assertEquals(runner.getPrimaryRock(pointFactory(0, 0),
            TileType.tundra, {}, MapDimensionsImpl(69, 88, 2)),
        "primary_rock_test", "primary rock test");
}
test
void testGetPrimaryTree() {
    ExplorationRunner runner = ExplorationRunner();
    runner.loadTable("boreal_major_tree", MockTable("boreal_major_test"));
    runner.loadTable("temperate_major_tree", MockTable("temperate_major_test"));
    Point point = pointFactory(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
    assertEquals(runner.getPrimaryTree(point, TileType.borealForest, {},
        dimensions), "boreal_major_test", "primary tree test for boreal forest");
    assertEquals(runner.getPrimaryTree(point, TileType.temperateForest, {},
        dimensions), "temperate_major_test", "primary tree test for temperate forest");
}

test
void testIllegalGetPrimaryTree() {
    Point point = pointFactory(0, 0);
    assertThatException(
                () => ExplorationRunner().getPrimaryTree(point,
                    TileType.tundra, {}, MapDimensionsImpl(69, 88, 2)))
        .hasType(`IllegalArgumentException`);
}

test
void testConsultTable() {
    ExplorationRunner runner = ExplorationRunner();
    runner.loadTable("test_table_one", MockTable("test_one"));
    runner.loadTable("test_table_two", MockTable("test_two"));
    runner.loadTable("test_table_three", MockTable("test_three"));
    Point point = pointFactory(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
    assertEquals(runner.consultTable("test_table_one", point,
            TileType.tundra, {}, dimensions), "test_one", "first table");
    assertEquals(runner.consultTable("test_table_two", point,
            TileType.tundra, {}, dimensions), "test_two", "second table");
    assertEquals(runner.consultTable("test_table_three", point,
            TileType.tundra, {}, dimensions), "test_three", "third table");
}
"Test the recursiveConsultTable method: the one method under test whose correctness
 is non-obvious. We don't use mock tables here because setting them up would be
 more trouble than they're worth."
test
void testRecursiveConsultTable() {
    ExplorationRunner runner = ExplorationRunner();
    runner.loadTable("test_table_one", ConstantTable("( #test_table_two# )"));
    runner.loadTable("test_table_two", ConstantTable("( #test_table_three# )"));
    runner.loadTable("test_table_three", ConstantTable("test_three"));
    runner.loadTable("test_table_four", ConstantTable("_ #test_table_one"));
    Point point = pointFactory(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
    assertEquals(runner.recursiveConsultTable("test_table_one", point,
            TileType.tundra, {}, dimensions),
        "( ( test_three ) )", "two levels of recursion");
    assertEquals(runner.recursiveConsultTable("test_table_two", point,
            TileType.tundra, {}, dimensions),
        "( test_three )", "one level of recursion");
    assertEquals(runner.recursiveConsultTable("test_table_three", point,
        TileType.tundra, {}, dimensions), "test_three", "no recursion");
    assertEquals(runner.recursiveConsultTable("test_table_four", point,
            TileType.plains, {}, dimensions), "_ ( ( test_three ) )",
        "one-sided split");
}

test
void testDefaultResults() {
    ExplorationRunner runner = ExplorationRunner();
    runner.loadTable("major_rock", ConstantTable("test_rock"));
    runner.loadTable("boreal_major_tree", ConstantTable("boreal_tree"));
    runner.loadTable("temperate_major_tree", ConstantTable("temperate_tree"));
    Point point = pointFactory(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 0);
    assertEquals(runner.defaultResults(point, TileType.tundra,
        {}, dimensions), "The primary rock type here is test_rock.",
        "defaultResults in non-forest");
    assertEquals(runner.defaultResults(point, TileType.borealForest,
        {}, dimensions),
        """The primary rock type here is test_rock.
           The main kind of tree is boreal_tree.
           """, "defaultResults in boreal forest");
    assertEquals(runner.defaultResults(point, TileType.temperateForest,
        {}, dimensions),
        """The primary rock type here is test_rock.
           The main kind of tree is temperate_tree.
           """, "defaultResults in temperate forest");
}

"Test recursive checking. Note that the method returns true if the table in question, or
 one it references, does *not* exist."
test
void testRecursiveCheck() {
    ExplorationRunner runner = ExplorationRunner();
    runner.loadTable("existent_table", ConstantTable("exists"));
    assertTrue(runner.recursiveCheck("non-existent-table"), "base case of non-existent table");
    assertFalse(runner.recursiveCheck("existent_table"), "base case of existing table");
    runner.loadTable("referent_one", ConstantTable("#existent_table#"));
    runner.loadTable("referent_two", ConstantTable("( #existent_table# )"));
    runner.loadTable("referent_three", QuadrantTable(1, "#referent_one#",
        "#referent_two#"));
    assertFalse(runner.recursiveCheck("referent_three"), "recursive case to exercise cache-hits");
    runner.loadTable("false_referent", ConstantTable("#nonexistent#"));
    assertTrue(runner.recursiveCheck("false_referent"), "reference to nonexistent table");
}

"Test global-recursive checking. Note that the method returns *true* if any table
 references a nonexistent table, and *false* if the graph is self-complete."
test
void testGlobalRecursiveCheck() {
    ExplorationRunner runner = ExplorationRunner();
    assertFalse(runner.globalRecursiveCheck(), "recursive check with no tables");
    runner.loadTable("existent", ConstantTable("true_table"));
    assertFalse(runner.globalRecursiveCheck(), "recursive check with only valid tables");
    runner.loadTable("false_ref", ConstantTable("#false#"));
    assertTrue(runner.globalRecursiveCheck(), "recursive check with an invalid table");
}