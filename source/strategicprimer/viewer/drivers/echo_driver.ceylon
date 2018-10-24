import java.io {
    IOException
}

import javax.xml.stream {
    XMLStreamException
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    Point,
    IMutableMapNG,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    Ground
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import strategicprimer.model.common.xmlio {
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
    SPOptions,
    DriverUsage,
    IDriverUsage,
    CLIDriver,
    DriverFactory,
    UtilityDriverFactory,
    ModelDriverFactory,
    ModelDriver,
    SimpleMultiMapModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.common {
    PathWrapper
}

"A factory for a driver that reads in maps and then writes them out again, to test the
 map-reading logic and to correct deprecated syntax."
service(`interface DriverFactory`)
shared class EchoDriverFactory satisfies UtilityDriverFactory {
    shared static IDriverUsage staticUsage = DriverUsage(false, ["-e", "--echo"],
        ParamCount.two, "Read, then write a map.",
        "Read and write a map, correcting deprecated syntax.",
        true, false, "input.xml", "output.xml", "--current-turn=NN");

    shared new () {}

    shared actual IDriverUsage usage => staticUsage;

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            EchoDriver(options);
}

"""A driver that reads in maps and then writes them out again---this is primarily to make
   sure that the map format is properly read, but is also useful for correcting deprecated
   syntax. (Because of that usage, warnings are disabled.)"""
shared class EchoDriver(SPOptions options) satisfies UtilityDriver {
    """Run the driver: read the map, then write it, correcting deprecated syntax and
       forest and Ground IDs."""
    shared actual void startDriver(String* args) {
        if (exists inArg = args.first, exists outArg = args.rest.first, args.size == 2) {
            IMutableMapNG map;
            try {
                map = mapIOHelper.readMap(PathWrapper(inArg), warningLevels.ignore);
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
                    mainForest.id = idFactory.register(1147200 + location.row * 176 + location.column);
                }
                if (exists mainGround = map.fixtures[location]?.narrow<Ground>()?.first,
                        mainGround.id < 0) {
                    mainGround.id = idFactory.register(1171484 + location.row * 176 + location.column);
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
                mapIOHelper.writeMap(PathWrapper(outArg), map);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error writing ``outArg``");
            }
        } else {
            throw IncorrectUsageException(EchoDriverFactory.staticUsage);
        }
    }
}

"A factory for a driver to fix ID mismatches between forests and Ground in the main and
 player maps."
service(`interface DriverFactory`)
shared class ForestFixerFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage(false, ["-f", "--fix-forest"],
        ParamCount.atLeastTwo, "Fix forest IDs",
        "Make sure that forest IDs in submaps match the main map", false, false);

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            return ForestFixerDriver(cli, options, model);
        } else {
            return createDriver(cli, options, SimpleMultiMapModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            SimpleMultiMapModel(map, path);
}

"A driver to fix ID mismatches between forests and Ground in the main and player maps."
shared class ForestFixerDriver(ICLIHelper cli, SPOptions options, model)
        satisfies CLIDriver {
    shared actual IMultiMapModel model;
    {Forest*} extractForests(IMapNG map, Point location) =>
//            map.fixtures[location].narrow<Forest>(); // TODO: syntax sugar once compiler bug fixed
            map.fixtures.get(location).narrow<Forest>();
    {Ground*} extractGround(IMapNG map, Point location) =>
//            map.fixtures[location].narrow<Ground>(); // TODO: syntax sugar once compiler bug fixed
            map.fixtures.get(location).narrow<Ground>();

    shared actual void startDriver() {
        IMutableMapNG mainMap = model.map;
        for (map->[file, _] in model.subordinateMaps) {
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
