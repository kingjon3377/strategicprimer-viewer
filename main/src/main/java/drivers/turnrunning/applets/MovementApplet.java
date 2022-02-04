package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;
import common.map.Point;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;
import drivers.common.cli.ICLIHelper;
import drivers.exploration.ExplorationCLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.Nullable;

import static lovelace.util.Decimalize.decimalize;

/* package */ class MovementApplet extends AbstractTurnApplet {
	public MovementApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		super(model, cli, idf);
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

	private void packFood(@Nullable IFortress fortress, IUnit unit) {
		if (fortress == null) {
			return;
		}
		List<IResourcePile> resources = new ArrayList(StreamSupport.stream(fortress.spliterator(), false)
			.filter(IResourcePile.class::isInstance).map(IResourcePile.class::cast)
			.collect(Collectors.toList()));
		while (true) {
			IResourcePile chosen =
				chooseFromList(resources, "Resources in ``fortress.name``:", "No resources in fortress.",
					"Resource to take (from):", false);
			if (chosen == null) {
				break;
			}
			Boolean takeAll = cli.inputBooleanInSeries("Take it all?");
			if (takeAll == null) {
				return; // TODO: Find a way to propagate the EOF to caller
			}
			else if (takeAll) {
				model.transferResource(chosen, unit, decimalize(chosen.getQuantity().getNumber()),
					idf::createID);
				resources.remove(chosen);
			} else {
				BigDecimal amount = cli.inputDecimal(String.format("Amount to take (in %s):",
					chosen.getQuantity().getUnits()));
				if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
					model.transferResource(chosen, unit, amount, idf::createID);
					resources.clear();
					StreamSupport.stream(fortress.spliterator(), false)
						.filter(IResourcePile.class::isInstance).map(IResourcePile.class::cast)
						.forEach(r -> resources.add(r));
				}
			}
		}
	}

	@Nullable
	@Override
	public String run() {
		final StringBuilder buffer = new StringBuilder();
		model.addSelectionChangeListener(explorationCLI);
		IUnit mover = model.getSelectedUnit();
		// Ask the user about total MP, through explorationCLI listening for the selection-change event
		model.setSelectedUnit(mover);
		while (explorationCLI.getMovement() > 0) {
			Point oldPosition = model.getSelectedUnitLocation();
			explorationCLI.moveOneStep();
			Point newPosition = model.getSelectedUnitLocation();
			IFortress startingFort = model.getMap().getFixtures(oldPosition).stream()
				.filter(IFortress.class::isInstance).map(IFortress.class::cast)
				.filter(f -> f.getOwner().equals(mover.getOwner())).findAny().orElse(null);
			if (startingFort != null && !model.getMap().getFixtures(newPosition).stream()
					.filter(IFortress.class::isInstance).map(IFortress.class::cast)
					.anyMatch(f -> f.getOwner().equals(mover.getOwner()))) {
				Boolean pack = cli.inputBooleanInSeries("Leaving a fortress. Take provisions along?");
				if (pack == null) {
					return null;
				} else if (pack) {
					packFood(startingFort, mover);
				}
			}
			String addendum = cli.inputMultilineString("Add to results:");
			if (addendum == null) {
				return null;
			}
			buffer.append(addendum);
		}
		// We don't want to be asked about MP for any other applets
		model.removeSelectionChangeListener(explorationCLI);
		return buffer.toString();
	}
}
