import controller.map.drivers {
    UtilityDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    IncorrectUsageException,
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper,
    MapReaderAdapter,
    IDRegistrar,
    IDFactoryFiller
}
import model.map {
    IMutableMapNG
}
import java.nio.file {
    JPaths = Paths
}
import util {
    Warning
}
import java.io {
    IOException
}
import controller.map.formatexceptions {
    SPFormatException
}
import javax.xml.stream {
    XMLStreamException
}
import java.lang {
    IllegalArgumentException
}
import model.map.fixtures.terrain {
    Forest
}
import model.map.fixtures {
    Ground
}
"""A driver that reads in maps and then writes them out again---this is primarily to make
   sure that the map format is properly read, but is also useful for correcting deprecated
   syntax. (Because of that usage, warnings are disabled.)"""
object echoDriver satisfies UtilityDriver {
    DriverUsage usageObject = DriverUsage(false, "-e", "--echo", ParamCount.two,
        "Read, then write a map.", "Read and write a map, correcting deprecated syntax.");
    usageObject.addSupportedOption("--current-turn=NN");
    usageObject.firstParamDesc = "input.xml";
    usageObject.subsequentParamDesc = "output.xml";
    shared actual IDriverUsage usage() => usageObject;
    """Run the driver: read the map, then write it, correcting deprecated syntax and
       forest and Ground IDs."""
    shared actual void startDriver(ICLIHelper cli, SPOptions options, String?* args) {
        if (exists inArg = args.first, exists outArg = args.rest.first, args.size == 2) {
            MapReaderAdapter reader = MapReaderAdapter();
            IMutableMapNG map;
            try {
                map = reader.readMap(JPaths.get(inArg), Warning.ignore);
            } catch (IOException except) {
                throw DriverFailedException("I/O error reading file ``inArg``", except);
            } catch (XMLStreamException except) {
                throw DriverFailedException("Malformed XML in ``inArg``", except);
            } catch (SPFormatException except) {
                throw DriverFailedException("SP map format error in ``inArg``", except);
            }
            IDRegistrar idFactory = IDFactoryFiller.createFactory(map);
            for (location in map.locations()) {
                if (exists mainForest = map.getForest(location), mainForest.id < 0) {
                    Integer id = 1147200 + location.row * 176 + location.col;
                    idFactory.register(id);
                    mainForest.id = id;
                }
                if (exists mainGround = map.getGround(location), mainGround.id < 0) {
                    Integer id = 1171484 + location.row * 176 + location.col;
                    idFactory.register(id);
                    mainGround.id = id;
                }
                for (fixture in map.getOtherFixtures(location)) {
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
                    map.setCurrentTurn(currentTurn);
                } else {
                    Warning.default.warn(IllegalArgumentException(
                        "--current-turn must be an integer"));
                }
            }
            try {
                reader.write(JPaths.get(outArg), map);
            } catch (IOException except) {
                throw DriverFailedException("I/O error writing ``outArg``", except);
            }
        } else {
            throw IncorrectUsageException(usageObject);
        }
    }
}