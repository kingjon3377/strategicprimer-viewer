package drivers.turnrunning.applets;

import legacy.map.IFixture;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.mobile.IUnit;
import drivers.common.cli.ICLIHelper;

import legacy.idreg.IDRegistrar;

import legacy.map.fixtures.ResourcePileImpl;

import java.math.BigDecimal;

import static lovelace.util.Decimalize.decimalize;

import legacy.map.fixtures.terrain.Forest;

import legacy.map.Point;
import legacy.map.HasExtent;
import legacy.map.TileFixture;

import drivers.turnrunning.ITurnRunningModel;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

/* package */ class WoodcuttingApplet extends AbstractTurnApplet {
	private final ITurnRunningModel model;
	private final ICLIHelper cli;
	private final IDRegistrar idf;

	public WoodcuttingApplet(final ITurnRunningModel model, final ICLIHelper cli, final IDRegistrar idf) {
		super(model, cli);
		this.model = model;
		this.cli = cli;
		this.idf = idf;
	}

	private static final List<String> COMMANDS = Collections.singletonList("woodcutting");

	@Override
	public List<String> getCommands() {
		return COMMANDS;
	}

	@Override
	public String getDescription() {
		return "cut down trees for wood or to clear land";
	}

	// TODO: To simplify our lives in Java's crippled type-system, make HasPopulation and HasExtent extend TileFixture
	private <T extends HasExtent<? extends TileFixture> & TileFixture> void
	reduceExtent(final Point point, final T fixture, final BigDecimal acres) {
		model.reduceExtent(point, fixture, IFixture.CopyBehavior.ZERO, acres);
	}

	@Override
	public @Nullable String run() {
		final StringBuilder builder = new StringBuilder();
		// FIXME: support other forms of woodcutting: logs, long beams, land-clearing, etc.
		final Point loc = confirmPoint("Where are they cutting wood?");
		if (Objects.isNull(loc)) {
			return null;
		}
		final int workers;
		final Integer tempW = cli.inputNumber("How many workers cutting?");
		if (!Objects.isNull(tempW) && tempW > 0) {
			workers = tempW;
		} else {
			return null;
		}
		final int baseHours;
		final Integer tempBH = cli.inputNumber("How many hours into a tree were they before?");
		if (!Objects.isNull(tempBH) && tempBH >= 0) {
			baseHours = tempBH;
		} else {
			return null;
		}
		final int totalHours;
		final Integer tempTH = cli.inputNumber("How many hours does each worker work?");
		if (!Objects.isNull(tempTH) && tempTH > 0) {
			totalHours = tempTH * workers + baseHours;
		} else {
			return null;
		}
		int treeCount = totalHours / 100;
		cli.printf("With unskilled workers, that would be %d trees%n", treeCount);
		if (totalHours % 100 == 0) {
			cli.println(".");
		} else {
			cli.printf(" and %d into the next.%n", totalHours % 100);
		}
		final Boolean tCorrect = cli.inputBoolean("Is that correct?");
		if (Objects.isNull(tCorrect)) {
			return null;
		} else if (tCorrect) {
			builder.append("The %d workers cut down and process %d trees".formatted(workers, treeCount));
			if (totalHours % 100 != 0) {
				cli.printf(" and get %d into the next%n", totalHours % 100);
			}
		} else {
			final String str = cli.inputMultilineString("Description of trees cut:");
			if (Objects.isNull(str)) {
				return null;
			} else {
				builder.append(str);
			}
			final Integer count = cli.inputNumber("Number of trees cut and processed: ");
			if (!Objects.isNull(count) && count > 0) {
				treeCount = count;
			} else {
				return null;
			}
		}
		int footage = treeCount * 300;
		final Boolean fCorrect = cli.inputBoolean("Is %d cubic feet correct?".formatted(footage));
		if (Objects.isNull(fCorrect)) {
			return null;
		} else if (fCorrect) {
			builder.append(", producing %d cubic feet of wood".formatted(footage));
		} else {
			final String str = cli.inputMultilineString("Description of production:");
			if (Objects.isNull(str)) {
				return null;
			} else {
				builder.append(str);
			}
			final Integer count = cli.inputNumber("Cubic feet production-ready wood: ");
			if (Objects.isNull(count)) { // TODO: or < 0? But allow 0 to skip adding resource.
				return null;
			} else {
				footage = count;
			}
		}
		if (footage > 0) {
			final IUnit unit = model.getSelectedUnit();
			// FIXME: Use model.addResource() rather than creating pile here ourselves
			if (Objects.isNull(unit)) {
				cli.println("No selected unit");
			} else if (!model.addExistingResource(new ResourcePileImpl(idf.createID(), "wood",
					"production-ready wood", new LegacyQuantity(footage, "cubic feet")), unit.owner())) {
				cli.println("Failed to find a fortress to add to in any map");
			}
		}
		if (treeCount > 7) {
			final Forest forest = chooseFromList(model.getMap().getFixtures(loc).stream()
							.filter(Forest.class::isInstance).map(Forest.class::cast)
							.collect(Collectors.toList()),
					"Forests on tile:", "No forests on tile", "Forest being cleared: ",
					ICLIHelper.ListChoiceBehavior.ALWAYS_PROMPT);
			if (!Objects.isNull(forest) && forest.getAcres().doubleValue() > 0.0) {
				BigDecimal acres = decimalize(treeCount * 10 / 72)
						.divide(decimalize(100), RoundingMode.HALF_EVEN)
						.min(decimalize(forest.getAcres()));
				final Boolean aCorrect = cli.inputBoolean("Is %.2f (of %.2f) cleared correct?".formatted(
						acres.doubleValue(),
						forest.getAcres().doubleValue()));
				if (Objects.isNull(aCorrect)) {
					return null;
				} else if (aCorrect) {
					// TODO: Make the Decimal constant a static-final field
					builder.append(", clearing %.2f acres (~ %d sq ft) of land.".formatted(
							acres, acres.multiply(decimalize(43560)).intValue()));
				} else {
					final String str = cli.inputMultilineString("Description of cleared land:");
					if (Objects.isNull(str)) {
						return null;
					} else {
						builder.append(str);
					}
					final BigDecimal tAcres = cli.inputDecimal("Acres cleared:");
					if (Objects.isNull(tAcres)) {
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
