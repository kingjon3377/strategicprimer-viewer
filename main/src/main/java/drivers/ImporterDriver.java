package drivers;

import drivers.common.DriverFailedException;
import drivers.common.UtilityDriver;
import drivers.common.SPOptions;

import java.awt.image.BufferedImage;

import java.nio.file.NoSuchFileException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import drivers.common.cli.ICLIHelper;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;

import legacy.map.LegacyPlayerCollection;
import lovelace.util.LovelaceLogger;
import lovelace.util.Range;
import lovelace.util.ResourceInputStream;

import javax.imageio.ImageIO;

import lovelace.util.EnumCounter;

import legacy.map.TileType;
import legacy.map.Point;
import legacy.map.IMutableLegacyMap;
import legacy.map.LegacyMap;
import legacy.map.MapDimensionsImpl;
import common.map.HasName;
import legacy.map.ILegacyMap;

import legacy.xmlio.MapIOHelper;

import legacy.idreg.IDRegistrar;
import legacy.idreg.IDFactory;

import legacy.map.fixtures.terrain.Forest;

import exploration.common.SurroundingPointIterable;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * An app to let the user create a map from an image.
 */
/* package */ class ImporterDriver implements UtilityDriver {
    private enum ImportableTerrain implements HasName {
        Mountain("mountain"),
        BorealForest("boreal forest"),
        TemperateForest("temperate forest");

        ImportableTerrain(final String name) {
            this.name = name;
        }

        private final String name;

        @Override
        public String getName() {
            return name;
        }
    }

    private static String pixelString(final int pixel) {
        return String.format("(%d, %d, %d)", (pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, pixel & 0xFF);
    }

    private final ICLIHelper cli;
    private final SPOptions options;

    private final List<HasName> terrains;

    @Override
    public SPOptions getOptions() {
        return options;
    }

    public ImporterDriver(final ICLIHelper cli, final SPOptions options) {
        this.cli = cli;
        this.options = options;
        terrains = Stream.concat(Stream.of(TileType.values()),
                Stream.of(ImportableTerrain.values())).map(HasName.class::cast).toList();
    }

    private @Nullable /*TileType|ImportableTerrain?*/ HasName askFor(final int color) {
        return cli.chooseFromList((List<? extends HasName>) terrains, "Tile type represented by " + pixelString(color), "No tile types found to choose from", "Tile type:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT).getValue1();
    }

    private static Range customRange(final int base, final int span, final int max) {
        if (base + span > max + 1) {
            return new Range(base, max - 1);
        } else {
            return new Range(base, base + span - 1); // TODO: Implement Ceylon's ranges more fully so we can use a class where we would use the 'base:span' format in Ceylon
        }
    }

    private final IDRegistrar idf = new IDFactory();

    private static @Nullable String findAdjacentForest(final ILegacyMap map, final Point location) {
        final List<Forest> forests =
                new SurroundingPointIterable(location, map.getDimensions(), 1).stream()
                        .flatMap(l -> map.getFixtures(l).stream())
                        .filter(Forest.class::isInstance).map(Forest.class::cast)
                        .collect(Collectors.toList());
        Collections.shuffle(forests);
        return forests.isEmpty() ? null : forests.getFirst().getKind();
    }

    @Override
    public void startDriver(final String... args) throws DriverFailedException {
        final int size;
        try {
            size = Integer.parseInt(options.getArgument("--size"));
        } catch (final NumberFormatException except) {
            throw new DriverFailedException(except, "--size argument must be numeric");
        }
        LovelaceLogger.debug("--size parameter is %s", size);
        for (final String arg : args) {
            final ResourceInputStream res; // FIXME: Probably never closed
            try {
                res = new ResourceInputStream(arg, ImporterDriver.class);
            } catch (final NoSuchFileException except) {
                throw new DriverFailedException(except, "Image file not found");
            }
            final BufferedImage image;
            try {
                image = ImageIO.read(res);
            } catch (final IOException except) {
                throw new DriverFailedException(except, "I/O error reading image");
            }
            final int width = image.getWidth();
            final int height = image.getHeight();
            LovelaceLogger.debug("Image is %dx%d", width, height);
            int baseRow = 0;
            final Map<Integer, /*TileType|ImportableTerrain*/HasName> mapping =
                    new HashMap<>();
            int mapRow = 0;
            final Map<Point, /*TileType|ImportableTerrain*/HasName> retval =
                    new HashMap<>();
            while (baseRow < height) {
                int baseColumn = 0;
                int mapColumn = 0;
                while (baseColumn < width) {
                    final EnumCounter<Integer> counter = new EnumCounter<>();
                    for (final int row : customRange(baseRow, size, height)) {
                        for (final int column : customRange(baseColumn, size, width)) {
                            counter.countMany(image.getRGB(row, column));
                        }
                    }

                    final Pair<Integer, Integer> dominant = counter.streamAllCounts()
                            .max(Comparator.comparing(Pair::getValue1)).orElse(null);
                    if (dominant != null) {
                        if (mapping.containsKey(dominant.getValue0())) {
                            final HasName type = mapping.get(dominant.getValue0());
                            LovelaceLogger.debug("Type for (%d, %d) deduced to be %s",
                                    mapRow, mapColumn, type);
                            retval.put(new Point(mapRow, mapColumn), type);
                        } else {
                            cli.print("In (", Integer.toString(mapRow),
                                    ", ", Integer.toString(mapColumn),
                                    "): ");
                            final HasName type = askFor(dominant.getValue0());
                            if (type != null) {
                                mapping.put(dominant.getValue0(), type);
                                retval.put(new Point(mapRow, mapColumn), type);
                            }
                        }
                    }
                    baseColumn += size;
                    mapColumn++;
                }
                baseRow += size;
                mapRow++;
            }
            final IMutableLegacyMap finalRetval = new LegacyMap(new MapDimensionsImpl(
                    retval.keySet().stream().mapToInt(Point::row).max().orElse(0) + 1,
                    retval.keySet().stream().mapToInt(Point::column).max().orElse(0) + 1, 2),
                    new LegacyPlayerCollection(), -1);
            for (final Map.Entry<Point, HasName> entry : retval.entrySet()) {
                final Point point = entry.getKey();
                final HasName type = entry.getValue();
                LovelaceLogger.trace("Setting %s to %s", point, type);
                if (type instanceof final TileType tt) {
                    finalRetval.setBaseTerrain(point, tt);
                } else {
                    final ImportableTerrain terr = (ImportableTerrain) type;
                    switch (terr) {
                        case Mountain -> {
                            finalRetval.setBaseTerrain(point, TileType.Plains);
                            finalRetval.setMountainous(point, true);
                        }
                        case TemperateForest -> {
                            finalRetval.setBaseTerrain(point, TileType.Plains);
                            final String foundTForest = findAdjacentForest(finalRetval, point);
                            if (foundTForest != null) {
                                finalRetval.addFixture(point, new Forest(foundTForest, false,
                                        idf.createID()));
                                continue;
                            }
                            final String inputTForest = cli.inputString("Kind of tree for a temperate forest: ");
                            if (inputTForest == null) {
                                return;
                            } else {
                                finalRetval.addFixture(point, new Forest(inputTForest, false,
                                        idf.createID()));
                            }
                        }
                        case BorealForest -> {
                            finalRetval.setBaseTerrain(point, TileType.Steppe);
                            final String foundBForest = findAdjacentForest(finalRetval, point);
                            if (foundBForest != null) {
                                finalRetval.addFixture(point, new Forest(foundBForest, false,
                                        idf.createID()));
                                continue;
                            }
                            final String inputBForest = cli.inputString("Kind of tree for a boreal forest: ");
                            if (inputBForest == null) {
                                return;
                            } else {
                                finalRetval.addFixture(point, new Forest(inputBForest, false, idf.createID()));
                            }
                        }
                        default -> throw new IllegalStateException("Exhaustive switch wasn't");
                    }
                }
                try {
                    MapIOHelper.writeMap(Paths.get(arg + ".xml"), finalRetval);
                } catch (final IOException except) {
                    throw new DriverFailedException(except, "I/O error while writing map");
                } catch (final XMLStreamException except) {
                    throw new DriverFailedException(except, "Map writer produced invalid XML");
                }
            }
        }
    }
}
