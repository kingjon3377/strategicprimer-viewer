package drivers.turnrunning.applets;

import drivers.common.cli.ICLIHelper;

import java.math.BigDecimal;

import static lovelace.util.Decimalize.decimalize;

import legacy.map.fixtures.IResourcePile;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import legacy.map.fixtures.LegacyQuantity;
import org.jetbrains.annotations.Nullable;

/* package */ enum FoodType {
	Milk(2, 3, 4, null, decimalize(0.5), decimalize(8), "milk"),
	Meat(2, 3, 4, 8, decimalize(0.25), decimalize(2), "meat"),
	Grain(7, 10, null, null, decimalize(0.125), decimalize(1), "grain"),
	SlowFruit(6, 8, 12, null, decimalize(0.125), decimalize(1), "slow-spoiling fruit"),
	QuickFruit(3, 6, 8, null, decimalize(0.25), decimalize(1), "fast-spoiling fruit"),
	// TODO: Add additional cases
	Other(null, null, null, null, null, null, "other");

	public static @Nullable FoodType askFoodType(final ICLIHelper cli, final String foodKind) {
		for (final FoodType type : values()) {
			switch (cli.inputBooleanInSeries("Is \"%s\" %s?".formatted(foodKind, type),
					foodKind + type)) {
				case YES -> {
					return type;
				}
				case NO -> { // Do nothing
				}
				case QUIT, EOF -> {
					return null;
				}
			}
		}
		return null;
	}

	private final @Nullable Integer keepsFor;

	public @Nullable Integer getKeepsFor() {
		return keepsFor;
	}

	private final @Nullable Integer keepsForIfCool;

	public @Nullable Integer getKeepsForIfCool() {
		return keepsForIfCool;
	}

	private final @Nullable Integer keepsForRefrigerated;

	public @Nullable Integer getKeepsForRefrigerated() {
		return keepsForRefrigerated;
	}

	private final @Nullable Integer keepsForFrozen;

	public @Nullable Integer getKeepsForFrozen() {
		return keepsForFrozen;
	}

	private final @Nullable BigDecimal fractionSpoilingDaily;

	public @Nullable BigDecimal getFractionSpoilingDaily() {
		return fractionSpoilingDaily;
	}

	private final @Nullable BigDecimal minimumSpoilage;

	public @Nullable BigDecimal getMinimumSpoilage() {
		return minimumSpoilage;
	}

	private final String string;

	@Override
	public String toString() {
		return string;
	}

	FoodType(final @Nullable Integer keepsFor, final @Nullable Integer keepsForIfCool,
			 final @Nullable Integer keepsForRefrig, final @Nullable Integer keepsForFrozen,
			 final @Nullable BigDecimal fracSpoilingDaily, final @Nullable BigDecimal minSpoilage,
			 final String str) {
		this.keepsFor = keepsFor;
		this.keepsForIfCool = keepsForIfCool;
		keepsForRefrigerated = keepsForRefrig;
		this.keepsForFrozen = keepsForFrozen;
		fractionSpoilingDaily = fracSpoilingDaily;
		minimumSpoilage = minSpoilage;
		string = str;
	}

	public @Nullable Boolean hasSpoiled(final IResourcePile pile, final int turn, final ICLIHelper cli) {
		final int age = turn - pile.getCreated();
		if (turn < 0 || pile.getCreated() < 0) { // Either corrupt turn information or non-spoiling rations
			return false;
		} else if (pile.getCreated() >= turn) { // Created this turn or in the future
			return false;
		} else if (Objects.nonNull(keepsFor) && age < keepsFor) {
			return false;
		} else if (Objects.nonNull(keepsForIfCool) && age < keepsForIfCool) {
			switch (cli.inputBooleanInSeries("Was this kept cool?", pile.getKind() + string + "cool")) {
				case YES -> {
					return false;
				}
				case NO -> { // Do nothing
				}
				case QUIT, EOF -> {
					return null;
				}
			}
		}
		if (Objects.nonNull(keepsForRefrigerated) && age < keepsForRefrigerated) {
			switch (cli.inputBooleanInSeries("Was this kept refrigerated?",
					pile.getKind() + string + "refrig")) {
				case YES -> {
					return false;
				}
				case NO -> { // Do nothing
				}
				case QUIT, EOF -> {
					return null;
				}
			}
		}
		if (Objects.nonNull(keepsForFrozen) && age < keepsForFrozen) {
			switch (cli.inputBooleanInSeries("Was this kept frozen?",
					pile.getKind() + string + "frozen")) {
				case YES -> {
					return false;
				}
				case NO -> { // Do nothing
				}
				case QUIT, EOF -> {
					return null;
				}
			}
		}
		if (Stream.of(keepsFor, keepsForIfCool, keepsForRefrigerated, keepsForFrozen).allMatch(Objects::isNull)) {
			return switch (cli.inputBooleanInSeries("Has this spoiled?", pile.getKind() + string + "other")) {
				case YES -> true;
				case NO -> false;
				case QUIT, EOF -> null;
			};
		} else {
			return true;
		}
	}

	public @Nullable BigDecimal amountSpoiling(final LegacyQuantity qty, final ICLIHelper cli) {
		final BigDecimal amt = decimalize(qty.number());
		final BigDecimal fractional = Optional.ofNullable(fractionSpoilingDaily).map(amt::multiply).orElse(null);
		return Stream.of(fractional, minimumSpoilage).filter(Objects::nonNull)
				.max(Comparator.naturalOrder()).orElseGet(() -> cli.inputDecimal("How many pounds spoil?"));
	}
}
