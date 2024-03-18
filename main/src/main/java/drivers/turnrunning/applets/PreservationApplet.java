package drivers.turnrunning.applets;

import legacy.idreg.IDRegistrar;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.towns.IFortress;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
			return "%s of %s".formatted(pile.getQuantity(), pile.getContents());
		} else {
			return "%s of %s (turn #%d)".formatted(pile.getQuantity(), pile.getContents(),
					pile.getCreated());
		}
	}

	@Override
	public @Nullable String run() {
		final StringBuilder builder = new StringBuilder();
		final IUnit unit = model.getSelectedUnit();
		if (Objects.isNull(unit)) {
			cli.println("No current unit");
			return "";
		}
		final List<String> foods = new ArrayList<>();
		while (true) {
			// TODO: should verb be "preserve" or "cook" instead of "convert"?
			final IResourcePile item = chooseFromList(getFoodFor(unit.owner(),
							model.getMap().getCurrentTurn()), "Available food:", "No food available",
					"Choose food to convert:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT, PreservationApplet::describePile);
			if (Objects.isNull(item)) {
				break;
			}
			final String convertedForm;
			final String tempOne = chooseFromList(foods, "Preserved food types:", "",
					"Type that converts into:", ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			if (Objects.isNull(tempOne)) {
				final String tempTwo = cli.inputString("Type of food that converts into:");
				if (Objects.isNull(tempTwo) || tempTwo.isEmpty()) {
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
			if (Objects.isNull(tempThree) || tempThree < 0) {
				return null;
			}
			turn = tempThree;
			final BigDecimal newPounds =
					cli.inputDecimal("How many pounds of that are produced from this source?");
			if (Objects.isNull(newPounds) || newPounds.signum() <= 0) {
				return null;
			}
			final BigDecimal subtrahend;
			final Boolean useAll = cli.inputBoolean("Use all %s?".formatted(item.getQuantity()));
			if (Objects.isNull(useAll)) {
				return null;
			} else if (useAll) {
				subtrahend = decimalize(item.getQuantity().number());
			} else {
				subtrahend = cli.inputDecimal("How many %s to use?".formatted(item.getQuantity().units()));
				if (Objects.isNull(subtrahend) || subtrahend.signum() <= 0) {
					return null;
				}
			}
			model.reduceResourceBy(item, subtrahend, unit.owner());
			// TODO: findHQ() should instead take the unit and find the fortress in the same tile, if any
			final IFortress hq = model.findHQ(unit.owner());
			if (Objects.isNull(hq)) {
				model.addResource(unit, idf.createID(), "food", convertedForm,
						new LegacyQuantity(newPounds, "pounds"), turn);
			} else {
				model.addResource(hq, idf.createID(), "food", convertedForm, new LegacyQuantity(newPounds, "pounds"), turn);
			}
			final String results = cli.inputMultilineString("Description for results:");
			if (Objects.isNull(results)) {
				return null;
			}
			builder.append(results);
		}
		return builder.toString();
	}
}
