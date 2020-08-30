import strategicprimer.drivers.common {
    DriverFactory,
    UtilityDriver,
    SPOptions,
    UtilityDriverFactory,
    DriverUsage,
    ParamCount
}
import java.awt.image {
    BufferedImage
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import java.lang {
    Types
}
import lovelace.util.jvm {
    ResourceInputStream
}
import javax.imageio {
    ImageIO
}
import lovelace.util.common {
    EnumCounter,
    PathWrapper
}
import strategicprimer.model.common.map {
    TileType,
    Point,
    IMutableMapNG,
    SPMapNG,
    MapDimensionsImpl,
    PlayerCollection,
    HasName,
    IMapNG
}
import strategicprimer.model.impl.xmlio {
    mapIOHelper
}
import ceylon.random {
    randomize
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    IDFactory
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
"A factory for an app to let the user create a map from an image."
service(`interface DriverFactory`)
shared class ImporterFactory() satisfies UtilityDriverFactory {
    shared actual DriverUsage usage = DriverUsage(false, "import",
        ParamCount.atLeastOne, "Import terrain data from a raster image",
        "Import terrain data from a raster image", false, false, "/path/to/image.png",
        "/path/to/image.png", "--size=NN");
    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
        ImporterDriver(cli, options);
}

"An app to let the user create a map from an image."
class ImporterDriver satisfies UtilityDriver {
    static class ImportableTerrain of mountain|borealForest|temperateForest
            satisfies HasName {
        shared actual String name;
        shared new mountain {
            name = "mountain";
        }

        shared new borealForest {
            name = "boreal forest";
        }

        shared new temperateForest {
            name = "temperate forest";
        }
    }
    static String pixelString(Integer pixel) =>
        "(``pixel.rightLogicalShift(16).and(#ff)``, ``pixel.rightLogicalShift(8).and(#ff)
        ``, ``pixel.and(#ff)``)";
    ICLIHelper cli;
    shared actual SPOptions options;
    shared new (ICLIHelper cli, SPOptions options) {
        this.cli = cli;
        this.options = options;
    }

    TileType|ImportableTerrain? askFor(Integer color) =>
        cli.chooseFromList(`TileType`.caseValues.chain(`ImportableTerrain`.caseValues)
                .sequence(), "Tile type represented by ``pixelString(color)``",
            "No tile types found to choose from", "Tile type:", false).item;
    []|Range<Integer> customRange(Integer base, Integer span, Integer max) {
        if (base + span > max + 1) {
            return base..(max - 1);
        } else {
            return base:span;
        }
    }
    IDRegistrar idf = IDFactory();
    String? findAdjacentForest(IMapNG map, Point location) =>
        randomize(surroundingPointIterable(location, map.dimensions, 1).flatMap(map.fixtures.get)
            .narrow<Forest>()).map(Forest.kind).first;
    shared actual void startDriver(String* args) {
        assert (is Integer size = Integer.parse(options.getArgument("--size")));
        log.debug("--size parameter is ``size``");
        for (arg in args) {
            ResourceInputStream res = ResourceInputStream(arg,
                `module strategicprimer.viewer`,
                Types.classForDeclaration(`class ImporterDriver`));
            BufferedImage image = ImageIO.read(res);
            Integer width = image.width;
            Integer height = image.height;
            log.debug("Image is ``width``x``height``");
            variable Integer baseRow = 0;
            MutableMap<Integer, TileType|ImportableTerrain> mapping =
                HashMap<Integer, TileType|ImportableTerrain>();
            variable Integer mapRow = 0;
            MutableMap<Point, TileType|ImportableTerrain> retval =
                HashMap<Point, TileType|ImportableTerrain>();
            while (baseRow < height) {
                variable Integer baseColumn = 0;
                variable Integer mapColumn = 0;
                while (baseColumn < width) {
                    EnumCounter<Integer> counter = EnumCounter<Integer>();
                    for (row in customRange(baseRow, size, height)) {
                        for (column in customRange(baseColumn, size, width)) {
                            counter.countMany(image.getRGB(row, column));
                        }
                    }
                    if (exists dominant = counter.allCounts.sort(decreasingItem).first) {
                        if (exists type = mapping[dominant.key]) {
                            log.trace("Type for (``mapRow``, ``mapColumn
                                ``) deduced to be ``type``");
                            retval[Point(mapRow, mapColumn)] = type;
                        } else if (exists type = askFor(dominant.key)) {
                            mapping[dominant.key] = type;
                            retval[Point(mapRow, mapColumn)] = type;
                        }
                    }
                    baseColumn += size;
                    mapColumn++;
                }
                baseRow += size;
                mapRow++;
            }
            IMutableMapNG finalRetval = SPMapNG(MapDimensionsImpl(
                    (retval.keys.map(Point.row).max(increasing) else 0) + 1,
                    (retval.keys.map(Point.column).max(increasing) else 0) + 1, 2),
                PlayerCollection(), -1);
            for (point->type in retval) {
                log.trace("Setting ``point`` to ``type``");
                switch (type)
                case (is TileType) {
                    finalRetval.baseTerrain[point] = type;
                }
                case (ImportableTerrain.mountain) {
                    finalRetval.baseTerrain[point] = TileType.plains;
                    finalRetval.mountainous[point] = true;
                }
                case (ImportableTerrain.temperateForest) {
                    finalRetval.baseTerrain[point] = TileType.plains;
                    if (exists forest = findAdjacentForest(finalRetval, point)) {
                        finalRetval.addFixture(point, Forest(forest, false,
                            idf.createID()));
                    } else if (exists forest =
                            cli.inputString("Kind of tree for a temperate forest: ")) {
                        finalRetval.addFixture(point,
                            Forest(forest, false, idf.createID()));
                    } else {
                        return;
                    }
                }
                case (ImportableTerrain.borealForest) {
                    finalRetval.baseTerrain[point] = TileType.steppe;
                    if (exists forest = findAdjacentForest(finalRetval, point)) {
                        finalRetval.addFixture(point, Forest(forest, false,
                            idf.createID()));
                    } else if (exists forest =
                        cli.inputString("Kind of tree for a boreal forest: ")) {
                        finalRetval.addFixture(point,
                            Forest(forest, false, idf.createID()));
                    } else {
                        return;
                    }
                }
            }
            mapIOHelper.writeMap(PathWrapper(arg + ".xml"), finalRetval);
        }
    }
}
