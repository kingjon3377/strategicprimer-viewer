package drivers.turnrunning.applets;

import common.map.Player;
import common.map.fixtures.IResourcePile;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

// Note we *deliberately* do not make a factory for service-discovery to find a way to build an instance of this class.
public class SpoilageApplet extends AbstractTurnApplet {
	public SpoilageApplet(final ITurnRunningModel model, final ICLIHelper cli) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		owner = model.getMap().getCurrentPlayer();
		turn = model.getMap().getCurrentTurn();
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;

	private Player owner;

	public Player getOwner() {
		return owner;
	}

	public void setOwner(final Player owner) {
		this.owner = owner;
	}

	private int turn;

	public int getTurn() {
		return turn;
	}

	public void setTurn(final int turn) {
		this.turn = turn;
	}

	private static final List<String> COMMANDS = Collections.singletonList("spoilage");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "Determine what food has spoiled";
	}

	@Override
	public @Nullable String run() {
		final StringBuilder buffer = new StringBuilder();
		for (final IResourcePile food : getFoodFor(owner, turn)) {
			if (food.getCreated() < 0 || turn < 0) { // rations whose spoilage isn't tracked or the current turn isn't set
				continue;
			}
			cli.print("Food is ");
			cli.println(food.toString());
			final FoodType type = FoodType.askFoodType(cli, food.getContents());
			if (type == null) {
				LovelaceLogger.warning("Didn't get a food type");
				return null;
			}
			final Boolean spoiled = type.hasSpoiled(food, turn, cli);
			if (spoiled == null) {
				LovelaceLogger.warning("EOF on has-this-spoiled");
				return null;
			} else if (spoiled) {
				final BigDecimal spoilage = type.amountSpoiling(food.getQuantity(), cli);
				if (spoilage == null) {
					LovelaceLogger.warning("Non-numeric spoilage amount");
					return null;
				}
				buffer.append(String.format("%.2f pounds of %s spoiled.%n%n", spoilage.doubleValue(),
						food));
				model.reduceResourceBy(food, spoilage, owner);
			}
		}
		return buffer.toString();
	}
}
