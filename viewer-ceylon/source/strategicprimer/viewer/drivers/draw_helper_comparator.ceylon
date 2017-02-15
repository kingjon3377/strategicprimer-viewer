import controller.map.drivers {
    SPOptions
}
import controller.map.misc {
    ICLIHelper
}

import java.awt {
    Image,
    Graphics
}
import java.awt.image {
    ImageObserver,
    BufferedImage
}
import java.nio.file {
    Path
}
import java.util {
    JCollections=Collections,
    Optional,
    Random
}
import java.util.\ifunction {
    Predicate
}

import model.map {
    IMapNG,
    TileFixture,
    PointFactory,
    MapDimensions
}
import model.misc {
    IMultiMapModel,
    IDriverModel
}
import model.viewer {
    ZOrderFilter,
    FixtureMatcher,
    TileViewSize,
    ViewerModel
}

import view.map.main {
    TileDrawHelper,
    CachingTileDrawHelper,
    DirectTileDrawHelper,
    Ver2TileDrawHelper
}
import view.util {
    Coordinate
}
"The first test: all in one place."
Integer first(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    BufferedImage image = BufferedImage(tileSize, tileSize, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0..reps) {
        image.flush();
        for (point in map.locations()) {
            helper.drawTileTranslated(image.createGraphics(), map, point, tileSize,
                tileSize);
        }
    }
    Integer end = system.nanoseconds;
    return end - start;
}
"The second test: Translating."
Integer second(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions();
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
    for (rep in 0..reps) {
        image.flush();
        for (point in map.locations()) {
            helper.drawTile(image.createGraphics(), map, point,
                PointFactory.coordinate(point.row * tileSize, point.col * tileSize),
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
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        for (point in map.locations()) {
            helper.drawTileTranslated(pen, map, point, tileSize, tileSize);
        }
        pen.dispose();
    }
    Integer end = system.nanoseconds;
    return end - start;
}
"Fourth test: translating, reusing Graphics."
Integer fourth(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions();
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
        for (point in map.locations()) {
            helper.drawTile(pen, map, point, PointFactory.coordinate(point.row * tileSize,
                point.col * tileSize), dimensions);
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
    MapDimensions mapDimensions = map.dimensions();
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
        for (row in testRowSpan) {
            for (col in testColSpan) {
                helper.drawTile(pen, map, PointFactory.point(row, col),
                    PointFactory.coordinate(row * tileSize, col * tileSize), dimensions);
            }
        }
        pen.dispose();
    }
    Integer end = system.nanoseconds;
    return end - start;
}
Integer fifthTwo(TileDrawHelper helper, IMapNG map, Integer reps, Integer tileSize) {
    MapDimensions mapDimensions = map.dimensions();
    BufferedImage image = BufferedImage(tileSize * mapDimensions.columns,
        tileSize * mapDimensions.rows, BufferedImage.typeIntRgb);
    Integer start = system.nanoseconds;
    for (rep in 0..reps) {
        image.flush();
        Graphics pen = image.createGraphics();
        Coordinate dimensions = PointFactory.coordinate(tileSize, tileSize);
        for (point in map.locations()) {
            if (testRowSpan.contains(point.row) && testColSpan.contains(point.col)) {
                helper.drawTile(pen, map, point,
                    PointFactory.coordinate(point.row * tileSize, point.col * tileSize),
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
object dummyObserver satisfies ImageObserver {
    shared actual Boolean imageUpdate(Image? image, Integer infoflags,
        Integer xCoordinate, Integer yCoordinate, Integer width, Integer height) =>
            false;
}
object dummyFilter satisfies ZOrderFilter {
    shared actual Boolean shouldDisplay(TileFixture? fix) => true;
}
object dummyPredicate satisfies Predicate<TileFixture> {
    shared actual Boolean test(TileFixture? t) => true;
}
{[TileDrawHelper, String, Accumulator]*} helpers = {
    [CachingTileDrawHelper(), "Caching:", Accumulator()],
    [DirectTileDrawHelper(), "Direct:", Accumulator()],
    [Ver2TileDrawHelper(dummyObserver, dummyFilter,
        JCollections.singleton(FixtureMatcher(dummyPredicate, "test"))),
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
                TileViewSize.scaleZoom(ViewerModel.defZoomLevel,
                    map.dimensions().version)),
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
object drawHelperComparator satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage(true, "-t", "--test", ParamCount.atLeastOne,
        "Test drawing performance.",
        """Test the performance of the TileDrawHelper classes---which do the heavy lifting
           of rendering the map in the viewer---using a variety of automated tests.""");
    "Run the tests."
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        Boolean() random = Random().nextBoolean;
        void runTestProcedure(ICLIHelper cli, IMapNG map, Optional<Path> filename,
                Boolean() rng) {
            cli.println("Testing using ``filename.map(Path.string).
                orElse("an unsaved map")``");
            PointFactory.clearCache();
            Boolean startCaching = rng();
            PointFactory.shouldUseCache(startCaching);
            String cachingMessage(Boolean caching) {
                return (caching) then "Using cache:" else "Not using cache:";
            }
            cli.println(cachingMessage(startCaching));
            Integer reps = 50;
            runAllTests(cli, map, reps);
            PointFactory.shouldUseCache(!startCaching);
            cli.println(cachingMessage(!startCaching));
            runAllTests(cli, map, reps);
        }
        if (is IMultiMapModel model) {
            for (pair in model.allMaps) {
                runTestProcedure(cli, pair.first(), pair.second(), random);
            }
        } else {
            runTestProcedure(cli, model.map, model.mapFile, random);
        }
    }
}