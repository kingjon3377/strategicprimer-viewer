package utility;

import common.map.fixtures.mobile.IMutableUnit;
import drivers.common.DriverFailedException;

import java.io.IOException;

import drivers.exploration.old.MissingTableException;

import java.util.Set;

import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.CommunityStats;

import java.util.Random;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import drivers.common.cli.ICLIHelper;

import drivers.common.CLIDriver;
import drivers.common.EmptyOptions;
import drivers.common.SPOptions;

import common.map.TileType;
import common.map.Point;
import common.map.IMapNG;

import common.map.fixtures.terrain.Forest;

import common.map.fixtures.IResourcePile;
import common.map.fixtures.ResourcePileImpl;

import drivers.exploration.old.ExplorationRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import org.javatuples.Pair;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

/**
 * A hackish driver to fix TODOs (missing content) in the map, namely units
 * with "TODO" for their "kind" and aquatic villages with non-aquatic races.
 *
 * TODO: Write tests of this functionality
 *
 * FIXME: Move mutation operations into a driver model
 */
public class TodoFixerCLI implements CLIDriver {
    public TodoFixerCLI(final ICLIHelper cli, final UtilityDriverModel model) {
        this.cli = cli;
        this.model = model;
    }

    private final ICLIHelper cli;

    private final UtilityDriverModel model;

    @Override
    public UtilityDriverModel getModel() {
        return model;
    }

    @Override
    public SPOptions getOptions() {
        return EmptyOptions.EMPTY_OPTIONS;
    }

    /**
     * A list of unit kinds (jobs) for plains etc.
     */
    private final List<String> plainsList = new ArrayList<>();

    /**
     * A list of unit kinds (jobs) for forest and jungle.
     */
    private final List<String> forestList = new ArrayList<>();

    /**
     * A list of unit kinds (jobs) for ocean.
     */
    private final List<String> oceanList = new ArrayList<>();

    /**
     * A map from village IDs to races.
     */
    private final Map<Integer, String> raceMap = new HashMap<>();

    /**
     * A list of aqautic races.
     */
    private final List<String> raceList = new ArrayList<>();

    /**
     * How many units we've fixed.
     */
    private int count = -1;

    /**
     * The number of units needing to be fixed.
     */
    private long totalCount = -1L;

    /**
     * Get the simplified-terrain-model instance covering the map's terrain at the given location.
     *
     * We don't just use TileType because we need mountains and forests in ver-2 maps.
     */
    private static SimpleTerrain getTerrain(final IMapNG map, final Point location) {
        final TileType terrain = map.getBaseTerrain(location);
        if (terrain == null) {
            return SimpleTerrain.Unforested;
        }
        switch (terrain) {
            case Jungle:
            case Swamp:
                return SimpleTerrain.Forested;
            case Desert:
            case Tundra:
                return SimpleTerrain.Unforested;
            case Ocean:
                return SimpleTerrain.Ocean;
            case Plains:
            case Steppe:
                if (map.isMountainous(location)) {
                    return SimpleTerrain.Unforested;
                } else if (map.getFixtures(location).stream().anyMatch(Forest.class::isInstance)) {
                    return SimpleTerrain.Forested;
                } else {
                    return SimpleTerrain.Unforested;
                }
            default:
                throw new IllegalStateException("Exhaustive switch isn't");
        }
    }

    private @Nullable ExplorationRunner _runner = null;

    private ExplorationRunner getRunner() throws IOException {
        if (_runner == null) {
            final ExplorationRunner retval = new ExplorationRunner();
            final Path directory = Paths.get("tables");
            if (!Files.isDirectory(directory)) {
                throw new IllegalStateException("TODO fixer requires a tables directory");
            }
            retval.loadAllTables(directory);
            _runner = retval;
            return retval;
        } else {
            return _runner;
        }
    }

    private static String simpleTerrain(final IMapNG map, final Point loc) {
        final TileType terrain = map.getBaseTerrain(loc);
        if (TileType.Ocean == terrain) {
            return "ocean";
        } else if (map.isMountainous(loc)) {
            return "mountain";
        } else if (map.getFixtures(loc).stream().noneMatch(Forest.class::isInstance)) {
            return "plains";
        } else {
            return "forest";
        }
    }

    private static boolean productionContainsHash(final Pair<Point, CommunityStats> pair) {
        return pair.getValue1().getYearlyProduction().stream().map(IResourcePile::getContents)
                .anyMatch(s -> s.contains("#"));
    }

    // TODO: Check against the Ceylon code to see if this is right.
    private static boolean anyEmptySkills(final Pair<Point, CommunityStats> pair) {
        return pair.getValue1().getHighestSkillLevels().keySet().stream()
                .anyMatch(String::isEmpty);
    }

    /**
     * Search for and fix aquatic villages with non-aquatic races.
     */
    private void fixAllVillages(final IMapNG map) throws MissingTableException, IOException {
        final List<Village> villages = map.streamLocations()
                .filter(l -> TileType.Ocean == map.getBaseTerrain(l))
                .flatMap(l -> map.getFixtures(l).stream())
                .filter(Village.class::isInstance).map(Village.class::cast)
                .filter(v -> LandRaces.LAND_RACES.contains(v.getRace())).toList();
        if (!villages.isEmpty()) {
            if (raceList.isEmpty()) {
                while (true) {
                    final String race = cli.inputString("Next aquatic rae: ");
                    if (race == null) {
                        return;
                    } else if (race.isEmpty()) {
                        break;
                    } else {
                        raceList.add(race);
                    }
                }
            }
            for (final Village village : villages) {
                if (raceMap.containsKey(village.getId())) {
                    village.setRace(raceMap.get(village.getId()));
                } else {
                    final Random rng = new Random(village.getId());
                    final String race = raceList.get(rng.nextInt(raceList.size()));
                    village.setRace(race);
                    raceMap.put(village.getId(), race);
                }
            }
        }

        final List<Pair<Point, CommunityStats>> brokenTownContents =
                map.streamLocations()
                        .flatMap(l -> map.getFixtures(l).stream()
                                .filter(ITownFixture.class::isInstance)
                                .map(ITownFixture.class::cast)
                                .map(ITownFixture::getPopulation)
                                .filter(Objects::nonNull)
                                .map(s -> Pair.with(l, s)))
                        .filter(TodoFixerCLI::productionContainsHash).toList();

        if (!brokenTownContents.isEmpty()) {
            final ExplorationRunner eRunner = getRunner();
            for (final Pair<Point, CommunityStats> pair : brokenTownContents) {
                final Point loc = pair.getValue0();
                final CommunityStats population = pair.getValue1();
                final Set<IResourcePile> production = population.getYearlyProduction();
                for (final IResourcePile resource : production.stream()
                        .filter(r -> r.getContents().contains("#")).toList()) {
                    final String table = resource.getContents().split("#")[1];
                    final IResourcePile replacement = new ResourcePileImpl(resource.getId(),
                            resource.getKind(), eRunner.recursiveConsultTable(table, loc,
                            map.getBaseTerrain(loc), map.isMountainous(loc),
                            map.getFixtures(loc), map.getDimensions()),
                            resource.getQuantity());
                    production.remove(resource);
                    production.add(replacement);
                }
            }
        }

        final List<Pair<Point, CommunityStats>> brokenExpertise =
                map.streamLocations()
                        .flatMap(l -> map.getFixtures(l).stream()
                                .filter(ITownFixture.class::isInstance)
                                .map(ITownFixture.class::cast)
                                .map(ITownFixture::getPopulation)
                                .filter(Objects::nonNull)
                                .map(s -> Pair.with(l, s)))
                        .filter(TodoFixerCLI::anyEmptySkills).toList();
        if (!brokenExpertise.isEmpty()) {
            final ExplorationRunner eRunner = getRunner();
            for (final Pair<Point, CommunityStats> pair : brokenExpertise) {
                final Point loc = pair.getValue0();
                final CommunityStats population = pair.getValue1();
                final int level = population.getHighestSkillLevels().get("");
                population.setSkillLevel("", 0);
                String newSkill = eRunner.recursiveConsultTable(
                        simpleTerrain(map, loc) + "_skills", loc, map.getBaseTerrain(loc),
                        map.isMountainous(loc), map.getFixtures(loc), map.getDimensions());
                if (population.getHighestSkillLevels().containsKey(newSkill) &&
                        population.getHighestSkillLevels().get(newSkill) >= level) {
                    final int existingLevel = population.getHighestSkillLevels().get(newSkill);
                    newSkill = eRunner.recursiveConsultTable("regional_specialty", loc,
                            map.getBaseTerrain(loc), map.isMountainous(loc),
                            map.getFixtures(loc), map.getDimensions());
                }
                if (population.getHighestSkillLevels().containsKey(newSkill) &&
                        population.getHighestSkillLevels().get(newSkill) >= level) {
                    continue;
                }
                population.setSkillLevel(newSkill, level);
            }
        }
    }

    /**
     * Fix a stubbed-out kind for a unit.
     */
    private void fixUnit(final IMutableUnit unit, final SimpleTerrain terrain) {
        final Random rng = new Random(unit.getId());
        count++;
        final List<String> jobList;
        final String description;
        switch (terrain) {
            case Unforested -> {
                jobList = plainsList;
                description = "plains, desert, or mountains";
            }
            case Forested -> {
                jobList = forestList;
                description = "forest or jungle";
            }
            case Ocean -> {
                jobList = oceanList;
                description = "ocean";
            }
            default -> throw new IllegalStateException("Exhaustive switch wasn't");
        }
        for (final String job : jobList) {
            if (rng.nextBoolean()) {
                cli.println(String.format("Setting unit with ID #%d (%d/%d) to kind %s",
                        unit.getId(), count, totalCount, job));
                unit.setKind(job);
                return;
            }
        }
        final String kind = cli.inputString(String.format("What's the next possible kind for %s? ",
                description));
        if (kind != null) {
            unit.setKind(kind);
            jobList.add(kind);
        }
    }

    /**
     * Search for and fix units with kinds missing.
     */
    private void fixAllUnits(final IMapNG map) {
        totalCount = map.streamAllFixtures()
                .filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
                .filter(u -> "TODO".equals(u.getKind())).count();
        final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
        final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
        for (final Point point : map.getLocations()) {
            final SimpleTerrain terrain = getTerrain(map, point);
            map.getFixtures(point).stream().filter(isUnit)
                    .map(unitCast).filter(u -> "TODO".equals(u.getKind()))
                    .forEach(fixture -> fixUnit(fixture, terrain));
        }
    }

    @Override
    public void startDriver() throws DriverFailedException {
        if (model.getSubordinateMaps().iterator().hasNext()) {
            for (final IMapNG map : model.getAllMaps()) {
                fixAllUnits(map);
                try {
                    fixAllVillages(map);
                } catch (final IOException except) {
                    throw new DriverFailedException(except,
                            "I/O error loading data from disk");
                } catch (final MissingTableException except) {
                    throw new DriverFailedException(except,
                            "Missing data file");
                }
                model.setMapModified(map, true);
            }
            model.getMap().getLocations().forEach(model::copyRiversAt);
        } else {
            fixAllUnits(model.getMap());
            try {
                fixAllVillages(model.getMap());
            } catch (final IOException except) {
                throw new DriverFailedException(except,
                        "I/O error loading data from disk");
            } catch (final MissingTableException except) {
                throw new DriverFailedException(except,
                        "Missing data file");
            }
            model.setMapModified(true);
        }
    }
}
