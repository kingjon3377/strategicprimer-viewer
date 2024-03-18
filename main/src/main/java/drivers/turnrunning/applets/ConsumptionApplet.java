package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;

import legacy.map.fixtures.IResourcePile;

import java.math.BigDecimal;

import static lovelace.util.Decimalize.decimalize;

import drivers.turnrunning.ITurnRunningModel;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * We <em>deliberately</em> do not make a factory annotated for service discovery.
 */
public class ConsumptionApplet extends AbstractTurnApplet {
	private final ITurnRunningModel model;
	private final ICLIHelper cli;

	public ConsumptionApplet(final ITurnRunningModel model, final ICLIHelper cli) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		turn = model.getMap().getCurrentTurn();
		unit = model.getSelectedUnit();
	}

	private int turn;

	public int getTurn() {
		return turn;
	}

	public void setTurn(final int turn) {
		this.turn = turn;
	}

	private @Nullable IUnit unit;

	public @Nullable IUnit getUnit() {
		return unit;
	}

	public void setUnit(final @Nullable IUnit unit) {
		this.unit = unit;
	}

	private static final List<String> COMMANDS = Collections.singletonList("consumption");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "Determine the food consumed by a unit.";
	}

	private static String describeFood(final IResourcePile food) {
		if (food.getCreated() < 0) {
			return "%.2f %s of %s".formatted(food.getQuantity().number().doubleValue(),
					food.getQuantity().units(), food.getContents());
		} else {
			return "%.2f %s of %s (turn #%d)".formatted(food.getQuantity().number().doubleValue(),
					food.getQuantity().units(), food.getContents(), food.getCreated());
		}
	}

	@Override
	public @Nullable String run() {
		final IUnit localUnit = unit;
		if (Objects.isNull(localUnit)) {
			return null;
		}
		final long workers = localUnit.stream().filter(IWorker.class::isInstance).count();
		BigDecimal remainingConsumption = new BigDecimal(4 * workers);
		while (remainingConsumption.signum() > 0) { // TODO: extract loop body as a function?
			cli.println("%.1f pounds of consumption unaccounted-for".formatted(remainingConsumption.doubleValue()));
			// TODO: should only count food *in the same place* (but unit movement away from HQ should ask user how much
			// food to take along, and to choose what food in a similar manner to this)
			final IResourcePile food = chooseFromList(getFoodFor(localUnit.owner(), turn),
					"Food stocks owned by player:", "No food stocks found", "Food to consume from:",
					ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT, ConsumptionApplet::describeFood);
			if (Objects.isNull(food)) {
				return null;
			}
			if (food.getQuantity().number().doubleValue() <= remainingConsumption.doubleValue()) {
				final Boolean resp = cli.inputBooleanInSeries("Consume all of the %s?".formatted(food.getContents()), "consume-all-of");
				if (Objects.isNull(resp)) {
					return null;
				} else if (resp) {
					model.reduceResourceBy(food, decimalize(food.getQuantity().number()),
							localUnit.owner());
					remainingConsumption = remainingConsumption.subtract(
							decimalize(food.getQuantity().number()));
					continue;
				} else { // TODO: extract this as a function?
					final BigDecimal amountToConsume = cli.inputDecimal("How many pounds of the %s to consume:".formatted(food.getContents()));
					if (Objects.isNull(amountToConsume)) {
						return null;
					}
					final BigDecimal minuend = amountToConsume.min(decimalize(
							food.getQuantity().number()));
					model.reduceResourceBy(food, minuend, localUnit.owner());
					remainingConsumption = remainingConsumption.subtract(minuend);
					continue;
				}
			} // else
			final Boolean resp = cli.inputBooleanInSeries("Eat all remaining %s from the %s?".formatted(
					remainingConsumption, food.getContents()), "all-remaining");
			if (Objects.isNull(resp)) {
				return null;
			} else if (resp) {
				model.reduceResourceBy(food, remainingConsumption, localUnit.owner());
				remainingConsumption = decimalize(0);
			} else { // TODO: extract this as a function?
				final BigDecimal amountToConsume = cli.inputDecimal("How many pounds of the %s to consume:".formatted(food.getContents()));
				if (Objects.isNull(amountToConsume)) {
					return null;
				} else if (amountToConsume.compareTo(remainingConsumption) > 0) {
					model.reduceResourceBy(food, remainingConsumption, localUnit.owner());
					remainingConsumption = decimalize(0);
					continue;
				} else {
					model.reduceResourceBy(food, amountToConsume, localUnit.owner());
					remainingConsumption = remainingConsumption.subtract(amountToConsume);
					continue;
				}
			}
		}
		return ""; // FIXME: Optionally report on what workers ate
	}
}
