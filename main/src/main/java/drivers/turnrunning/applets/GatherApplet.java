package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;
import legacy.map.HasKind;
import legacy.map.IFixture;
import legacy.map.Point;
import legacy.map.TileFixture;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Shrub;
import drivers.common.cli.ICLIHelper;
import drivers.resourceadding.ResourceAddingCLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import exploration.common.HuntingModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

/* package */ final class GatherApplet extends AbstractTurnApplet {
	public GatherApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		huntingModel = new HuntingModel(model.getMap());
		resourceAddingHelper = new ResourceAddingCLIHelper(cli, idf);
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;
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
	private static String meadowStatus(final Object argument) {
		if (argument instanceof final Meadow m) {
			return "(%s)".formatted(m.getStatus());
		} else {
			return "";
		}
	}

	@Override
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		final Point center = confirmPoint("Location to search around: ");
		if (Objects.isNull(center)) {
			return null;
		}
		final Integer startingTime = cli.inputNumber("Minutes to spend gathering: ");
		if (Objects.isNull(startingTime)) {
			return null;
		}
		int time = startingTime;
		final Supplier<Pair<Point, /*Grove|Shrub|Meadow|HuntingModel.NothingFound*/TileFixture>> encounters =
				huntingModel.gather(center);
		int noResultsTime = 0;
		while (time > 0) {
			final Pair<Point, TileFixture> pair = encounters.get();
			final Point loc = pair.getValue0();
			final TileFixture find = pair.getValue1();
			if (find instanceof HuntingModel.NothingFound) {
				noResultsTime += NO_RESULT_COST;
				time -= NO_RESULT_COST;
			} else {
				if (noResultsTime > 0) {
					// TODO: Add to results?
					cli.print("Found nothing for the next");
					cli.println(inHours(noResultsTime));
					noResultsTime = 0;
				}
				switch (cli.inputBooleanInSeries("Gather from %s%s".formatted(
						find.getShortDescription(), meadowStatus(find)), ((HasKind) find).getKind())) {
					case YES -> {
						final IUnit unit = model.getSelectedUnit();
						if (Objects.nonNull(unit)) {
							cli.println("Enter details of harvest (any empty string aborts):");
							while (true) {
								final IMutableResourcePile resource = resourceAddingHelper.enterResource();
								if (Objects.isNull(resource)) {
									break;
								} else if ("food".equals(resource.getKind())) {
									resource.setCreated(model.getMap().getCurrentTurn());
								}
								if (!model.addExistingResource(resource, unit.owner())) {
									cli.println("Failed to find a fortress to add to in any map");
								}
							}
						}
						final int cost = Optional.ofNullable(cli.inputNumber("Time to gather: "))
								.orElse((int) Short.MAX_VALUE);
						time -= cost;
						// TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
						if (find instanceof final Shrub s && s.getPopulation() > 0) {
							switch (cli.inputBooleanInSeries("Reduce shrub population here?")) {
								case YES -> {
									reducePopulation(loc, (Shrub) find, "plants", IFixture.CopyBehavior.ZERO);
									cli.print(inHours(time));
									cli.println("remaining.");
									continue;
								}
								case NO -> { // Do nothing
								}
								case QUIT -> {
									return buffer.toString().strip();
								}
								case EOF -> {
									return null;
								}
							}
						}
						cli.print(inHours(time));
						cli.println(" remaining.");
					}
					case NO -> time -= NO_RESULT_COST;
					case QUIT -> {
						return buffer.toString().strip();
					}
					case EOF -> {
						return null;
					}
				}
				model.copyToSubMaps(loc, find, IFixture.CopyBehavior.ZERO);
			}
			final String addendum = cli.inputMultilineString("Add to results about that:");
			if (Objects.isNull(addendum)) {
				return null;
			} else {
				buffer.append(addendum);
			}
		}
		if (noResultsTime > 0) {
			// TODO: Add to results?
			cli.print("Found nothing for the next ");
			cli.println(inHours(noResultsTime));
		}
		return buffer.toString().strip();
	}
}
