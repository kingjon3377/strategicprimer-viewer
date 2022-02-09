package drivers.turnrunning.applets;

import common.map.fixtures.mobile.IUnit;
import drivers.common.cli.ICLIHelper;

import common.idreg.IDRegistrar;

import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.Quantity;

import java.math.BigDecimal;

import static lovelace.util.Decimalize.decimalize;

import common.map.fixtures.terrain.Forest;

import common.map.Point;
import common.map.HasExtent;
import common.map.TileFixture;

import drivers.turnrunning.ITurnRunningModel;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

/* package */ class WoodcuttingApplet extends AbstractTurnApplet {
	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	public WoodcuttingApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli, idf);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
	}

	private static final List<String> COMMANDS = Collections.unmodifiableList(Arrays.asList("woodcutting"));

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "cut down trees for wood or to clear land";
	}

	// TODO: To simplify our lives in the crippled type-system of Java, make HasPopulation and HasExtent extend TileFixture
	private <T extends HasExtent<? extends TileFixture>&TileFixture> void
			reduceExtent(final Point point, final T fixture, final BigDecimal acres) {
		model.reduceExtent(point, fixture, true, acres);
	}

	@Override
	@Nullable
	public String run() {
		final StringBuilder builder = new StringBuilder();
		// FIXME: support other forms of woodcutting: logs, long beams, land-clearing, etc.
		final Point loc = confirmPoint("Where are they cutting wood?");
		if (loc == null) {
			return null;
		}
		final int workers;
		final Integer tempW = cli.inputNumber("How many workers cutting?");
		if (tempW != null && tempW > 0) {
			workers = tempW;
		} else {
			return null;
		}
		final int baseHours;
		final Integer tempBH = cli.inputNumber("How many hours into a tree were they before?");
		if (tempBH != null && tempBH >= 0) {
			baseHours = tempBH;
		} else {
			return null;
		}
		final int totalHours;
		final Integer tempTH = cli.inputNumber("How many hours does each worker work?");
		if (tempTH != null && tempTH > 0) {
			totalHours = tempTH * workers + baseHours;
		} else {
			return null;
		}
		int treeCount = totalHours / 100;
		cli.print(String.format("With unskilled workers, that would be %d trees", treeCount));
		if (totalHours % 100 == 0) {
			cli.println(".");
		} else {
			cli.println(String.format(" and %d into the next.", totalHours % 100));
		}
		final Boolean tCorrect = cli.inputBoolean("Is that correct?");
		if (tCorrect == null) {
			return null;
		} else if (tCorrect) {
			builder.append(String.format("The %d workers cut down and process %d trees", workers, treeCount));
			if (totalHours % 100 != 0) {
				cli.println(String.format(" and get %d into the next", totalHours % 100));
			}
		} else {
			final String str = cli.inputMultilineString("Description of trees cut:");
			if (str == null) {
				return null;
			} else {
				builder.append(str);
			}
			final Integer count = cli.inputNumber("Number of trees cut and processed: ");
			if (count != null && count > 0) {
				treeCount = count;
			} else {
				return null;
			}
		}
		int footage = treeCount * 300;
		final Boolean fCorrect = cli.inputBoolean(String.format("Is %d cubic feet correct?", footage));
		if (fCorrect == null) {
			return null;
		} else if (fCorrect) {
			builder.append(String.format(", producing %d cubic feet of wood", footage));
		} else {
			final String str = cli.inputMultilineString("Description of production:");
			if (str == null) {
				return null;
			} else {
				builder.append(str);
			}
			final Integer count = cli.inputNumber("Cubic feet production-ready wood: ");
			if (count == null) { // TODO: or < 0? But allow 0 to skip adding resource.
				return null;
			} else {
				footage = count;
			}
		}
		if (footage > 0) {
			final IUnit unit = model.getSelectedUnit();
			// FIXME: Use model.addResource() rather than creating pile here ourselves
			if (!model.addExistingResource(new ResourcePileImpl(idf.createID(), "wood",
					"production-ready wood", new Quantity(footage, "cubic feet")), unit.getOwner())) {
				cli.println("Failed to find a fortress to add to in any map");
			}
		}
		if (treeCount > 7) {
			final Forest forest = chooseFromList(model.getMap().getFixtures(loc).stream()
					.filter(Forest.class::isInstance).map(Forest.class::cast)
					.collect(Collectors.toList()),
				"Forests on tile:", "No forests on tile", "Forest being cleared: ", false);
			if (forest != null && forest.getAcres().doubleValue() > 0.0) {
				BigDecimal acres = decimalize(treeCount * 10 / 72)
					.divide(decimalize(100), RoundingMode.HALF_EVEN)
					.min(decimalize(forest.getAcres()));
				final Boolean aCorrect = cli.inputBoolean(String.format(
					"Is %.2f (of %.2f) cleared correct?", acres.doubleValue(),
					forest.getAcres().doubleValue()));
				if (aCorrect == null) {
					return null;
				} else if (aCorrect) {
					builder.append(String.format(", clearing %.2f acres (~ %d sq ft) of land.`",
						acres, acres.multiply(decimalize(43560)).intValue()));
				} else {
					final String str = cli.inputMultilineString("Descriptoin of cleared land:");
					if (str == null) {
						return null;
					} else {
						builder.append(str);
					}
					final BigDecimal tAcres = cli.inputDecimal("Acres cleared:");
					if (tAcres == null) {
						return null;
					} else {
						acres = tAcres;
					}
				}
				reduceExtent(loc, forest, acres);
			}
		}
		return builder.toString();
	}
}
