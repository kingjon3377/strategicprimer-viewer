import java.awt {
    Image,
    Graphics
}
import java.awt.image {
    BufferedImage
}
import java.nio.file {
    Path,
	Paths
}
import java.util.\ifunction {
    Predicate
}

import strategicprimer.viewer.drivers.map_viewer {
    CachingTileDrawHelper,
    directTileDrawHelper,
    Ver2TileDrawHelper
}
import strategicprimer.drivers.common {
    SPOptions,
    IDriverUsage,
    DriverUsage,
    ParamCount,
	UtilityDriver,
	IncorrectUsageException
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.map {
    MapDimensions,
    TileFixture,
    IMapNG,
    pointFactory,
    clearPointCache,
	CachingStrategy,
	pointCachingStrategy
}
import ceylon.random {
    DefaultRandom,
	Random,
	randomize
}
import strategicprimer.model.xmlio {
	readMap,
	warningLevels
}
import ceylon.collection {
	MutableMap,
	HashMap
}
import ceylon.file {
	File,
	parsePath,
	Nil
}

"The first test: all in one place."
Integer first(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    BufferedImage image = BufferedImage(tileSize, tileSize, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0:reps) {
        image.flush();
        for (point in map.locations) {
            helper.drawTileTranslated(image.createGraphics(), map, point, tileSize,
                tileSize);
        }
    }
    Integer end = system.nanoseconds;
    return end - start;
}
"The second test: Translating."
Integer second(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions;
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    Coordinate dimensions = coordinateFactory(tileSize, tileSize);
    for (rep in 0:reps) {
        image.flush();
        for (point in map.locations) {
            helper.drawTile(image.createGraphics(), map, point,
                coordinateFactory(point.row * tileSize, point.column * tileSize),
                dimensions);
        }
    }
    Integer end = system.nanoseconds;
    return end - start;
}
"Third test: in-place, reusing Graphics."
Integer third(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    BufferedImage image = BufferedImage(tileSize, tileSize, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0:reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        for (point in map.locations) {
            helper.drawTileTranslated(pen, map, point, tileSize, tileSize);
        }
        pen.dispose();
    }
    Integer end = system.nanoseconds;
    return end - start;
}
"Fourth test: translating, reusing Graphics."
Integer fourth(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions;
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0:reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = coordinateFactory(tileSize, tileSize);
        for (point in map.locations) {
            helper.drawTile(pen, map, point, coordinateFactory(point.row * tileSize,
                point.column * tileSize), dimensions);
        }
        pen.dispose();
    }
    Integer end = system.nanoseconds;
    return end - start;
}
Range<Integer> testRowSpan = 20..40;
Range<Integer> testColSpan = 55..82;
"Fifth test, part one: iterating."
Integer fifthOne(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions;
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0:reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = coordinateFactory(tileSize, tileSize);
        for (row in testRowSpan) {
            for (col in testColSpan) {
                helper.drawTile(pen, map, pointFactory(row, col),
                    coordinateFactory(row * tileSize, col * tileSize),
                    dimensions);
            }
        }
        pen.dispose();
    }
    Integer end = system.nanoseconds;
    return end - start;
}
Integer fifthTwo(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions;
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0:reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = coordinateFactory(tileSize, tileSize);
        for (point in map.locations) {
            if (testRowSpan.contains(point.row) && testColSpan.contains(point.column)) {
                helper.drawTile(pen, map, point,
                    coordinateFactory(point.row * tileSize, point.column * tileSize),
                    dimensions);
            }
        }
        pen.dispose();
    }
    Integer end = system.nanoseconds;
    return end - start;
}
{[String, Integer(TileDrawHelper, IMapNG, Integer, Integer)]*} tests = {
    ["1. All in one place", first],
    ["2. Translating", second],
    ["3. In-place, reusing Graphics", third],
    ["4. Translating, reusing Graphics", fourth],
    ["5a. Ordered iteration vs filtering: Iteration", fifthOne],
    ["5b. Ordered iteration vs filtering: Filtering", fifthTwo]
};
class Accumulator() {
    variable Integer accumulatedValue = 0;
    shared Integer storedValue => accumulatedValue;
    shared void add(Integer addend) { accumulatedValue = accumulatedValue + addend; }
}
Boolean dummyObserver(Image? image, Integer infoflags,
        Integer xCoordinate, Integer yCoordinate, Integer width, Integer height) =>
            false;

Boolean dummyFilter(TileFixture? fix) => true;
object dummyPredicate satisfies Predicate<TileFixture> {
    shared actual Boolean test(TileFixture? t) => true;
}
{[TileDrawHelper, String]*} createHelpers() => { [CachingTileDrawHelper(), "Caching:"],
	[directTileDrawHelper, "Direct:"],
	[Ver2TileDrawHelper(dummyObserver, dummyFilter, {FixtureMatcher(dummyFilter, "test")}), "Ver 2:"]
};
MutableMap<[CachingStrategy, String, String, String], Accumulator> results = HashMap<[CachingStrategy, String, String, String], Accumulator>();
Accumulator getResultsAccumulator(String file, String testee, String test) {
	[CachingStrategy, String, String, String] tuple = [pointCachingStrategy, file, testee, test];
	if (exists retval = results[tuple]) {
		return retval;
	} else {
		Accumulator retval = Accumulator();
		results.put(tuple, retval);
		return retval;
	}
}
{[TileDrawHelper, String]*} helpers = createHelpers();
"Run all the tests on the specified map."
void runAllTests(ICLIHelper cli, IMapNG map, String fileName, Integer repetitions) {
	Integer printStats(String prefix, Integer total, Integer reps) {
		cli.println("``prefix``\t``total``, average of ``total / reps`` ns.");
		return total;
	}
    for ([testDesc, test] in tests) {
        cli.println("``testDesc``:");
        for ([testCase, caseDesc] in helpers) {
            Accumulator accumulator = getResultsAccumulator(fileName, caseDesc, testDesc);
            accumulator.add(printStats(caseDesc, test(testCase, map, repetitions,
                scaleZoom(ViewerModel.defaultZoomLevel, map.dimensions.version)),
                repetitions));
        }
    }
    cli.println("----------------------------------------");
    cli.print("Total:");
    for ([testCase, caseDesc] in helpers) {
        printStats(caseDesc, results.filterKeys((tuple) => tuple.startsWith([pointCachingStrategy, fileName, caseDesc]))
            .items.map(Accumulator.storedValue).fold(0)(plus), repetitions);
    }
    cli.println("");
}
"A driver to compare the performance of TileDrawHelpers."
shared object drawHelperComparator satisfies UtilityDriver {
    shared actual IDriverUsage usage = DriverUsage {
	        graphical = true;
	        invocations = ["-t", "--test"];
	        paramsWanted = ParamCount.atLeastOne;
	        shortDescription = "Test drawing performance.";
	        longDescription =
	            """Test the performance of the TileDrawHelper classes---which do the heavy lifting
	               of rendering the map in the viewer---using a variety of automated tests.""";
	        supportedOptionsTemp = ["--report=out.csv"];
    };
    "Run the tests."
    shared actual void startDriverOnArguments(ICLIHelper cli,
            SPOptions options, String* args) {
        if (args.size == 0) {
            throw IncorrectUsageException(usage);
        }
        Random random = DefaultRandom();
        Integer reps = 50;
        {CachingStrategy*} cachingStrategies = `CachingStrategy`.caseValues;
        void runTestProcedure(ICLIHelper cli, IMapNG map, String filename) {
            cli.println("Testing using ``filename``");
            clearPointCache();
            clearCoordinateCache();
            for (strategy in randomize(cachingStrategies)) {
                pointCachingStrategy = strategy;
                coordinateCachingStrategy = strategy;
                cli.println("Using ``strategy`` caching strategy");
                runAllTests(cli, map, filename, reps);
            }
        }
        MutableMap<String, Integer> mapSizes = HashMap<String, Integer>();
        for (arg in args) {
            Path path = Paths.get(arg);
            IMapNG map = readMap(path, warningLevels.ignore);
            mapSizes[arg] = map.locations.size;
            runTestProcedure(cli, map, path.string else "an unsaved map");
        }
        String reportFilename = options.getArgument("--report");
        if (reportFilename != "false") {
            File outFile;
            switch (resource = parsePath(reportFilename).resource)
            case (is Nil) {
                outFile = resource.createFile();
            }
            case (is File) {
                outFile = resource;
            }
            else {
                cli.println("Specified file to write details to is present but not a regular file");
                return;
            }
            try (writer = outFile.Overwriter()) {
                writer.writeLine(
                    "Filename,Tile Count,Caching,DrawHelper Tested,Test Case,Repetitions,Time (ns)");
                for ([cachingStrategy, file, helper, test]->total in results) {
                    writer.write("\"``file``\",``mapSizes[file] else ""``,``cachingStrategy``");
                    writer.writeLine(",\"``helper``\",\"``test``\",``reps``,``total.storedValue``");
                }
            }
        }
    }
}
