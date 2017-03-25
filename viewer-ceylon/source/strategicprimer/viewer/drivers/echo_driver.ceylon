import strategicprimer.viewer.model.map {
    IMutableMapNG,
    IMapNG
}
import model.map {
    Point
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
import strategicprimer.viewer.model.map.fixtures.terrain {
    Forest
}
import model.map.fixtures {
    Ground
}
import strategicprimer.viewer.xmlio {
    readMap,
    writeMap
}
import strategicprimer.viewer.model {
    IMultiMapModel,
    IDriverModel,
    IDRegistrar
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
            IMutableMapNG map;
            try {
                map = readMap(JPaths.get(inArg), Warning.ignore);
            } catch (IOException except) {
                throw DriverFailedException(except, "I/O error reading file ``inArg``");
            } catch (XMLStreamException except) {
                throw DriverFailedException(except, "Malformed XML in ``inArg``");
            } catch (SPFormatException except) {
                throw DriverFailedException(except, "SP map format error in ``inArg``");
            }
            IDRegistrar idFactory = createIDFactory(map);
            for (location in map.locations) {
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
                    map.currentTurn = currentTurn;
                } else {
                    Warning.default.warn(IllegalArgumentException(
                        "--current-turn must be an integer"));
                }
            }
            try {
                writeMap(JPaths.get(outArg), map);
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
    {Forest*} extractForests(IMapNG map, Point location) {
        {Forest*} retval = { for (fixture in map.getOtherFixtures(location))
        if (is Forest fixture) fixture };
        if (exists forest = map.getForest(location)) {
            return retval.follow(forest);
        } else {
            return retval;
        }
    }
    {Ground*} extractGround(IMapNG map, Point location) {
        {Ground*} retval = { for (fixture in map.getOtherFixtures(location))
        if (is Ground fixture) fixture };
        if (exists ground = map.getGround(location)) {
            return retval.follow(ground);
        } else {
            return retval;
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        assert (is IMultiMapModel model);
        IMutableMapNG mainMap = model.map;
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