package drivers.exploration;

import legacy.map.ILegacyMap;
import legacy.map.Player;
import legacy.map.Point;
import legacy.map.TileFixture;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.Immortal;
import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.TownStatus;
import legacy.map.fixtures.towns.Village;
import drivers.common.cli.ICLIHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

/* package */ final class ExplorationAutomationConfig {
	public ExplorationAutomationConfig(final Player player) {
		this.player = player;
		conditions = List.of(new Condition<>("at others' fortresses",
						fixture -> "a fortress belonging to " + fixture.owner(), IFortress.class),
				new Condition<>("at active towns",
						fixture -> "a %s active %s".formatted(fixture.getTownSize(), fixture.getKind()),
						AbstractTown.class, t -> TownStatus.Active == t.getStatus()),
				new Condition<>("at inactive towns", fixture -> "a %s %s %s".formatted(fixture.getTownSize(),
								fixture.getStatus(), fixture.getKind()),
						AbstractTown.class, t -> TownStatus.Active != t.getStatus()),
				new Condition<>("at independent villages", "an independent village",
						Village.class, v -> v.owner().isIndependent()), new Condition<>("at other players' villages",
						"another player's village",
						Village.class, v -> !v.owner().equals(this.player), v -> !v.owner().isIndependent()),
				// TODO: For "your villages" (and perhaps other towns), include name in stop message
				new Condition<>("at villages sworn to you", "one of your villages",
						Village.class, v -> v.owner().equals(this.player)),
				new Condition<>("on meeting other players' units", unit -> "a unit belonging to " + unit.owner(),
						IUnit.class, u -> !u.owner().equals(this.player), u -> !u.owner().isIndependent()),
				new Condition<>("on meeting independent units", "an independent unit", IUnit.class,
						// TODO: Provide helper default methods isIndependent() and sameOwner() in HasOwner?
						u -> u.owner().isIndependent()),
				new Condition<>("on meeting an immortal", i -> "a(n) " + i.getShortDescription(), Immortal.class));
	}

	private final Player player;

	public Player getPlayer() {
		return player;
	}

	private static final class Condition<Type extends TileFixture> {
		/**
		 * @param configExplanation A description to use in the question asking whether to stop for this condition.
		 * @param stopExplanation   A description to use when stopping because of this condition.
		 * @param conditions        Returns true when a tile fixture matches all of these conditions.
		 */
		@SafeVarargs
		public Condition(final String configExplanation, final String stopExplanation, final Class<Type> cls,
						 final Predicate<Type>... conditions) {
			this.configExplanation = configExplanation;
			this.stopExplanation = ignored -> stopExplanation;
			this.conditions = List.of(conditions);
			this.cls = cls;
		}

		/**
		 * @param configExplanation A description to use in the question asking whether to stop for this condition.
		 * @param stopExplanation   A factory for a description to use when stopping because of this condition.
		 * @param conditions        Returns true when a tile fixture matches all of these conditions.
		 */
		@SafeVarargs
		public Condition(final String configExplanation, final Function<Type, String> stopExplanation,
						 final Class<Type> cls, final Predicate<Type>... conditions) {
			this.configExplanation = configExplanation;
			this.stopExplanation = stopExplanation;
			this.conditions = List.of(conditions);
			this.cls = cls;
		}

		private final Class<Type> cls;

		/**
		 * A description to use in the question asking whether to stop for this condition.
		 */
		private final String configExplanation;

		/**
		 * A description to use in the question asking whether to stop for this condition.
		 */
		public String getConfigExplanation() {
			return configExplanation;
		}

		/**
		 * A factory for a description to use when stopping because of this condition.
		 */
		private final Function<Type, String> stopExplanation;

		/**
		 * A factory for a description to use when stopping because of this condition.
		 */
		public Function<Type, String> getStopExplanation() {
			return stopExplanation;
		}

		/**
		 * Returns true when a tile fixture matches all of these conditions.
		 */
		private final List<Predicate<Type>> conditions;

		private @Nullable Type matched = null;

		private boolean allConditions(final TileFixture fixture) {
			if (cls.isInstance(fixture)) {
				final Type typed = cls.cast(fixture);
				for (final Predicate<Type> condition : conditions) {
					if (!condition.test(typed)) {
						return false;
					}
				}
				matched = typed;
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Returns true when the given tile matches this condition.
		 */
		public boolean matches(final ILegacyMap map, final Point point) {
			return map.streamFixtures(point).anyMatch(this::allConditions);
		}

		/**
		 * The description of the fixture that matched, if any, so caller doesn't have to figure out which fixture
		 * matched to use {@link #stopExplanation}.
		 */
		public String explain() {
			return Optional.ofNullable(matched).map(stopExplanation).orElse("");
		}
	}

	private final List<Condition<? extends TileFixture>> conditions;

	private @Nullable List<Condition<? extends TileFixture>> enabledConditions = null;

	public boolean stopAtPoint(final ICLIHelper cli, final ILegacyMap map, final Point point) {
		final List<Condition<? extends TileFixture>> localEnabledConditions;
		if (Objects.isNull(enabledConditions)) {
			final List<Condition<? extends TileFixture>> temp = new ArrayList<>();
			for (final Condition<? extends TileFixture> condition : conditions) {
				switch (cli.inputBooleanInSeries("Stop for instructions " +
						condition.getConfigExplanation() + "?")) {
					case YES -> temp.add(condition);
					case NO -> { // Do nothing
					}
					case QUIT, EOF -> {
						// We want to abort at least the caller's loop.
						// TODO: Somehow signal EOF (in that case) to callers
						return true;
					}
				}
			}
			localEnabledConditions = Collections.unmodifiableList(temp);
			enabledConditions = localEnabledConditions;
		} else {
			localEnabledConditions = enabledConditions;
		}
		final Condition<? extends TileFixture> matchingCondition = localEnabledConditions.stream()
				.filter(c -> c.matches(map, point)).findFirst().orElse(null);
		if (Objects.isNull(matchingCondition)) {
			return false;
		} else {
			cli.printf("There is %s here, so the explorer stops.%n", matchingCondition.explain());
			return true;
		}
	}
}
