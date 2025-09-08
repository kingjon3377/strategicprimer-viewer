package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;
import legacy.map.HasOwner;
import legacy.map.Point;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.towns.IFortress;
import drivers.common.cli.ICLIHelper;
import drivers.exploration.ExplorationCLIHelper;
import drivers.turnrunning.ITurnRunningModel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import static lovelace.util.Decimalize.decimalize;

/* package */ final class MovementApplet extends AbstractTurnApplet {
	public MovementApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
		explorationCLI = new ExplorationCLIHelper(model, cli);
		model.addMovementCostListener(explorationCLI);
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;
	private final ExplorationCLIHelper explorationCLI;

	private static final List<String> COMMANDS = Collections.singletonList("move");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "move a unit";
	}

	private void packFood(final @Nullable IFortress fortress, final IUnit unit) {
		if (Objects.isNull(fortress)) {
			return;
		}
		final List<IResourcePile> resources = fortress.stream()
				.filter(IResourcePile.class::isInstance).map(IResourcePile.class::cast).collect(Collectors.toList());
		final IntSupplier createID = idf::createID;
		final Predicate<Object> isResource = IResourcePile.class::isInstance;
		final Function<Object, IResourcePile> resourceCast = IResourcePile.class::cast;
		final Consumer<IResourcePile> addResource = resources::add;
		while (true) {
			final IResourcePile chosen =
					chooseFromList(resources, "Resources in %s:".formatted(fortress.getName()),
							"No resources in fortress.", "Resource to take (from):",
							ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			if (Objects.isNull(chosen)) {
				break;
			}
			switch (cli.inputBooleanInSeries("Take it all?")) {
				case YES -> {
					model.transferResource(chosen, unit, decimalize(chosen.getQuantity().number()),
							createID);
					resources.remove(chosen);
				}
				case NO -> {
					final BigDecimal amount = cli.inputDecimal("Amount to take (in %s):"
							.formatted(chosen.getQuantity().units()));
					if (Objects.nonNull(amount) && amount.signum() > 0) {
						model.transferResource(chosen, unit, amount, createID);
						resources.clear();
						fortress.stream().filter(isResource).map(resourceCast)
								.forEach(addResource);
					}
				}
				case QUIT -> {
					return;
				}
				case EOF -> {
					return; // TODO: Find a way to propagate the EOF to caller
				}
			}
		}
	}

	@Override
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		model.addSelectionChangeListener(explorationCLI);
		final IUnit mover = model.getSelectedUnit();
		if (Objects.isNull(mover)) {
			cli.println("No currently selected unit");
			return "";
		}
		// Ask the user about total MP, through explorationCLI listening for the selection-change event
		model.setSelectedUnit(mover);
		final Predicate<Object> isFortress = IFortress.class::isInstance;
		final Function<Object, IFortress> fortressCast = IFortress.class::cast;
		final Predicate<HasOwner> sameOwner = f -> f.owner().equals(mover.owner());
		while (explorationCLI.getMovement() > 0) {
			final Point oldPosition = model.getSelectedUnitLocation();
			explorationCLI.moveOneStep();
			final Point newPosition = model.getSelectedUnitLocation();
			final IFortress startingFort = model.getMap().streamFixtures(oldPosition)
					.filter(isFortress).map(fortressCast)
					.filter(sameOwner).findAny().orElse(null);
			if (Objects.nonNull(startingFort) && model.getMap().streamFixtures(newPosition)
					.filter(isFortress).map(fortressCast)
					.noneMatch(sameOwner)) {
				switch (cli.inputBooleanInSeries("Leaving a fortress. Take provisions along?")) {
					case YES -> packFood(startingFort, mover);
					case NO -> { // Do nothing
					}
					case QUIT -> {
						// We don't want to be asked about MP for any other applets
						model.removeSelectionChangeListener(explorationCLI);
						return buffer.toString();
					}
					case EOF -> {
						// We don't want to be asked about MP for any other applets
						model.removeSelectionChangeListener(explorationCLI);
						return null;
					}
				}
			}
			final String addendum = cli.inputMultilineString("Add to results:");
			if (Objects.isNull(addendum)) {
				return null;
			}
			buffer.append(addendum);
		}
		// We don't want to be asked about MP for any other applets
		model.removeSelectionChangeListener(explorationCLI);
		return buffer.toString();
	}
}
