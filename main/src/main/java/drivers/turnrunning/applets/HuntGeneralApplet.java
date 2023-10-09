package drivers.turnrunning.applets;

import common.map.IFixture;
import common.map.Player;
import common.map.Point;

import common.map.TileFixture;
import common.map.fixtures.IMutableResourcePile;
import exploration.common.HuntingModel;

import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.AnimalPlurals;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import drivers.resourceadding.ResourceAddingCLIHelper;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

// TODO: Once we're on Java 17, make "sealed", limited to HuntingApplet, FishingApplet, and TrappingApplet
/* package */ abstract class HuntGeneralApplet extends AbstractTurnApplet {
    protected final HuntingModel huntingModel;
    protected final ResourceAddingCLIHelper resourceAddingHelper;
    protected final ITurnRunningModel model;
    protected final ICLIHelper cli;
    protected final IDRegistrar idf;
    protected final String verb;

    protected HuntGeneralApplet(final String verb, final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
        super(model, cli);
        huntingModel = new HuntingModel(model.getMap());
        resourceAddingHelper = new ResourceAddingCLIHelper(cli, idf);
        this.model = model;
        this.cli = cli;
        this.idf = idf;
        this.verb = verb;
    }

    /**
     * A description of what could be a single animal or a population of animals.
     */
    private static String populationDescription(final Animal animal) {
        if (animal.getPopulation() > 1) {
            return String.format("a group of perhaps %d %s", animal.getPopulation(),
                    AnimalPlurals.get(animal.getKind()));
        } else {
            return animal.getKind();
        }
    }

    private static String describeUnit(final IUnit unit) {
        return String.format("%s (%s)", unit.getName(), unit.getKind());
    }

    protected @Nullable Boolean handleCapture(final Animal find) {
        final IUnit unit = chooseFromList(model.getUnits(Optional.ofNullable(model.getSelectedUnit())
                        .map(IUnit::owner).orElse(model.getMap().getCurrentPlayer())),
                "Available units:", "No units", "Unit to add animals to:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT, HuntGeneralApplet::describeUnit);
        if (unit == null) {
            return false;
        } else {
            final Integer num = cli.inputNumber("Number captured:");
            if (num == null) {
                return null;
            } else {
                return model.addAnimal(unit, find.getKind(), "wild", idf.createID(), num);
            }
        }
    }

    protected @Nullable Integer processMeat() {
        int cost = 0;
        // TODO: somehow handle processing-in-parallel case
        final Integer iterations = cli.inputNumber("How many carcasses?");
        if (iterations == null) {
            return null;
        }
        for (int i = 0; i < iterations; i++) {
            final Integer mass = cli.inputNumber("Weight of this animal's meat in pounds: ");
            if (mass == null) {
                return null;
            }
            final Integer hands = cli.inputNumber("# of workers processing this carcass: ");
            if (hands == null) {
                return null;
            }
            cost += (int) Math.round(HuntingModel.processingTime(mass) / hands);
        }
        return cost;
    }

    protected void resourceEntry(final Player owner) {
        cli.println("Enter resources produced (any empty string aborts):");
        while (true) {
            final IMutableResourcePile resource = resourceAddingHelper.enterResource();
            if (resource == null) {
                break;
            }
            if ("food".equals(resource.getKind())) {
                resource.setCreated(model.getMap().getCurrentTurn());
            }
            if (!model.addExistingResource(resource, owner)) {
                cli.println("Failed to find a fortress to add to in any map");
            }
        }
    }

    private @Nullable Integer handleFight(final Point loc, final Animal find, final int time) {
        int cost;
        final Integer temp = cli.inputNumber(String.format("Time to %s: ", verb));
        if (temp == null) {
            return null;
        } else {
            cost = temp;
        }
        final Boolean capture = cli.inputBooleanInSeries("Capture any animals?");
        if (capture == null) {
            return null;
        } else if (capture && handleCapture(find) == null) {
            return null;
        }
        final Boolean processNow = cli.inputBooleanInSeries("Process carcasses now?");
        if (processNow == null) {
            return null;
        } else if (processNow) {
            final Integer processingTime = processMeat();
            if (processingTime == null) {
                return null;
            }
            cost += processingTime;
        }
        final Boolean reduce = cli.inputBooleanInSeries(String.format(
                "Reduce animal group population of %d?", find.getPopulation()));
        if (reduce == null) {
            return null;
        } else if (reduce) {
            reducePopulation(loc, find, "animals", IFixture.CopyBehavior.ZERO);
        } else {
            model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
        }
        if (model.getSelectedUnit() != null) {
            resourceEntry(model.getSelectedUnit().owner());
        }
        return cost;
    }

    private @Nullable Integer handleEncounter(final StringBuilder buffer, final int time, final Point loc,
            /*Animal|AnimalTracks|HuntingModel.NothingFound*/ final TileFixture find) {
        switch (find) {
            case HuntingModel.NothingFound nothingFound -> {
                cli.println(String.format("Found nothing for the next %d minutes.", noResultCost));
                return noResultCost;
            }
            case AnimalTracks at -> {
                model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
                cli.println(String.format("Found only tracks or traces from %s for the next %d minutes.",
                        at.getKind(), noResultCost));
                return noResultCost;
            }
            case Animal a -> {
                final Boolean fight = cli.inputBooleanInSeries(String.format("Found %s. Should they %s?",
                        populationDescription(a), verb), a.getKind());
                if (fight == null) {
                    return null;
                } else if (fight) {
                    return handleFight(loc, (Animal) find, time);
                } else {
                    model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
                    return noResultCost;
                }
            }
            default -> {
                LovelaceLogger.error("Unhandled case from hunting model");
                return null;
            }
        }
    }

    // TODO: Distinguish hunting from fishing in no-result time cost (encounters / hour)?
    // Note that the intended return type of encounterSrc::apply is Pair<Point, Animal|AnimalTracks|NothingFound>,
    // but Java doesn't offer union types.
    protected @Nullable String impl(final String command, final Function<Point, Iterable<Pair<Point, TileFixture>>> encounterSrc) {
        final StringBuilder buffer = new StringBuilder();
        final Point center = confirmPoint("Location to search around: ");
        if (center == null) {
            return ""; // TODO: return null, surely?
        }
        final Integer startingTime = cli.inputNumber(String.format("Minutes to spend %sing: ", command));
        if (startingTime == null) {
            return ""; // TODO: return null, surely?
        }
        int time = startingTime;
        int noResultsTime = 0;
        final Iterator<Pair<Point, TileFixture>> encounters = encounterSrc.apply(center).iterator();
        while (time > 0 && encounters.hasNext()) {
            final Pair<Point, TileFixture> pair = encounters.next();
            final Point loc = pair.getValue0();
            final TileFixture find = pair.getValue1();
            if (find instanceof HuntingModel.NothingFound) {
                noResultsTime += noResultCost;
                time -= noResultCost;
                if (time <= 0) {
                    cli.print("Found nothing for the next ");
                    cli.println(inHours(noResultsTime));
                }
                continue;
            } else if (noResultsTime > 0) {
                cli.print("Found nothing for the next ");
                cli.println(inHours(noResultsTime));
                final String addendum = cli.inputMultilineString("Add to results about that:");
                if (addendum == null) {
                    return null;
                }
                buffer.append(addendum);
                noResultsTime = 0;
            }
            cli.print(inHours(time));
            cli.println(" remaining.");
            final Integer cost = handleEncounter(buffer, time, loc, find);
            if (cost == null) {
                return null;
            }
            time -= cost;
            final String addendum = cli.inputMultilineString("Add to results about that:");
            if (addendum == null) {
                return null;
            }
            buffer.append(addendum);
        }
        return buffer.toString().strip();
    }
}
