package drivers.turnrunning.applets;

import common.idreg.IDRegistrar;
import common.map.Player;
import common.map.fixtures.IResourcePile;
import drivers.common.cli.ICLIHelper;
import drivers.turnrunning.ITurnRunningModel;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;

// Note we *deliberately* do not make a factory for service-discovery to find a way to build an instance of this class.
public class SpoilageApplet extends AbstractTurnApplet {
	/**
	 * A logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SpoilageApplet.class.getName());

	public SpoilageApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli, idf);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
		owner = model.getMap().getCurrentPlayer();
		turn = model.getMap().getCurrentTurn();
	}

	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

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

	@Nullable
	@Override
	public String run() {
		final StringBuilder buffer = new StringBuilder();
		for (IResourcePile food : getFoodFor(owner, turn)) {
			if (turn < 0) { // rations whose spoilage isn't tracked
				continue;
			}
			cli.print("Food is ");
			cli.println(food.toString());
			final FoodType type = FoodType.askFoodType(cli, food.getKind());
			if (type == null) {
				LOGGER.warning("Didn't get a food type");
				return null;
			}
			final Boolean spoiled = type.hasSpoiled(food, turn, cli);
			if (spoiled == null) {
				LOGGER.warning("EOF on has-this-spoiled");
				return null;
			} else if (spoiled) {
				BigDecimal spoilage = type.amountSpoiling(food.getQuantity(), cli);
				if (spoilage == null) {
					LOGGER.warning("Non-numeric spoilage amount");
					return null;
				}
				buffer.append(String.format("%.2f pounds of %s spoiled.%n%n", spoilage.doubleValue(),
					food.toString()));
				model.reduceResourceBy(food, spoilage, owner);
			}
		}
		return buffer.toString();
	}
}