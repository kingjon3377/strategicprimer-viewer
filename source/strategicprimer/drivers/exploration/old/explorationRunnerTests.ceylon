import ceylon.collection {
    Queue,
    LinkedList
}

import ceylon.test {
    assertEquals,
    assertThatException,
    assertTrue,
    assertFalse,
    test
}

import lovelace.util.common {
    defer
}

import strategicprimer.model.common.map {
    Point,
    TileType,
    MapDimensionsImpl,
    MapDimensions
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

"Tests of the [[ExplorationRunner]]."
object explorationRunnerTests {
    "Test of the [[ExplorationRunner.getPrimaryRock]] method."
    test
    shared void testGetPrimaryRock() {
        ExplorationRunner runner = ExplorationRunner();
        runner.loadTable("major_rock", MockTable("primary_rock_test"));
        assertEquals(runner.getPrimaryRock(Point(0, 0),
                TileType.tundra, false, [], MapDimensionsImpl(69, 88, 2)),
            "primary_rock_test", "primary rock test");
    }

    "Test of the [[ExplorationRunner.getPrimaryTree]] method."
    test
    shared void testGetPrimaryTree() {
        ExplorationRunner runner = ExplorationRunner();
        runner.loadTable("boreal_major_tree", MockTable("boreal_major_test"));
        runner.loadTable("temperate_major_tree", MockTable("temperate_major_test"));
        Point point = Point(0, 0);
        MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
        assertEquals(runner.getPrimaryTree(point, TileType.steppe, false,
            {Forest("kind", false, 3)}, dimensions), "boreal_major_test",
            "primary tree test for forest in steppe");
        assertEquals(runner.getPrimaryTree(point, TileType.plains, false,
            {Forest("second", false, 4)}, dimensions), "temperate_major_test",
            "primary tree test for forest in plains");
    }

    "Test that [[ExplorationRunner.getPrimaryTree]] objects to being called on
     non-forested tiles."
    test
    shared void testIllegalGetPrimaryTree() =>
        assertThatException(
                    defer(ExplorationRunner().getPrimaryTree, [Point(0, 0),
                        TileType.tundra, false, [], MapDimensionsImpl(69, 88, 2)]))
            /*.hasType(`IllegalArgumentException`)*/; // TODO: Uncomment once Ceylon tooling bug fixed

    "Test the [[ExplorationRunner.consultTable]] method."
    test
    shared void testConsultTable() {
        ExplorationRunner runner = ExplorationRunner();
        runner.loadTable("test_table_one", MockTable("test_one"));
        runner.loadTable("test_table_two", MockTable("test_two"));
        runner.loadTable("test_table_three", MockTable("test_three"));
        Point point = Point(0, 0);
        MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
        assertEquals(runner.consultTable("test_table_one", point,
                TileType.tundra, false, [], dimensions), "test_one", "first table");
        assertEquals(runner.consultTable("test_table_two", point,
                TileType.tundra, false, [], dimensions), "test_two", "second table");
        assertEquals(runner.consultTable("test_table_three", point,
                TileType.tundra, false, [], dimensions), "test_three", "third table");
    }

    "Test the [[ExplorationRunner.recursiveConsultTable]] method: the one
     method under test whose correctness is non-obvious. We don't use mock
     tables here because setting them up would be more trouble than they're
     worth."
    test
    shared void testRecursiveConsultTable() {
        ExplorationRunner runner = ExplorationRunner();
        runner.loadTable("test_table_one", ConstantTable("( #test_table_two# )"));
        runner.loadTable("test_table_two", ConstantTable("( #test_table_three# )"));
        runner.loadTable("test_table_three", ConstantTable("test_three"));
        runner.loadTable("test_table_four", ConstantTable("_ #test_table_one"));
        Point point = Point(0, 0);
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

    "Test the [[ExplorationRunner.defaultResults]] method."
    test
    shared void testDefaultResults() {
        ExplorationRunner runner = ExplorationRunner();
        runner.loadTable("major_rock", ConstantTable("test_rock"));
        runner.loadTable("boreal_major_tree", ConstantTable("boreal_tree"));
        runner.loadTable("temperate_major_tree", ConstantTable("temperate_tree"));
        Point point = Point(0, 0);
        MapDimensions dimensions = MapDimensionsImpl(69, 88, 0);
        assertEquals(runner.defaultResults(point, TileType.tundra,
            false, [], dimensions), """The primary rock type here is test_rock.
                                """,
            "defaultResults in non-forest");
        assertEquals(runner.defaultResults(point, TileType.steppe,
            false, [Forest("boreal_tree", false, 1)], dimensions),
            """The primary rock type here is test_rock.
               The main kind of tree is boreal_tree.
               """, "defaultResults in boreal forest");
        assertEquals(runner.defaultResults(point, TileType.plains,
            false, [Forest("temperate_tree", false, 2)], dimensions),
            """The primary rock type here is test_rock.
               The main kind of tree is temperate_tree.
               """, "defaultResults in temperate forest");
    }

    "Test recursive checking. Note that the method returns true if the table in
     question, or one it references, does *not* exist."
    test
    shared void testRecursiveCheck() {
        ExplorationRunner runner = ExplorationRunner();
        runner.loadTable("existent_table", ConstantTable("exists"));
        assertTrue(runner.recursiveCheck("non-existent-table"),
            "base case of non-existent table");
        assertFalse(runner.recursiveCheck("existent_table"),
            "base case of existing table");
        runner.loadTable("referent_one", ConstantTable("#existent_table#"));
        runner.loadTable("referent_two", ConstantTable("( #existent_table# )"));
        runner.loadTable("referent_three", QuadrantTable(1, "#referent_one#",
            "#referent_two#"));
        assertFalse(runner.recursiveCheck("referent_three"),
            "recursive case to exercise cache-hits");
        runner.loadTable("false_referent", ConstantTable("#nonexistent#"));
        assertTrue(runner.recursiveCheck("false_referent"),
            "reference to nonexistent table");
    }

    "Test global-recursive checking. Note that the method returns *true* if any table
     references a nonexistent table, and *false* if the graph is self-complete."
    test
    shared void testGlobalRecursiveCheck() {
        ExplorationRunner runner = ExplorationRunner();
        assertFalse(runner.globalRecursiveCheck(), "recursive check with no tables");
        runner.loadTable("existent", ConstantTable("true_table"));
        assertFalse(runner.globalRecursiveCheck(),
            "recursive check with only valid tables");
        runner.loadTable("false_ref", ConstantTable("#false#"));
        assertTrue(runner.globalRecursiveCheck(),
            "recursive check with an invalid table");
    }
    "Test loading [[quadrant-based tables|QuadrantTable]]."
    test
    shared void testLoadQuadrantTable() {
        ExplorationRunner runner = ExplorationRunner();
        Queue<String> data = LinkedList<String>(["quadrant", "2", "one", "two", "three",
            "four", "five", "six"]);
        String table = "testLoadQuadrantTable().result";
        runner.loadTableFromDataStream(data.accept, table);
        Point point = Point(0, 0);
        MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
        assertEquals("one", runner.consultTable(table, point, TileType.tundra, false,
            [], dimensions), "loading quadrant table");
        Point pointTwo = Point(36, 30);
        assertEquals("one", runner.consultTable(table, point, TileType.ocean, false,
            [], dimensions), "quadrant table isn't a terrain table");
        assertEquals( runner.consultTable(table, pointTwo, TileType.tundra, false,
            [], dimensions), "five", "quadrant table isn't a constant table");
        assertEquals( runner.consultTable(table, pointTwo, TileType.tundra, false,
            [], MapDimensionsImpl(35, 32, 2)), "six",
            "quadrant table can use alternate dimensions");
        assertThatException(defer(runner.loadTableFromDataStream,
            [LinkedList{"quadrant"}.accept, "testLoadQuadrantTable().illegal"]));
    }

    "A mock object to make sure that tables other than [[QuadrantTable]] do not
     use the map dimensions."
    suppressWarnings("expressionTypeNothing")
    object mockDimensions satisfies MapDimensions {
        shared actual Integer rows => nothing;
        shared actual Integer columns => nothing;
        shared actual Integer version => nothing;
    }

    "Test loading [[random-number-based tables|RandomTable]]."
    test
    shared void testLoadRandomTable() {
        ExplorationRunner runner = ExplorationRunner();
        String table = "testLoadRandomTable()";
        runner.loadTableFromDataStream(LinkedList{"random", "0 one", "99 two"}.accept,
            table);
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.tundra,
            false, [], mockDimensions), "one", "loading random table");
    }

    "Test loading [[terrain-based tables|TerrainTable]]."
    test
    shared void testLoadTerrainTable() {
        ExplorationRunner runner = ExplorationRunner();
        String table = "testLoadTerrainTable()";
        runner.loadTableFromDataStream(LinkedList{"terrain", "tundra one",
        "plains two", "ocean three", "mountain four", "temperate_forest five"}.accept,
            table);
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.tundra,
            false, [], mockDimensions), "one", "loading terrain table: tundra");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains,
            false, [], mockDimensions), "two", "loading terrain table: plains");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.ocean, false,
            [], mockDimensions), "three", "loading terrain table: ocean");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains,
            false, [Forest("forestKind", false, 1)], mockDimensions), "five",
            "loading terrain table: version 2 equivalent of temperate forest");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains, true,
            [], mockDimensions), "four",
            "loading terrain table: version 2 equivalent of mountain");
    }

    """Test loading [[constant "tables"|ConstantTable]]"""
    test
    shared void testLoadConstantTable() {
        ExplorationRunner runner = ExplorationRunner();
        String table = "testLoadConstantTable()";
        runner.loadTableFromDataStream(LinkedList{"constant", "one"}.accept, table);
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains,
            false, [], mockDimensions), "one");
    }

    "Test that the table-loading code correctly rejects invalid input."
    test
    shared void testTableLoadingInvalidInput() {
        ExplorationRunner runner = ExplorationRunner();
        // no data
        assertThatException(defer(runner.loadTableFromDataStream,
            [LinkedList{""}.accept, "testTableLoadingInvalidInput().noData"]));
        // invalid header
        assertThatException(defer(runner.loadTableFromDataStream,
            [LinkedList{"2", "invalidData", "invalidData"}.accept,
                "testTableLoadingInvalidInput().invalidHeader"]));
    }
}
