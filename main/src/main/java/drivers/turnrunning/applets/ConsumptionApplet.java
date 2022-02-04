package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;

import common.map.fixtures.IResourcePile;

import java.math.BigDecimal;

import static lovelace.util.Decimalize.decimalize;

import drivers.turnrunning.ITurnRunningModel;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * We <em>deliberately</em> do not make a factory annotated for service discovery.
 */
public class ConsumptionApplet extends AbstractTurnApplet {
	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	public ConsumptionApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) {
		super(model, cli, idf);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
		turn = model.getMap().getCurrentTurn();
		unit = model.getSelectedUnit();
	}

	private int turn;

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	@Nullable
	private IUnit unit;

	@Nullable
	public IUnit getUnit() {
		return unit;
	}

	public void setUnit(@Nullable IUnit unit) {
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

	private static String describeFood(IResourcePile food) {
		if (food.getCreated() < 0) {
			return String.format("%.2f %s of %s", food.getQuantity().getNumber(),
				food.getQuantity().getUnits(), food.getContents());
		} else {
			return String.format("%.2f %s of %s (turn #%d)",  food.getQuantity().getNumber(),
				food.getQuantity().getUnits(), food.getContents(), food.getCreated());
		}
	}

	@Override
	@Nullable
	public String run() {
		IUnit localUnit = unit;
		if (localUnit == null) {
			return null;
		}
		final long workers = localUnit.stream().filter(IWorker.class::isInstance).count();
		BigDecimal remainingConsumption = new BigDecimal(4 * workers);
		BigDecimal zero = new BigDecimal(0);
		while (remainingConsumption.compareTo(zero) > 1) { // TODO: extract loop body as a function?
			cli.println(String.format("%.1f pounds of consumption unaccounted-for",
				remainingConsumption.doubleValue()));
			IResourcePile food = chooseFromList(getFoodFor(localUnit.getOwner(), turn),
				"Food stocks owned by player:", "No food stocks found", "Food to consume from:",
				false, ConsumptionApplet::describeFood); // TODO: should only count food *in the same place* (but unit movement away from HQ should ask user how much food to take along, and to choose what food in a similar manner to this)
			if (food == null) {
				return null;
			}
			if (food.getQuantity().getNumber().doubleValue() <= remainingConsumption.doubleValue()) {
				Boolean resp = cli.inputBooleanInSeries(String.format("Consume all of the %s?",
					food.getContents()), "consume-all-of");
				if (resp == null) {
					return null;
				} else if (resp) {
					model.reduceResourceBy(food, decimalize(food.getQuantity().getNumber()),
						localUnit.getOwner());
					remainingConsumption = remainingConsumption.subtract(
						decimalize(food.getQuantity().getNumber()));
					continue;
				} else { // TODO: extract this as a function?
					BigDecimal amountToConsume = cli.inputDecimal(String.format(
						"How many pounds of the %s to consume:", food.getContents()));
					if (amountToConsume != null) { // TODO: invert
						BigDecimal minuend = amountToConsume.min(decimalize(
							food.getQuantity().getNumber()));
						model.reduceResourceBy(food, minuend, localUnit.getOwner());
						remainingConsumption = remainingConsumption.subtract(minuend);
						continue;
					} else {
						return null;
					}
				}
			} // else
			Boolean resp = cli.inputBooleanInSeries(String.format("Eat all remaining %s from the %s?",
				remainingConsumption, food.getContents()), "all-remaining");
			if (resp == null) {
				return null;
			} else if (resp) {
				model.reduceResourceBy(food, remainingConsumption, localUnit.getOwner());
				remainingConsumption = decimalize(0);
			} else { // TODO: extract this as a function?
				BigDecimal amountToConsume = cli.inputDecimal(String.format(
					"How many pounds of the %s to consume:", food.getContents()));
				if (amountToConsume == null) {
					return null;
				} else if (amountToConsume.compareTo(remainingConsumption) > 0) {
					model.reduceResourceBy(food, remainingConsumption, localUnit.getOwner());
					remainingConsumption = decimalize(0);
					continue;
				} else {
					model.reduceResourceBy(food, amountToConsume, localUnit.getOwner());
					remainingConsumption = remainingConsumption.subtract(amountToConsume);
					continue;
				}
			}
		}
		return ""; // FIXME: Optionally report on what workers ate
	}
}
