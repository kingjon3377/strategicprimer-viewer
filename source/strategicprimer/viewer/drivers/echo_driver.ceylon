import java.io {
    IOException
}
import java.lang {
    IllegalArgumentException
}
import java.nio.file {
    JPaths=Paths
}

import javax.xml.stream {
    XMLStreamException
}

import strategicprimer.model.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.map {
    Point,
    IMutableMap,
    IMap
}
import strategicprimer.model.map.fixtures {
    Ground
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.xmlio {
    readMap,
    warningLevels,
    SPFormatException,
    writeMap
}
import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    ParamCount,
    UtilityDriver,
    DriverFailedException,
    IncorrectUsageException,
    SimpleCLIDriver,
    SPOptions,
    DriverUsage,
    IDriverUsage,
    ICLIHelper
}
import ceylon.file {
    parsePath
}
"""A driver that reads in maps and then writes them out again---this is primarily to make
   sure that the map format is properly read, but is also useful for correcting deprecated
   syntax. (Because of that usage, warnings are disabled.)"""
object echoDriver satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-e", "--echo", ParamCount.two,
        "Read, then write a map.", "Read and write a map, correcting deprecated syntax.",
        "input.xml", "output.xml", "--current-turn=NN");
    """Run the driver: read the map, then write it, correcting deprecated syntax and
       forest and Ground IDs."""
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (exists inArg = args.first, exists outArg = args.rest.first, args.size == 2) {
            IMutableMap map;
            try {
                map = readMap(JPaths.get(inArg), warningLevels.ignore);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error reading file ``inArg``");
            } catch (XMLStreamException except) {
                throw DriverFailedException(except, "Malformed XML in ``inArg``");
            } catch (SPFormatException except) {
                throw DriverFailedException(except, "SP map format error in ``inArg``");
            }
            IDRegistrar idFactory = createIDFactory(map);
            for (location in map.locations) {
                if (exists mainForest = map.forest(location), mainForest.id < 0) {
                    Integer id = 1147200 + location.row * 176 + location.column;
                    idFactory.register(id);
                    mainForest.id = id;
                }
                if (exists mainGround = map.ground(location), mainGround.id < 0) {
                    Integer id = 1171484 + location.row * 176 + location.column;
                    idFactory.register(id);
                    mainGround.id = id;
                }
                for (fixture in map.otherFixtures(location)) {
                    if (is Forest fixture, fixture.id < 0) {
                        fixture.id = idFactory.createID();
                    } else if (is Ground fixture, fixture.id < 0) {
                        fixture.id = idFactory.createID();
                    }
                }
            }
            if (options.hasOption("--current-turn")) {
                value currentTurn = Integer.parse(options.getArgument("--current-turn"));
                if (is Integer currentTurn) {
                    map.currentTurn = currentTurn;
                } else {
                    warningLevels.default.handle(IllegalArgumentException(
                        "--current-turn must be an integer"));
                }
            }
            try {
                writeMap(parsePath(outArg), map);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error writing ``outArg``");
            }
        } else {
            throw IncorrectUsageException(usage);
        }
    }
}
"A driver to fix ID mismatches between forests and Ground in the main and player maps."
object forestFixerDriver satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-f", "--fix-forest",
        ParamCount.atLeastTwo, "Fix forest IDs",
        "Make sure that forest IDs in submaps match the main map");
    {Forest*} extractForests(IMap map, Point location) =>
            map.allFixtures(location).narrow<Forest>();
    {Ground*} extractGround(IMap map, Point location) =>
            map.allFixtures(location).narrow<Ground>();
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        assert (is IMultiMapModel model);
        IMutableMap mainMap = model.map;
        for ([map, file] in model.subordinateMaps) {
            cli.println("Starting ``file?.string
                else "a map with no associated path"``");
            for (location in map.locations) {
                {Forest*} mainForests = extractForests(mainMap, location);
                {Forest*} subForests = extractForests(map, location);
                for (forest in subForests) {
                    if (mainForests.contains(forest)) {
                        continue ;
                    } else if (exists matching = mainForests
                            .find(forest.equalsIgnoringID)) {
                        forest.id = matching.id;
                    } else {
                        cli.println("Unmatched forest in ``location``:``forest``");
                        mainMap.addFixture(location, forest.copy(false));
                    }
                }
                {Ground*} mainGround = extractGround(mainMap, location);
                {Ground*} subGround = extractGround(map, location);
                for (ground in subGround) {
                    if (mainGround.contains(ground)) {
                        continue;
                    } else if (exists matching = mainGround
                            .find(ground.equalsIgnoringID)) {
                        ground.id = matching.id;
                    } else {
                        cli.println("Unmatched ground in ``location``: ``ground``");
                        mainMap.addFixture(location, ground.copy(false));
                    }
                }
            }
        }
    }
}