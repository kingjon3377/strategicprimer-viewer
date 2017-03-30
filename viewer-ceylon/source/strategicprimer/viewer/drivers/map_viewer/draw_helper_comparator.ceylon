import java.awt {
    Image,
    Graphics
}
import java.awt.image {
    BufferedImage
}
import java.nio.file {
    Path
}
import java.util {
    Random
}
import java.util.\ifunction {
    Predicate
}

import model.map {
    MapDimensions
}

import strategicprimer.viewer.drivers {
    SPOptions,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SimpleCLIDriver,
    ICLIHelper
}
import strategicprimer.viewer.drivers.map_viewer {
    CachingTileDrawHelper,
    DirectTileDrawHelper,
    Ver2TileDrawHelper
}
import strategicprimer.viewer.model {
    IMultiMapModel,
    IDriverModel
}
import strategicprimer.viewer.model.map {
    TileFixture,
    IMapNG,
    pointFactory,
    clearPointCache
}

variable Boolean usePointCache = false;
"The first test: all in one place."
Integer first(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    BufferedImage image = BufferedImage(tileSize, tileSize, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0..reps) {
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
    Coordinate dimensions = coordinateFactory(tileSize, tileSize, usePointCache);
    for (rep in 0..reps) {
        image.flush();
        for (point in map.locations) {
            helper.drawTile(image.createGraphics(), map, point,
                coordinateFactory(point.row * tileSize, point.col * tileSize,
                    usePointCache), dimensions);
        }
    }
    Integer end = system.nanoseconds;
    return end - start;
}
"Third test: in-place, reusing Graphics."
Integer third(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    BufferedImage image = BufferedImage(tileSize, tileSize, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0..reps) {
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
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = coordinateFactory(tileSize, tileSize, usePointCache);
        for (point in map.locations) {
            helper.drawTile(pen, map, point, coordinateFactory(point.row * tileSize,
                point.col * tileSize, usePointCache), dimensions);
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
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = coordinateFactory(tileSize, tileSize, usePointCache);
        for (row in testRowSpan) {
            for (col in testColSpan) {
                helper.drawTile(pen, map, pointFactory(row, col, usePointCache),
                    coordinateFactory(row * tileSize, col * tileSize, usePointCache),
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
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = coordinateFactory(tileSize, tileSize, usePointCache);
        for (point in map.locations) {
            if (testRowSpan.contains(point.row) && testColSpan.contains(point.col)) {
                helper.drawTile(pen, map, point,
                    coordinateFactory(point.row * tileSize, point.col * tileSize,
                        usePointCache), dimensions);
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
{[TileDrawHelper, String, Accumulator]*} helpers = {
    [CachingTileDrawHelper(), "Caching:", Accumulator()],
    [DirectTileDrawHelper(), "Direct:", Accumulator()],
    [Ver2TileDrawHelper(dummyObserver, dummyFilter,
        {FixtureMatcher(dummyFilter, "test")}),
        "Ver. 2:", Accumulator()]
};
"Run all the tests on the specified map."
void runAllTests(ICLIHelper cli, IMapNG map, Integer repetitions) {
    Integer printStats(String prefix, Integer total, Integer reps) {
        cli.println("``prefix``\t``total``, average of ``total / reps`` ns.");
        return total;
    }
    for ([testDesc, test] in tests) {
        cli.println("``testDesc``:");
        for ([testCase, caseDesc, accumulator] in helpers) {
            accumulator.add(printStats(caseDesc, test(testCase, map, repetitions,
                scaleZoom(ViewerModel.defaultZoomLevel, map.dimensions.version)),
                repetitions));
        }
    }
    cli.println("----------------------------------------");
    cli.print("Total:");
    for ([testCase, caseDesc, accumulator] in helpers) {
        printStats(caseDesc, accumulator.storedValue, repetitions);
    }
    cli.println("");
}
"A driver to compare the performance of TileDrawHelpers."
shared object drawHelperComparator satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage(true, "-t", "--test", ParamCount.atLeastOne,
        "Test drawing performance.",
        """Test the performance of the TileDrawHelper classes---which do the heavy lifting
           of rendering the map in the viewer---using a variety of automated tests.""");
    "Run the tests."
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        Boolean() random = Random().nextBoolean;
        void runTestProcedure(ICLIHelper cli, IMapNG map, Path? filename,
                Boolean() rng) {
            cli.println("Testing using ``filename?.string else "an unsaved map"``");
            clearPointCache();
            usePointCache = rng();
            String cachingMessage(Boolean caching) {
                return (caching) then "Using cache:" else "Not using cache:";
            }
            cli.println(cachingMessage(usePointCache));
            Integer reps = 50;
            runAllTests(cli, map, reps);
            usePointCache = !usePointCache;
            cli.println(cachingMessage(usePointCache));
            runAllTests(cli, map, reps);
        }
        if (is IMultiMapModel model) {
            for ([map, file] in model.allMaps) {
                runTestProcedure(cli, map, file, random);
            }
        } else {
            runTestProcedure(cli, model.map, model.mapFile, random);
        }
    }
}