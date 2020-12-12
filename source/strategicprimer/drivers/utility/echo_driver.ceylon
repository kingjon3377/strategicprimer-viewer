import java.io {
    IOException
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    IMutableMapNG
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
    ParamCount,
    UtilityDriver,
    DriverFailedException,
    IncorrectUsageException,
    SPOptions,
    DriverUsage,
    IDriverUsage,
    DriverFactory,
    UtilityDriverFactory
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.common {
    PathWrapper,
    MalformedXMLException
}

"A factory for a driver that reads in maps and then writes them out again, to test the
 map-reading logic and to correct deprecated syntax."
service(`interface DriverFactory`)
shared class EchoDriverFactory satisfies UtilityDriverFactory {
    shared static IDriverUsage staticUsage = DriverUsage(false, "echo",
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
shared class EchoDriver(options) satisfies UtilityDriver {
    shared actual SPOptions options;
    """Run the driver: read the map, then write it, correcting deprecated syntax and
       forest and Ground IDs."""
    shared actual void startDriver(String* args) {
        if (exists inArg = args.first, exists outArg = args.rest.first, args.size == 2) {
            IMutableMapNG map;
            try {
                map = mapIOHelper.readMap(PathWrapper(inArg), warningLevels.ignore);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error reading file ``inArg``");
            } catch (MalformedXMLException except) {
                throw DriverFailedException(except, "Malformed XML in ``inArg``");
            } catch (SPFormatException except) {
                throw DriverFailedException(except, "SP map format error in ``inArg``");
            }
            IDRegistrar idFactory = createIDFactory(map);
            Integer columnCount = map.dimensions.columns;
            for (location in map.locations) {
                if (exists mainForest = map.fixtures[location]?.narrow<Forest>()?.first,
                        mainForest.id < 0) {
                    mainForest.id = idFactory.register(
                        1147200 + location.row * columnCount + location.column);
                }
                if (exists mainGround = map.fixtures[location]?.narrow<Ground>()?.first,
                        mainGround.id < 0) {
                    mainGround.id = idFactory.register(
                        1171484 + location.row * columnCount + location.column);
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
