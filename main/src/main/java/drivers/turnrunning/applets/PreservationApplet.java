package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.towns.IFortress;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import static lovelace.util.Decimalize.decimalize;

/* package */ class PreservationApplet extends AbstractTurnApplet {
	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	public PreservationApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
	}

	private static final List<String> COMMANDS = Collections.singletonList("preserve"); // TODO: Or simply "cook"?

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "Convert food into less-perishable form.";
	}

	private static String describePile(final IResourcePile pile) {
		if (pile.getCreated() < 0) {
			return String.format("%s of %s", pile.getQuantity(), pile.getContents());
		} else {
			return String.format("%s of %s (turn #%d)", pile.getQuantity(), pile.getContents(),
				pile.getCreated());
		}
	}

	@Override
	public @Nullable String run() {
		final StringBuilder builder = new StringBuilder();
		final IUnit unit = model.getSelectedUnit();
		final List<String> foods = new ArrayList<>();
		while (true) {
			// TODO: should verb be "preserve" or "cook" instead of "convert"?
			final IResourcePile item = chooseFromList(getFoodFor(unit.getOwner(),
				model.getMap().getCurrentTurn()), "Available food:", "No food available",
				"Choose food to convert:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT, PreservationApplet::describePile);
			if (item == null) {
				break;
			}
			final String convertedForm;
			final String tempOne = chooseFromList(foods, "Preserved food types:", "",
				"Type that converts into:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			if (tempOne == null) {
				final String tempTwo = cli.inputString("Type of food that converts into:");
				if (tempTwo == null || tempTwo.isEmpty()) {
					return null;
				} else {
					convertedForm = tempTwo;
					foods.add(tempTwo);
				}
			} else {
				convertedForm = tempOne;
			}
			final int turn;
			final Integer tempThree = cli.inputNumber("What turn should spoilage counter start from?");
			if (tempThree == null || tempThree < 0) {
				return null;
			}
			turn = tempThree;
			final BigDecimal newPounds =
				cli.inputDecimal("How many pounds of that are produced from this source?");
			if (newPounds == null || newPounds.signum() <= 0) {
				return null;
			}
			final BigDecimal subtrahend;
			final Boolean useAll = cli.inputBoolean("Use all ``item.quantity``?");
			if (useAll == null) {
				return null;
			} else if (useAll) {
				subtrahend = decimalize(item.getQuantity().getNumber());
			} else {
				subtrahend = cli.inputDecimal(String.format("How many %s to use?",
					item.getQuantity().getUnits()));
				if (subtrahend == null || subtrahend.signum() <= 0) {
					return null;
				}
			}
			model.reduceResourceBy(item, subtrahend, unit.getOwner());
			// TODO: findHQ() should instead take the unit and find the fortress in the same tile, if any
			final IFortress hq = model.findHQ(unit.getOwner());
			if (hq == null) {
				model.addResource(unit, idf.createID(), "food", convertedForm,
						new Quantity(newPounds, "pounds"), turn);
			} else {
				model.addResource(hq, idf.createID(), "food", convertedForm, new Quantity(newPounds, "pounds"), turn);
			}
			final String results = cli.inputMultilineString("Description for results:");
			if (results == null) {
				return null;
			}
			builder.append(results);
		}
		return builder.toString();
	}
}
