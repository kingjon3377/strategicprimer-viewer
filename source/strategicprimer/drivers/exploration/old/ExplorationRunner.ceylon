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
    UnsupportedOperationException
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
import strategicprimer.model.map.fixtures.terrain {
    Forest
}

"""A class to create exploration results. The initial implementation is a bit hackish, and
   should be generalized and improved---except that the entire idea of generating results
   from "encounter tables" instead of selecting from "fixtures" stored in the map has been
   abandoned. This old method remains as a way of generating the map's contents *once* per
   campaign."""
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
    shared Boolean hasTable(String name) => tables[name] exists;
    "Consult a table, and if a result indicates recursion, perform it. Recursion is
     indicated by hash-marks around the name of the table to call; results are undefined
     if there are more than two hash marks in any given String, or if either is at the
     beginning or end of the string, since we use String.split"
    shared String recursiveConsultTable(String table, Point location, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        String result = consultTable(table, location, terrain, mountainous, fixtures,
            mapDimensions);
        if (result.contains('#')) {
            {String+} broken = result.split('#'.equals, true, false, 3);
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
                        if (string.contains('#')) {
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
    suppressWarnings("deprecation")
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
        case (TileType.borealForest) {
            return consultTable("boreal_major_tree", location, terrain,
                mountainous, fixtures, mapDimensions);
        } case (TileType.temperateForest) {
            return consultTable("temperate_major_tree", location, terrain,
                mountainous, fixtures, mapDimensions);
        } case (TileType.steppe) {
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
    suppressWarnings("deprecation")
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
        if (terrain == TileType.borealForest || terrain == TileType.temperateForest ||
                (!fixtures.narrow<Forest>().empty && (terrain == TileType.steppe ||
                    terrain == TileType.plains))) {
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
    todo("Consider whether non-test uses are reasonable",
        "Move tests *into* this class instead")
    shared restricted(`module strategicprimer.drivers.exploration.old`, `module strategicprimer.drivers.converters`,
                `module strategicprimer.drivers.generators`)
        void loadTable(String name, EncounterTable table) => tables[name] = table;
}

"A mock [[EncounterTable]] for the apparatus to test the ExplorationRunner."
class MockTable(String* values) satisfies EncounterTable {
    Queue<String> queue = LinkedList<String>(values);
    shared actual String generateEvent(Point point, TileType? terrain,
            Boolean mountainous, {TileFixture*} fixtures, MapDimensions mapDimensions) {
        assert (exists retval = queue.accept());
        return retval;
    }
    shared actual Set<String> allEvents {
        throw UnsupportedOperationException("mock object");
    }
}
object explorationRunnerTests {
	test
	shared void testGetPrimaryRock() {
	    ExplorationRunner runner = ExplorationRunner();
	    runner.loadTable("major_rock", MockTable("primary_rock_test"));
	    assertEquals(runner.getPrimaryRock(pointFactory(0, 0),
	            TileType.tundra, false, [], MapDimensionsImpl(69, 88, 2)),
	        "primary_rock_test", "primary rock test");
	}
	test
	suppressWarnings("deprecation")
	shared void testGetPrimaryTree() {
	    ExplorationRunner runner = ExplorationRunner();
	    runner.loadTable("boreal_major_tree", MockTable("boreal_major_test",
	        "boreal_second_test"));
	    runner.loadTable("temperate_major_tree", MockTable("temperate_major_test",
	        "temperate_second_test"));
	    Point point = pointFactory(0, 0);
	    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
	    assertEquals(runner.getPrimaryTree(point, TileType.borealForest, false, [],
	        dimensions), "boreal_major_test", "primary tree test for boreal forest");
	    assertEquals(runner.getPrimaryTree(point, TileType.temperateForest, false, [],
	        dimensions), "temperate_major_test", "primary tree test for temperate forest");
	    assertEquals(runner.getPrimaryTree(point, TileType.steppe, false,
	        {Forest("kind", false, 3)}, dimensions), "boreal_second_test",
	        "primary tree test for forest in steppe");
	    assertEquals(runner.getPrimaryTree(point, TileType.plains, false,
	        {Forest("second", false, 4)}, dimensions), "temperate_second_test",
	        "primary tree test for forest in plains");
	}

	test
	shared void testIllegalGetPrimaryTree() {
	    // TODO: Uncomment hasType() call once Ceylon tooling bug fixed
	    Point point = pointFactory(0, 0);
	    assertThatException(
	                () => ExplorationRunner().getPrimaryTree(point,
	                    TileType.tundra, false, [], MapDimensionsImpl(69, 88, 2)))
	        /*.hasType(`IllegalArgumentException`)*/;
	}

	test
	shared void testConsultTable() {
	    ExplorationRunner runner = ExplorationRunner();
	    runner.loadTable("test_table_one", MockTable("test_one"));
	    runner.loadTable("test_table_two", MockTable("test_two"));
	    runner.loadTable("test_table_three", MockTable("test_three"));
	    Point point = pointFactory(0, 0);
	    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
	    assertEquals(runner.consultTable("test_table_one", point,
	            TileType.tundra, false, [], dimensions), "test_one", "first table");
	    assertEquals(runner.consultTable("test_table_two", point,
	            TileType.tundra, false, [], dimensions), "test_two", "second table");
	    assertEquals(runner.consultTable("test_table_three", point,
	            TileType.tundra, false, [], dimensions), "test_three", "third table");
	}
	"Test the recursiveConsultTable method: the one method under test whose correctness
	 is non-obvious. We don't use mock tables here because setting them up would be
	 more trouble than they're worth."
	test
	shared void testRecursiveConsultTable() {
	    ExplorationRunner runner = ExplorationRunner();
	    runner.loadTable("test_table_one", ConstantTable("( #test_table_two# )"));
	    runner.loadTable("test_table_two", ConstantTable("( #test_table_three# )"));
	    runner.loadTable("test_table_three", ConstantTable("test_three"));
	    runner.loadTable("test_table_four", ConstantTable("_ #test_table_one"));
	    Point point = pointFactory(0, 0);
	    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
	    assertEquals(runner.recursiveConsultTable("test_table_one", point,
	            TileType.tundra, false, [], dimensions),
	        "( ( test_three ) )", "two levels of recursion");
	    assertEquals(runner.recursiveConsultTable("test_table_two", point,
	            TileType.tundra, false, [], dimensions),
	        "( test_three )", "one level of recursion");
	    assertEquals(runner.recursiveConsultTable("test_table_three", point,
	        TileType.tundra, false, [], dimensions), "test_three", "no recursion");
	    assertEquals(runner.recursiveConsultTable("test_table_four", point,
	            TileType.plains, false, [], dimensions), "_ ( ( test_three ) )",
	        "one-sided split");
	}

	test
	suppressWarnings("deprecation")
	shared void testDefaultResults() {
	    ExplorationRunner runner = ExplorationRunner();
	    runner.loadTable("major_rock", ConstantTable("test_rock"));
	    runner.loadTable("boreal_major_tree", ConstantTable("boreal_tree"));
	    runner.loadTable("temperate_major_tree", ConstantTable("temperate_tree"));
	    Point point = pointFactory(0, 0);
	    MapDimensions dimensions = MapDimensionsImpl(69, 88, 0);
	    assertEquals(runner.defaultResults(point, TileType.tundra,
	        false, [], dimensions), """The primary rock type here is test_rock.
	                            """,
	        "defaultResults in non-forest");
	    assertEquals(runner.defaultResults(point, TileType.borealForest,
	        false, [], dimensions),
	        """The primary rock type here is test_rock.
	           The main kind of tree is boreal_tree.
	           """, "defaultResults in boreal forest");
	    assertEquals(runner.defaultResults(point, TileType.temperateForest,
	        false, [], dimensions),
	        """The primary rock type here is test_rock.
	           The main kind of tree is temperate_tree.
	           """, "defaultResults in temperate forest");
	}

	"Test recursive checking. Note that the method returns true if the table in question, or
	 one it references, does *not* exist."
	test
	shared void testRecursiveCheck() {
	    ExplorationRunner runner = ExplorationRunner();
	    runner.loadTable("existent_table", ConstantTable("exists"));
	    assertTrue(runner.recursiveCheck("non-existent-table"),
	        "base case of non-existent table");
	    assertFalse(runner.recursiveCheck("existent_table"), "base case of existing table");
	    runner.loadTable("referent_one", ConstantTable("#existent_table#"));
	    runner.loadTable("referent_two", ConstantTable("( #existent_table# )"));
	    runner.loadTable("referent_three", QuadrantTable(1, "#referent_one#",
	        "#referent_two#"));
	    assertFalse(runner.recursiveCheck("referent_three"),
	        "recursive case to exercise cache-hits");
	    runner.loadTable("false_referent", ConstantTable("#nonexistent#"));
	    assertTrue(runner.recursiveCheck("false_referent"), "reference to nonexistent table");
	}

	"Test global-recursive checking. Note that the method returns *true* if any table
	 references a nonexistent table, and *false* if the graph is self-complete."
	test
	shared void testGlobalRecursiveCheck() {
	    ExplorationRunner runner = ExplorationRunner();
	    assertFalse(runner.globalRecursiveCheck(), "recursive check with no tables");
	    runner.loadTable("existent", ConstantTable("true_table"));
	    assertFalse(runner.globalRecursiveCheck(), "recursive check with only valid tables");
	    runner.loadTable("false_ref", ConstantTable("#false#"));
	    assertTrue(runner.globalRecursiveCheck(), "recursive check with an invalid table");
	}
}