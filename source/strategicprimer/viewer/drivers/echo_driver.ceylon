import java.io {
    IOException
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
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.map.fixtures {
    Ground
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.xmlio {
    mapIOHelper,
    warningLevels,
    SPFormatException
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
	ISPDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import ceylon.file {
    parsePath
}
"""A driver that reads in maps and then writes them out again---this is primarily to make
   sure that the map format is properly read, but is also useful for correcting deprecated
   syntax. (Because of that usage, warnings are disabled.)"""
service(`interface ISPDriver`)
shared class EchoDriver() satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["-e", "--echo"],
        ParamCount.two, "Read, then write a map.",
        "Read and write a map, correcting deprecated syntax.",
        true, false, "input.xml", "output.xml", "--current-turn=NN");
    """Run the driver: read the map, then write it, correcting deprecated syntax and
       forest and Ground IDs."""
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (exists inArg = args.first, exists outArg = args.rest.first, args.size == 2) {
            IMutableMapNG map;
            try {
                map = mapIOHelper.readMap(JPaths.get(inArg), warningLevels.ignore);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error reading file ``inArg``");
            } catch (XMLStreamException except) {
                throw DriverFailedException(except, "Malformed XML in ``inArg``");
            } catch (SPFormatException except) {
                throw DriverFailedException(except, "SP map format error in ``inArg``");
            }
            IDRegistrar idFactory = createIDFactory(map);
            for (location in map.locations) {
                if (exists mainForest = map.fixtures[location]?.narrow<Forest>()?.first,
                        mainForest.id < 0) {
                    Integer id = 1147200 + location.row * 176 + location.column;
                    idFactory.register(id);
                    mainForest.id = id;
                }
                if (exists mainGround = map.fixtures[location]?.narrow<Ground>()?.first,
                        mainGround.id < 0) {
                    Integer id = 1171484 + location.row * 176 + location.column;
                    idFactory.register(id);
                    mainGround.id = id;
                }
//                for (fixture in map.fixtures[location]) { // TODO: syntax sugar once compiler bug fixed
                for (fixture in map.fixtures.get(location)) {
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
                    warningLevels.default.handle(AssertionError(
                        "--current-turn must be an integer"));
                }
            }
            try {
                mapIOHelper.writeMap(parsePath(outArg), map);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error writing ``outArg``");
            }
        } else {
            throw IncorrectUsageException(usage);
        }
    }
}
"A driver to fix ID mismatches between forests and Ground in the main and player maps."
service(`interface ISPDriver`)
shared class ForestFixerDriver() satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["-f", "--fix-forest"],
        ParamCount.atLeastTwo, "Fix forest IDs",
        "Make sure that forest IDs in submaps match the main map", false, false);
    {Forest*} extractForests(IMapNG map, Point location) =>
//            map.fixtures[location].narrow<Forest>(); // TODO: syntax sugar once compiler bug fixed
            map.fixtures.get(location).narrow<Forest>(); // TODO: syntax sugar once compiler bug fixed
    {Ground*} extractGround(IMapNG map, Point location) =>
//            map.fixtures[location].narrow<Ground>();
            map.fixtures.get(location).narrow<Ground>();
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        assert (is IMultiMapModel model);
        IMutableMapNG mainMap = model.map;
        for (map->file in model.subordinateMaps) {
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
