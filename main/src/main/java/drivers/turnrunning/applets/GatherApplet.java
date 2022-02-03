package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;
import common.map.HasKind;
import common.map.Point;
import common.map.TileFixture;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Shrub;
import drivers.common.cli.ICLIHelper;
import drivers.resourceadding.ResourceAddingCLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import exploration.common.HuntingModel;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/* package */ class GatherApplet extends AbstractTurnApplet {
	public GatherApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		super(model, cli, idf);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
		huntingModel = new HuntingModel(model.getMap());
		resourceAddingHelper = new ResourceAddingCLIHelper(cli, idf);
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;
	private final HuntingModel huntingModel;
	private final ResourceAddingCLIHelper resourceAddingHelper;

	private static final List<String> COMMANDS = Collections.singletonList("gather");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "gather vegetation from surrounding area";
	}

	/**
	 * If argument is a meadow, its status in the format used below; otherwise the empty string.
	 */
	private static String meadowStatus(Object argument) {
		if (argument instanceof Meadow) {
			return String.format("(%s)", ((Meadow) argument).getStatus());
		} else {
			return "";
		}
	}

	private static String toHours(int minutes) {
		if (minutes < 0) {
			return "negative " + toHours(0 - minutes);
		} else if (minutes == 0) {
			return "no time";
		} else if (minutes == 1) {
			return "1 minute";
		} else if (minutes < 60) {
			return minutes + " minutes";
		} else if (minutes == 60) {
			return "1 hour";
		} else if (minutes < 120) {
			return "1 hour, " + toHours(minutes % 60);
		} else if (minutes % 60 == 0) {
			return (minutes / 60) + " hours";
		} else {
			return (minutes / 60) + " hours, " + toHours(minutes % 60);
		}
	}

	@Override
	@Nullable
	public String run() {
		StringBuilder buffer = new StringBuilder();
		final Point center = confirmPoint("Location to search around: ");
		if (center == null) {
			return ""; // TODO: null, surely?
		}
		final Integer stTemp = cli.inputNumber("Minutes to spend gathering: ");
		if (stTemp == null) {
			return ""; // TODO: null, surely?
		}
		final int startingTime = stTemp;
		int time = startingTime;
		Iterator<Pair<Point, /*Grove|Shrub|Meadow|HuntingModel.NothingFound*/TileFixture>> encounters =
			huntingModel.gather(center).iterator();
		int noResultsTime = 0;
		while (time > 0 && encounters.hasNext()) {
			Pair<Point, TileFixture> pair = encounters.next();
			Point loc = pair.getValue0();
			TileFixture find = pair.getValue1();
			if (find instanceof HuntingModel.NothingFound) {
				noResultsTime += noResultCost;
				time -= noResultCost;
			} else {
				if (noResultsTime > 0) {
					cli.println("Found nothing for the next " + toHours(noResultsTime)); // TODO: Add to results?
					noResultsTime = 0;
				}
				Boolean resp = cli.inputBooleanInSeries(String.format("Gather from %s%s",
					find.getShortDescription(), meadowStatus(find)), ((HasKind) find).getKind());
				if (resp == null) {
					return null;
				} else if (resp) {
					IUnit unit = model.getSelectedUnit();
					if (unit != null) {
						cli.println("Enter details of harvest (any empty string aborts):");
						IMutableResourcePile resource;
						while ((resource = resourceAddingHelper.enterResource()) != null) {
							if ("food".equals(resource.getKind())) {
								resource.setCreated(model.getMap().getCurrentTurn());
							}
							if (!model.addExistingResource(resource, unit.getOwner())) {
								cli.println("Failed to find a fortress to add to in any map");
							}
						}
					}
					int cost = Optional.ofNullable(cli.inputNumber("Time to gather: "))
						.orElse((int) Short.MAX_VALUE);
					time -= cost;
					// TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
					if (find instanceof Shrub && ((Shrub) find).getPopulation() > 0) {
						Boolean reduce = cli.inputBooleanInSeries("Reduce shrub population here?");
						if (reduce == null) {
							return null;
						} else if (reduce) {
							reducePopulation(loc, (Shrub) find, "plants", true);
							cli.print(inHours(time));
							cli.println("remaining.");
							continue;
						}
					}
					cli.print(inHours(time));
					cli.println(" remaining.");
				} else {
					time -= noResultCost;
				}
				model.copyToSubMaps(loc, find, true);
			}
			String addendum = cli.inputMultilineString("Add to results about that:");
			if (addendum == null) {
				return null;
			} else {
				buffer.append(addendum);
			}
		}
		if (noResultsTime > 0) {
			cli.println("Found nothing for the next " + toHours(noResultsTime)); // TODO: Add to results?
		}
		return buffer.toString().trim();
	}
}
